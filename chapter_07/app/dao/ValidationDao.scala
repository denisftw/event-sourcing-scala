package dao

import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import com.appliedscala.events.LogRecord

import java.util.UUID
import com.appliedscala.events.answer._
import com.appliedscala.events.question.{QuestionCreated, QuestionDeleted}
import com.appliedscala.events.tag.{TagCreated, TagDeleted}
import com.appliedscala.events.user.{UserActivated, UserDeactivated}
import play.api.Logger
import scalikejdbc._

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Created by denis on 12/5/16.
  */
class ValidationDao(implicit mat: Materializer) {
  private val log = Logger(this.getClass)
  import util.ThreadPools.CPU
  def validateSingle(event: LogRecord): Future[Option[String]] = {
    processSingleEvent(event, skipValidation = false)
  }
  def refreshState(events: Seq[LogRecord], fromScratch: Boolean): Future[Option[String]] = {
    resetState(fromScratch).flatMap {
      case Some(value) => Future.successful(Some(value))
      case None => processEvents(events, skipValidation = true)
    }
  }

  import util.ThreadPools.IO
  private def validateTagCreated(tagText: String, userId: UUID): Future[Option[String]] = {
    validateUser(userId) {
      val maybeExistingT = Future {
        NamedDB(Symbol("validation")).readOnly { implicit session =>
          sql"select tag_id from tags where tag_text = $tagText".
            map(_.string("tag_id")).headOption().apply()
        }
      }
      maybeExistingT.transform {
        case Success(Some(_)) => Success(Some("The tag already exists!"))
        case Success(None) => Success(None)
        case _ => Success(Some("Validation state exception!"))
      }
    }
  }

  private def updateTagCreated(tagId: UUID, tagText: String): Future[Option[String]] = {
    invokeUpdate {
      NamedDB(Symbol("validation")).localTx { implicit session =>
        sql"insert into tags(tag_id, tag_text) values($tagId, $tagText)".
          update().apply()
      }
    }
  }

  private def validateTagDeleted(tagId: UUID, userId: UUID): Future[Option[String]] = {
    validateUser(userId) {
      Future {
        val maybeExistingTag = NamedDB(Symbol("validation")).readOnly { implicit session =>
          sql"select tag_id from tags tn where tag_id = ${tagId}".
            map(_.string("tag_id")).headOption().apply()
        }
        val maybeDependentQuestions = NamedDB(Symbol("validation")).readOnly { implicit session =>
          sql"select question_id from tag_question tq where tag_id = ${tagId}".
            map(_.string("question_id")).list().apply()
        }
        (maybeExistingTag, maybeDependentQuestions) match {
          case (None, _) => Some("This tag doesn't exist!")
          case (_, head :: tail) => Some("There are questions that depend on this tag!")
          case (_, Nil) => None
        }
      }
    }
  }

  private def updateTagDeleted(tagId: UUID): Future[Option[String]] = {
    invokeUpdate {
      NamedDB(Symbol("validation")).localTx { implicit session =>
        sql"delete from tags where tag_id = ${tagId}".update().apply()
      }
    }
  }

  private def validateUserActivated(userId: UUID): Future[Option[String]] = {
    val isActivatedT = isActivated(userId)
    isActivatedT.transform {
      case Success(true) => Success(Some("The user is already activated!"))
      case Failure(_) => Success(Some("Validation state exception"))
      case Success(false) => Success(None)
    }
  }

  private def updateUserActivated(userId: UUID): Future[Option[String]] = {
    invokeUpdate{
      NamedDB(Symbol("validation")).localTx { implicit session =>
        sql"insert into active_users(user_id) values($userId)".
          update().apply()
      }
    }
  }

  private def validateUser(userId: UUID)(block: => Future[Option[String]]): Future[Option[String]] = {
    val isActivatedT = isActivated(userId)
    isActivatedT.transformWith {
      case Success(false) => Future.successful(Some("The user is not activated!"))
      case Failure(_) => Future.successful(Some("Validation state exception!"))
      case Success(true) => block
    }
  }

