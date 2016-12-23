package actors


import akka.actor.{Actor, Props}
import com.appliedscala.events.LogRecord
import java.util.UUID

import com.appliedscala.events.question.{QuestionCreated, QuestionDeleted}
import com.appliedscala.events.tag.{TagCreated, TagDeleted}
import com.appliedscala.events.user.{UserActivated, UserDeactivated}
import scalikejdbc._

import scala.util.{Failure, Success, Try}

/**
  * Created by denis on 12/5/16.
  */
class ValidationActor extends Actor {
  import ValidationActor._
  override def receive: Receive = {
    case ValidateEventRequest(event) => sender() !
      processSingleEvent(event, skipValidation = false)
    case RefreshStateCommand(events, fromScratch) => sender() ! {
      val resetResult = resetState(fromScratch)
      resetResult match {
        case Some(_) => resetResult
        case None => processEvents(events, skipValidation = true)
      }
    }
    case _ => sender() ! Some("Unknown message type!")
  }

  private def validateTagCreated(tagText: String, userId: UUID): Option[String] = {
    validateUser(userId) {
      val maybeExistingT = Try {
        NamedDB('validation).readOnly { implicit session =>
          sql"select tag_id from tags where tag_text = $tagText".
            map(_.string("tag_id")).headOption().apply()
        }
      }
      maybeExistingT match {
        case Success(Some(_)) => Some("The tag already exists!")
        case Success(None) => None
        case _ => Some("Validation state exception!")
      }
    }
  }

  private def updateTagCreated(tagId: UUID, tagText: String): Option[String] = {
    invokeUpdate{
      NamedDB('validation).localTx { implicit session =>
        sql"insert into tags(tag_id, tag_text) values($tagId, $tagText)".
          update().apply()
      }
    }
  }

  private def validateTagDeleted(tagId: UUID, userId: UUID): Option[String] = {
    validateUser(userId) {
      val maybeExistingTag = NamedDB('validation).readOnly { implicit session =>
        sql"select tag_id from tags tn where tag_id = ${tagId}".
          map(_.string("tag_id")).headOption().apply()
      }
      val maybeDependentQuestions = NamedDB('validation).readOnly { implicit session =>
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

  private def updateTagDeleted(tagId: UUID): Option[String] = {
    invokeUpdate {
      NamedDB('validation).localTx { implicit session =>
        sql"delete from tags where tag_id = ${tagId}".update().apply()
      }
    }
  }

  private def validateUserActivated(userId: UUID): Option[String] = {
    val isActivatedT = isActivated(userId)
    isActivatedT match {
      case Success(true) => Some("The user is already activated!")
      case Failure(_) => Some("Validation state exception")
      case Success(false) => None
    }
  }

  private def updateUserActivated(userId: UUID): Option[String] = {
    invokeUpdate{
      NamedDB('validation).localTx { implicit session =>
        sql"insert into active_users(user_id) values($userId)".
          update().apply()
      }
    }
  }

  private def validateUser(userId: UUID)(block: => Option[String]): Option[String] = {
    val isActivatedT = isActivated(userId)
    isActivatedT match {
      case Success(false) => Some("The user is not activated!")
      case Failure(_) => Some("Validation state exception!")
      case Success(true) => block
    }
  }

  private def isActivated(userId: UUID): Try[Boolean] = {
    Try {
      NamedDB('validation).readOnly { implicit session =>
        sql"select user_id from active_users where user_id = $userId".
          map(_.string("user_id")).headOption().apply().isDefined
      }
    }
  }

  private def validateUserDeactivated(userId: UUID): Option[String] = {
    val isActivatedT = isActivated(userId)
    isActivatedT match {
      case Success(true) => None
      case Failure(_) => Some("Validation state exception")
      case Success(false) => Some("The user is already deactivated!")
    }
  }

  private def updateUserDeactivated(userId: UUID): Option[String] = {
    invokeUpdate{
      NamedDB('validation).localTx { implicit session =>
        sql"delete from active_users where user_id = $userId".
          update().apply()
      }
    }
  }

  private def validateQuestionDeleted(questionId: UUID, userId: UUID):
      Option[String] = {
    validateUser(userId) {
      val maybeQuestionOwnerT = Try {
        NamedDB('validation).readOnly { implicit session =>
          sql"SELECT user_id FROM question_user WHERE question_id = $questionId".
            map(_.string("user_id")).headOption().apply()
        }
      }
      maybeQuestionOwnerT match {
        case Success(None) => Some("The question doesn't exist!")
        case Success(Some(questionOwner)) =>
          if (questionOwner != userId.toString) {
            Some("This user has no rights to delete this question!")
          } else {
            None
          }
        case _ => Some("Validation state exception!")
      }
    }
  }

  private def updateQuestionDeleted(id: UUID): Option[String] = {
    invokeUpdate {
      NamedDB('validation).localTx { implicit session =>
        sql"delete from question_user where question_id = $id".update().apply()
      }
    }
  }

  private def validateQuestionCreated(questionId: UUID, userId: UUID, tags: Seq[UUID]): Option[String] = {
    validateUser(userId) {
      val existingTagsT = Try {
        NamedDB('validation).localTx { implicit session =>
          implicit val binderFactory: ParameterBinderFactory[UUID] = ParameterBinderFactory {
            value => (stmt, idx) => stmt.setObject(idx, value)
          }
          val tagIdsSql = SQLSyntax.in(sqls"tag_id", tags)
          sql"select * from tags where $tagIdsSql".map(_.string("tag_id")).list().apply().length
        }
      }
      existingTagsT match {
        case Failure(th) => Some("Validation state exception!")
        case Success(num) if num == tags.length => None
        case _ => Some("Some tags referenced by the question do not exist!")
      }
    }
  }

  private def updateQuestionCreated(questionId: UUID, userId: UUID, tags: Seq[UUID]): Option[String] = {
    invokeUpdate {
      NamedDB('validation).localTx { implicit session =>
        sql"insert into question_user(question_id, user_id) values(${questionId}, ${userId})".update().apply()
        tags.foreach { tagId =>
          sql"insert into tag_question(tag_id, question_id) values(${tagId}, ${questionId})".update().apply()
        }
      }
    }
  }

  private def invokeUpdate(block : => Any): Option[String] = {
    val result = Try { block }
    result match {
      case Success(_) => None
      case Failure(th) => Some("Validation state exception!")
    }
  }

  private def resetState(fromScratch: Boolean): Option[String] = {
    if (!fromScratch) None
    else invokeUpdate {
      NamedDB('validation).localTx { implicit session =>
        sql"delete from tags where 1 > 0".update().apply()
        sql"delete from active_users where 1 > 0".update().apply()
        sql"delete from question_user where 1 > 0".update.apply()
        sql"delete from tag_question where 1 > 0".update().apply()
      }
    }
  }

  private def validateAndUpdate(skipValidation: Boolean)
     (validateBlock: => Option[String])
     (updateBlock: => Option[String]): Option[String] = {
    if (skipValidation) {
      updateBlock
    } else {
      val validationResult = validateBlock
      validationResult match {
        case None => updateBlock
        case _ => validationResult
      }
    }
  }

  private def processEvents(events: Seq[LogRecord],
      skipValidation: Boolean): Option[String] = {
    var lastResult: Option[String] = None
    import scala.util.control.Breaks._
    breakable {
      events.foreach { event =>
        lastResult match {
          case None => lastResult = processSingleEvent(event, skipValidation)
          case Some(_) => break()
        }
      }
    }
    lastResult
  }

  private def processSingleEvent(event: LogRecord,
      skipValidation: Boolean): Option[String] = {
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
      case _ => Some("Unknown event")
    }
  }
}

object ValidationActor {
  case class ValidateEventRequest(event: LogRecord)
  case class RefreshStateCommand(events: Seq[LogRecord],
      fromScratch: Boolean = true)

  val name = "validation-actor"
  val path = s"/user/$name"
  def props() = Props(new ValidationActor)
}