  private def isActivated(userId: UUID): Future[Boolean] = {
    Future {
      NamedDB(Symbol("validation")).readOnly { implicit session =>
        sql"select user_id from active_users where user_id = $userId".
          map(_.string("user_id")).headOption().apply().isDefined
      }
    }
  }

  private def validateUserDeactivated(userId: UUID): Future[Option[String]] = {
    val isActivatedT = isActivated(userId)
    isActivatedT.transform {
      case Success(true) => Success(None)
      case Failure(_) => Success(Some("Validation state exception"))
      case Success(false) => Success(Some("The user is already deactivated!"))
    }
  }

  private def updateUserDeactivated(userId: UUID): Future[Option[String]] = {
    invokeUpdate{
      NamedDB(Symbol("validation")).localTx { implicit session =>
        sql"delete from active_users where user_id = $userId".
          update().apply()
      }
    }
  }

  private def validateQuestionDeleted(questionId: UUID, userId: UUID):
  Future[Option[String]] = {
    validateUser(userId) {
      val maybeQuestionOwnerT = Future {
        NamedDB(Symbol("validation")).readOnly { implicit session =>
          sql"select user_id from question_user where question_id = $questionId".
            map(_.string("user_id")).headOption().apply()
        }
      }
      maybeQuestionOwnerT.transform {
        case Success(None) => Success(Some("The question doesn't exist!"))
        case Success(Some(questionOwner)) =>
          if (questionOwner != userId.toString) {
            Success(Some("This user has no rights to delete this question!"))
          } else {
            Success(None)
          }
        case _ => Success(Some("Validation state exception!"))
      }
    }
  }

  private def updateQuestionDeleted(id: UUID): Future[Option[String]] = {
    invokeUpdate {
      NamedDB(Symbol("validation")).localTx { implicit session =>
        sql"delete from question_user where question_id = $id".update().apply()
      }
    }
  }

  private def validateQuestionCreated(questionId: UUID, userId: UUID, tags: Seq[UUID]): Future[Option[String]] = {
    validateUser(userId) {
      val existingTagsT = Future {
        NamedDB(Symbol("validation")).localTx { implicit session =>
          implicit val binderFactory: ParameterBinderFactory[UUID] = ParameterBinderFactory {
            value => (stmt, idx) => stmt.setObject(idx, value)
          }
          val tagIdsSql = SQLSyntax.in(sqls"tag_id", tags)
          sql"select * from tags where $tagIdsSql".map(_.string("tag_id")).list().apply().length
        }
      }
      existingTagsT.transform {
        case Failure(th) => Success(Some("Validation state exception!"))
        case Success(num) if num == tags.length => Success(None)
        case _ => Success(Some("Some tags referenced by the question do not exist!"))
      }
    }
  }

  private def updateAnswerCreated(answerId: UUID, userId: UUID,
      questionId: UUID): Future[Option[String]] = {
    invokeUpdate {
      NamedDB(Symbol("validation")).localTx { implicit session =>
        sql"""insert into answer_user(answer_id, user_id)
             values(${answerId}, ${userId})""".update().apply()
        sql"""insert into question_answer(question_id, answer_id)
             values(${questionId}, ${answerId})""".update().apply()
      }
    }
  }

  private def validateAnswerCreated(answerId: UUID, userId: UUID,
      questionId: UUID): Future[Option[String]] = {
    validateUser(userId) {
      val resultT = Future {
        NamedDB(Symbol("validation")).readOnly { implicit session =>
          val questionExists =
            sql"select * from question_user where question_id = ${questionId}".
              map(_.string("question_id")).headOption().apply().isDefined
          val answerExists =
            sql"select * from answer_user where answer_id = ${answerId}".
              map(_.string("answer_id")).headOption().apply().isDefined
          val alreadyWritten =
            sql"""select user_id from answer_user au inner join
               question_answer qa on au.answer_id = qa.answer_id
               where question_id = ${questionId} and user_id = ${userId}""".
              map(_.string("user_id")).headOption().apply().isDefined
          (questionExists, answerExists, alreadyWritten)
        }
      }

      resultT.transform {
        case Success((false, _, _)) => Success(Some("This question doesn't exist!"))
        case Success((_, _, true)) =>
          Success(Some("Users can only give one answer to the question!"))
        case Success((_, true, _)) => Success(Some("This answer already exists!"))
        case Success((true, _, _)) => Success(None)
        case Failure(_) => Success(Some("Validation state exception!"))
      }
    }
  }

  private def validateAnswerDeleted(answerId: UUID, userId: UUID): Future[Option[String]] = {
    validateUser(userId) {
      val UserIdStr = userId.toString
      val maybeAnswerOwnerT = Future {
        NamedDB(Symbol("validation")).readOnly { implicit session =>
          sql"select user_id from answer_user where answer_id = ${answerId}".
            map(_.string("user_id")).headOption().apply()
        }
      }
      maybeAnswerOwnerT.transform {
        case Success(None) => Success(Some("The answer doesn't exists"))
        case Success(Some(UserIdStr)) => Success(None)
        case Success(Some(_)) => Success(Some("The answer was written by another user!"))
        case _ => Success(Some("Validation state exception!"))
      }
    }
  }

  private def updateAnswerDeleted(answerId: UUID): Future[Option[String]] = {
    invokeUpdate {
      NamedDB(Symbol("validation")).localTx { implicit session =>
        sql"delete from answer_user where answer_id = ${answerId}".update().apply()
      }
    }
  }

  private def validateAnswerUpdated(answerId: UUID, userId: UUID, questionId: UUID): Future[Option[String]] = {
    val UserIdStr = userId.toString
    val maybeAnswerOwnerT = Future {
      NamedDB(Symbol("validation")).readOnly { implicit session =>
        sql"select user_id from answer_user where answer_id = ${answerId}".
          map(_.string("user_id")).headOption().apply()
      }
    }
    maybeAnswerOwnerT.transform {
      case Success(None) => Success(Some("The answer doesn't exists"))
      case Success(Some(UserIdStr)) => Success(None)
      case Success(Some(_)) => Success(Some("The answer was written by another user!"))
      case _ => Success(Some("Validation state exception!"))
    }
  }

  private def updateAnswerUpdated(): Future[Option[String]] = Future.successful(None)

  private def validateAnswerUpvoted(answerId: UUID, userId: UUID, questionId: UUID): Future[Option[String]] = {
    val UserIdStr = userId.toString
    val resultT = Future {
      NamedDB(Symbol("validation")).readOnly { implicit session =>
        val questionExists =
          sql"select * from question_user where question_id = ${questionId}".
            map(_.string("question_id")).headOption().apply().isDefined
        val answerAuthor =
          sql"select user_id from answer_user where answer_id = ${answerId}".
            map(_.string("user_id")).headOption().apply()
        val alreadyUpvoted =
          sql"""select upvoted_by_user_id from answer_upvoter where answer_id = ${answerId} and upvoted_by_user_id = ${userId}""".
            map(_.string("upvoted_by_user_id")).headOption().apply().isDefined
        (questionExists, answerAuthor, alreadyUpvoted)
      }
    }
    resultT.transform {
      case Success((false, _, _)) => Success(Some("This question doesn't exist!"))
      case Success((_, None, _)) => Success(Some("This answer doesn't exist!"))
      case Success((_, Some(UserIdStr), _)) => Success(Some("Users cannot like their own answers!"))
      case Success((_, Some(_), true)) => Success(Some("Users cannot like answers more than once!"))
      case Success((_, Some(_), false)) => Success(None)
      case _ => Success(Some("Validation state exception!"))
    }
  }

  private def updateAnswerUpvoted(answerId: UUID, userId: UUID): Future[Option[String]] = {
    invokeUpdate {
      NamedDB(Symbol("validation")).localTx { implicit session =>
        sql"insert into answer_upvoter(answer_id, upvoted_by_user_id) values(${answerId}, ${userId})".update().apply()
      }
    }
  }

  private def validateAnswerDownvoted(answerId: UUID, userId: UUID, questionId: UUID): Future[Option[String]] = {
    val resultT = Future {
      NamedDB(Symbol("validation")).readOnly { implicit session =>
        val questionExists =
          sql"select * from question_user where question_id = ${questionId}".
            map(_.string("question_id")).headOption().apply().isDefined
        val alreadyUpvoted =
          sql"""select upvoted_by_user_id from answer_upvoter where answer_id = ${answerId} and upvoted_by_user_id = ${userId}""".
            map(_.string("upvoted_by_user_id")).headOption().apply().isDefined
        (questionExists, alreadyUpvoted)
      }
    }
    resultT.transform {
      case Success((false, _)) => Success(Some("This question doesn't exist!"))
      case Success((_, true)) => Success(None)
      case Success((_, false)) => Success(Some("Users cannot downvote what they haven't upvoted"))
      case _ => Success(Some("Validation state exception!"))
    }
  }

  private def updateAnswerDownvoted(answerId: UUID, userId: UUID): Future[Option[String]] = {
    invokeUpdate {
      NamedDB(Symbol("validation")).localTx { implicit session =>
        sql"delete from answer_upvoter where answer_id = ${answerId} and upvoted_by_user_id = ${userId}".update().apply()
      }
    }
  }

  private def updateQuestionCreated(questionId: UUID, userId: UUID, tags: Seq[UUID]): Future[Option[String]] = {
    invokeUpdate {
      NamedDB(Symbol("validation")).localTx { implicit session =>
        sql"insert into question_user(question_id, user_id) values(${questionId}, ${userId})".update().apply()
        tags.foreach { tagId =>
          sql"insert into tag_question(tag_id, question_id) values(${tagId}, ${questionId})".update().apply()
        }
      }
    }
  }

  private def invokeUpdate(block : => Any): Future[Option[String]] = {
    val result = Future { block }
    result.transform {
      case Success(_) => Success(None)
      case Failure(th) => Success(Some("Validation state exception!"))
    }
  }

  private def resetState(fromScratch: Boolean): Future[Option[String]] = {
    if (!fromScratch) Future.successful(None)
    else invokeUpdate {
      NamedDB(Symbol("validation")).localTx { implicit session =>
        sql"delete from tags where 1 > 0".update().apply()
        sql"delete from active_users where 1 > 0".update().apply()
        sql"delete from question_user where 1 > 0".update().apply()
        sql"delete from tag_question where 1 > 0".update().apply()
        sql"delete from answer_user where 1 > 0".update().apply()
        sql"delete from question_answer where 1 > 0".update().apply()
        sql"delete from answer_upvoter where 1 > 0".update().apply()
      }
    }
  }

  private def validateAndUpdate(skipValidation: Boolean)
     (validateBlock: => Future[Option[String]])
     (updateBlock: => Future[Option[String]]): Future[Option[String]] = {
    if (skipValidation) {
      updateBlock
    } else {
      val validationResult = validateBlock
      validationResult.transformWith {
        case Success(None) => updateBlock
        case _ => validationResult
      }
    }
  }

  private def processEvents(events: Seq[LogRecord], skipValidation: Boolean): Future[Option[String]] = {
    log.info(s"Processing ${events.size} events, skip validation: $skipValidation")
    val result = Source.apply(events).foldAsync(Option.empty[String]) { (previousResult, nextEvent) =>
      previousResult match {
        case None => processSingleEvent(nextEvent, skipValidation)
        case _ => Future.successful(previousResult)
      }
    }.runWith(Sink.last)
    result
  }

  // Not used
  private def processEventsNonTailRec(events: Seq[LogRecord], skipValidation: Boolean): Future[Option[String]] = {
    def processEventsRec(xs: List[LogRecord], previous: Future[Option[String]]): Future[Option[String]] = {
      xs match {
        case Nil => previous
        case ::(event, rest) =>
          previous.transformWith {
            case Failure(_) => Future.successful(Some("Exception occurred"))
            case Success(None) =>
              val result = processSingleEvent(event, skipValidation)
              processEventsRec(rest, result)
            case Success(error) => Future.successful(error)
          }
      }
    }
    processEventsRec(events.toList, Future.successful(None))
  }

  private def processSingleEvent(event: LogRecord,
      skipValidation: Boolean): Future[Option[String]] = {
    event.action match {
      case UserActivated.actionName =>
        val decoded = event.data.as[UserActivated]
        validateAndUpdate(skipValidation) {
          validateUserActivated(decoded.id)
        } { updateUserActivated(decoded.id) }
      case UserDeactivated.actionName =>
        val decoded = event.data.as[UserDeactivated]
        validateAndUpdate(skipValidation) {
          validateUserDeactivated(decoded.id)
        } { updateUserDeactivated(decoded.id) }
      case TagCreated.actionName =>
        val decoded = event.data.as[TagCreated]
        validateAndUpdate(skipValidation) {
          validateTagCreated(decoded.text, decoded.createdBy)
        } { updateTagCreated(decoded.id, decoded.text) }
      case TagDeleted.actionName =>
        val decoded = event.data.as[TagDeleted]
        validateAndUpdate(skipValidation) {
          validateTagDeleted(decoded.id, decoded.deletedBy)
        } { updateTagDeleted(decoded.id) }
      case QuestionCreated.actionName =>
        val decoded = event.data.as[QuestionCreated]
        validateAndUpdate(skipValidation) {
          validateQuestionCreated(decoded.questionId,
            decoded.createdBy, decoded.tags)
        } { updateQuestionCreated(decoded.questionId,
          decoded.createdBy, decoded.tags) }
      case QuestionDeleted.actionName =>
        val decoded = event.data.as[QuestionDeleted]
        validateAndUpdate(skipValidation) {
          validateQuestionDeleted(decoded.questionId, decoded.deletedBy)
        } { updateQuestionDeleted(decoded.questionId) }
      case AnswerCreated.actionName =>
        val decoded = event.data.as[AnswerCreated]
        validateAndUpdate(skipValidation) {
          validateAnswerCreated(decoded.answerId,
            decoded.createdBy, decoded.questionId)
        } { updateAnswerCreated(decoded.answerId,
          decoded.createdBy, decoded.questionId) }
      case AnswerDeleted.actionName =>
        val decoded = event.data.as[AnswerDeleted]
        validateAndUpdate(skipValidation) {
          validateAnswerDeleted(decoded.answerId,
            decoded.deletedBy)
        } { updateAnswerDeleted(decoded.answerId) }
      case AnswerUpdated.actionName =>
        val decoded = event.data.as[AnswerUpdated]
        validateAndUpdate(skipValidation) {
          validateAnswerUpdated(decoded.answerId,
            decoded.updatedBy, decoded.questionId)
        } { updateAnswerUpdated() }
      case AnswerUpvoted.actionName =>
        val decoded = event.data.as[AnswerUpvoted]
        validateAndUpdate(skipValidation) {
          validateAnswerUpvoted(decoded.answerId,
            decoded.userId, decoded.questionId)
        } { updateAnswerUpvoted(decoded.answerId,
          decoded.userId) }
      case AnswerDownvoted.actionName =>
        val decoded = event.data.as[AnswerDownvoted]
        validateAndUpdate(skipValidation) {
          validateAnswerDownvoted(decoded.answerId,
            decoded.userId, decoded.questionId)
        } { updateAnswerDownvoted(decoded.answerId,
          decoded.userId) }
      case _ => Future.successful(Some("Unknown event"))
    }
  }
}
