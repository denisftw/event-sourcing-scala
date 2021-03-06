package dao

import java.util.UUID
import com.appliedscala.events.LogRecord
import com.appliedscala.events.answer.{AnswerCreated, AnswerDeleted, AnswerDownvoted, AnswerUpdated, AnswerUpvoted}
import com.appliedscala.events.question.{QuestionCreated, QuestionDeleted}
import com.appliedscala.events.tag.{TagCreated, TagDeleted}
import com.appliedscala.events.user.{UserActivated, UserDeactivated}
import model.{Answer, Question, QuestionThread, Tag}

import java.time.ZonedDateTime
import org.neo4j.driver.Record
import services.{Neo4JQuery, Neo4JQueryExecutor, Neo4JUpdate}

import scala.concurrent.Future

/**
  * Created by denis on 11/16/16.
  */
class Neo4JReadDao(queryExecutor: Neo4JQueryExecutor) {

  import util.ThreadPools.CPU
  private def clear(fromScratch: Boolean): Future[Unit] = {
    if (fromScratch) {
      val update = "MATCH (all) DETACH DELETE all"
      queryExecutor.executeUpdate(Neo4JQuery.simple(update)).map(_ => ())
    } else Future.successful(())
  }

  private def rebuildState(events: Seq[LogRecord]): Future[Unit] = {
    val updates = events.flatMap { event =>
      prepareUpdates(event).queries
    }
    queryExecutor.executeBatch(updates)
  }

  def refreshState(events: Seq[LogRecord], fromScratch: Boolean): Future[Unit] = {
    for {
      _ <- clear(fromScratch)
      _ <- rebuildState(events)
    } yield ()
  }

  def processEvent(event: LogRecord): Future[Unit] = {
    val update = prepareUpdates(event)
    queryExecutor.executeSequentially(update)
  }

  def handleEventWithUpdate(logRecord: LogRecord)(updateBlock: Option[UUID] => Unit): Future[Unit] = {
    val update = prepareUpdates(logRecord)
    queryExecutor.executeSequentially(update).map { _ =>
      updateBlock(update.updateId)
    }
  }

  private def prepareUpdates(record: LogRecord): Neo4JUpdate = {
    record.action match {
      case UserActivated.actionName =>
        val decoded = record.data.as[UserActivated]
        Neo4JUpdate(activateUser(decoded.id))
      case UserDeactivated.actionName =>
        val decoded = record.data.as[UserDeactivated]
        Neo4JUpdate(deactivateUser(decoded.id))
      case TagCreated.actionName =>
        val decoded = record.data.as[TagCreated]
        Neo4JUpdate(createTag(decoded.id, decoded.text))
      case TagDeleted.actionName =>
        val decoded = record.data.as[TagDeleted]
        Neo4JUpdate(deleteTag(decoded.id))
      case QuestionCreated.actionName =>
        val decoded = record.data.as[QuestionCreated]
        Neo4JUpdate(createQuestion(decoded.questionId, decoded.title,
          decoded.details, decoded.createdBy, decoded.tags, decoded.created))
      case QuestionDeleted.actionName =>
        val decoded = record.data.as[QuestionDeleted]
        Neo4JUpdate(deleteQuestion(decoded.questionId))
      case AnswerCreated.actionName =>
        val decoded = record.data.as[AnswerCreated]
        Neo4JUpdate(createAnswer(decoded.answerId, decoded.questionId,
          decoded.createdBy, decoded.answerText, decoded.created),
          updateId = Some(decoded.questionId))
      case AnswerUpdated.actionName =>
        val decoded = record.data.as[AnswerUpdated]
        Neo4JUpdate(updateAnswer(decoded.answerId,
          decoded.answerText, decoded.updated),
          updateId = Some(decoded.questionId))
      case AnswerDeleted.actionName =>
        val decoded = record.data.as[AnswerDeleted]
        Neo4JUpdate(deleteAnswer(decoded.answerId),
          updateId = Some(decoded.questionId))
      case AnswerUpvoted.actionName =>
        val decoded = record.data.as[AnswerUpvoted]
        Neo4JUpdate(upvoteAnswer(decoded.answerId, decoded.userId),
          updateId = Some(decoded.questionId))
      case AnswerDownvoted.actionName =>
        val decoded = record.data.as[AnswerDownvoted]
        Neo4JUpdate(downvoteAnswer(decoded.answerId, decoded.userId),
          updateId = Some(decoded.questionId))
      case _ => Neo4JUpdate(Nil)
    }
  }

  def getAllTags: Future[Seq[Tag]] = {
    val query =
      """MATCH (t: Tag) RETURN t.tagId as tagId,
       t.tagText as tagText ORDER BY tagText"""
    val recordsF = queryExecutor.executeQuery(Neo4JQuery.simple(query))
    recordsF.map { records =>
      records.map { record =>
        val id = record.get("tagId").asString()
        val text = record.get("tagText").asString()
        Tag(UUID.fromString(id), text)
      }
    }
  }

  def getQuestion(questionId: UUID): Future[Option[Question]] = {
    val queryQuestion = """MATCH (u:User)-[:WROTE]->(q:Question
         {id: $questionId })-[:BELONGS]->(t: Tag)
         RETURN q.title as title, q.details as details, q.id as question_id,
         q.created as created, u.id as user_id, collect(distinct t) as tags"""
    val questionRecordsF = queryExecutor.executeQuery(Neo4JQuery(queryQuestion,
      Map("questionId" -> questionId.toString)))
    val questionF = questionRecordsF.map { records =>
      records.headOption.map(recordToQuestion)
    }
    questionF
  }

  def getQuestionThread(questionId: UUID): Future[Option[QuestionThread]] = {
    for {
      maybeQuestion <- getQuestion(questionId)
      answers <- getAnswers(questionId)
    } yield {
      maybeQuestion.map { question => QuestionThread(question, answers) }
    }
  }

  def getAnswers(questionId: UUID): Future[Seq[Answer]] = {
    val queryAnswer =
      """MATCH (a:Answer)-[:ANSWERS]->(Question {id: $questionId })
         OPTIONAL MATCH (u:User)-[:UPVOTES]->(a)
         RETURN collect(distinct u.id) as upvotes,
         a.answerText as answerText, a.id as id,
         a.authorId as authorId, a.updated as updated"""
    val answerRecordsF = queryExecutor.executeQuery(Neo4JQuery(queryAnswer,
      Map("questionId" -> questionId.toString)))
    val answersF = answerRecordsF.map { records =>
      records.map { record => recordToAnswer(questionId, record) }
    }
    answersF
  }

  private def recordToAnswer(questionId: UUID, record: Record): Answer = {
    val answerText = record.get("answerText").asString()
    val answerId = UUID.fromString(record.get("id").asString())
    val updated = record.get("updated").asZonedDateTime()
    val answerAuthorId = UUID.fromString(record.get("authorId").asString())
    val upvotesIndices = 0.until(record.get("upvotes").size()).toList
    val upvotesSeq = upvotesIndices.map { index =>
      UUID.fromString(record.get("upvotes").get(index).asString())
    }
    Answer(answerId, questionId, answerText,
      answerAuthorId, None, upvotesSeq, updated)
  }

  private def recordToQuestion(record: Record): Question = {
    val detailsStr = record.get("details").asString()
    val details = if (detailsStr.isEmpty) None else Some(detailsStr)
    val title = record.get("title").asString()
    val created = record.get("created").asZonedDateTime()
    val questionId = UUID.fromString(record.get("question_id").asString())
    val authorId = UUID.fromString(record.get("user_id").asString())
    val tagsSize = record.get("tags").size()
    val tagsIndices = 0.until(tagsSize).toList
    val tags = tagsIndices.map { index =>
      val values = record.get("tags").get(index).asMap()
      val code = UUID.fromString(values.get("tagId").asInstanceOf[String])
      val text = values.get("tagText").asInstanceOf[String]
      Tag(code, text)
    }
    val question = Question(questionId, title, details,
      tags, created, authorId, None)
    question
  }

  def getQuestions: Future[Seq[Question]] = {
    val query =
      """
         MATCH (u:User)-[:WROTE]->(q:Question)-[:BELONGS]->(t: Tag)
         RETURN q.title as title, q.details as details, q.id as question_id,
         q.created as created, u.id as user_id, collect(t) as tags"""
    val recordsF = queryExecutor.executeQuery(Neo4JQuery.simple(query))
    val result = recordsF.map { records =>
      records.map(recordToQuestion)
    }
    result
  }

  private def deleteQuestion(questionId: UUID): Seq[Neo4JQuery] = {
    val update = """MATCH (q:Question { id: $questionId }) DETACH DELETE q"""
    Seq(Neo4JQuery(update, Map("questionId" -> questionId.toString)))
  }

  private def createQuestionUserQuery(questionId: UUID, title: String,
      details: Option[String], addedBy: UUID, created: ZonedDateTime):
  Neo4JQuery = {
    val detailsPart = details.getOrElse("")
    val createQuestion =
      """MATCH (u:User { id: $userId } )
        CREATE (q: Question { title: $title, id: $questionId,
         created: $created, details: $details } ),
         (q)-[wb:WRITTEN]->(u)-[w:WROTE]->(q)"""
    Neo4JQuery(createQuestion,
      Map("userId" -> addedBy.toString, "title" -> title,
        "questionId" -> questionId.toString,
        "created" -> created, "details" -> detailsPart))
  }

  private def createQuestionTagQuery(questionId: UUID,
      tagIds: Seq[UUID]): Neo4JQuery = {
    val tags = tagIds.map { id => id.toString }.toArray
    val updateTagRelationships =
      """MATCH (t:Tag),(q:Question { id: $questionId })
         WHERE t.tagId IN $tags
         CREATE (q)-[b:BELONGS]->(t)-[c:CONTAINS]->(q)"""
    Neo4JQuery(updateTagRelationships,
      Map("questionId" -> questionId.toString, "tags" -> tags))
  }

  private def createQuestion(questionId: UUID, title: String,
       details: Option[String], addedBy: UUID, tagIds: Seq[UUID],
       created: ZonedDateTime): Seq[Neo4JQuery] = {
    val userQuery = createQuestionUserQuery(questionId, title, details,
      addedBy, created)
    val tagQuery = createQuestionTagQuery(questionId, tagIds)
    Seq(userQuery, tagQuery)
  }

  private def createTag(tagId: UUID, tagText: String): Seq[Neo4JQuery] = {
    val update = """CREATE (:Tag { tagId: $tagId, tagText: $tagText })"""
    Seq(Neo4JQuery(update, Map(
      "tagId" -> tagId.toString, "tagText" -> tagText)))
  }

  private def deleteTag(tagId: UUID): Seq[Neo4JQuery] = {
    val update = """MATCH (t:Tag { tagId: $tagId }) DETACH DELETE t"""
    Seq(Neo4JQuery(update, Map("tagId" -> tagId.toString)))
  }

  private def activateUser(userId: UUID): Seq[Neo4JQuery] = {
    val update = """CREATE (:User { id: $userId })"""
    Seq(Neo4JQuery(update, Map("userId" -> userId.toString)))
  }

  private def deactivateUser(userId: UUID): Seq[Neo4JQuery] = {
    val update = """MATCH (u:User { id: $userId }) DETACH DELETE u"""
    Seq(Neo4JQuery(update, Map("userId" -> userId.toString)))
  }

  private def createAnswer(answerId: UUID, questionId: UUID, createdBy: UUID,
      answerText: String, created: ZonedDateTime): Seq[Neo4JQuery] = {
    val createAnswer =
      """
        MATCH (u:User { id: $authorId } )
        CREATE (a: Answer { answerText: $answerText, id: $answerId,
         updated: $updated, authorId: $authorId } ),
         (a)-[wb:ANSWERED_BY]->(u)-[w:WROTE_ANSWER]->(a)
      """
    val createAnswerInfo = Neo4JQuery(createAnswer, Map("answerText" -> answerText,
      "answerId" -> answerId.toString, "updated" -> created, "authorId" -> createdBy.toString))
    val linkToQuestion =
      """
        MATCH (a:Answer { id: $answerId }), (q:Question { id: $questionId })
        CREATE (q)-[ha:ANSWERED_IN]->(a)-[an:ANSWERS]->(q)
      """
    Seq(createAnswerInfo, Neo4JQuery(linkToQuestion, Map("answerId" -> answerId.toString,
      "questionId" -> questionId.toString)))
  }

  private def updateAnswer(answerId: UUID, updatedText: String,
     updated: ZonedDateTime): Seq[Neo4JQuery] = {
    val update =
      """
         MATCH (a:Answer { id: $answerId })
         SET a.answerText = $answerText, a.updated = $updated
      """
    Seq(Neo4JQuery(update, Map("answerId" -> answerId.toString,
      "answerText" -> updatedText, "updated" -> updated)))
  }

  private def deleteAnswer(answerId: UUID): Seq[Neo4JQuery] = {
    val update = """MATCH (a:Answer { id: $answerId }) DETACH DELETE a"""
    Seq(Neo4JQuery(update, Map("answerId" -> answerId.toString)))
  }

  private def downvoteAnswer(answerId: UUID, userId: UUID): Seq[Neo4JQuery] = {
    val update = """MATCH (a:Answer { id: $answerId })-[r:UPVOTES|IS_UPVOTED]-(
         u:User { id: $userId } ) DELETE r"""
    Seq(Neo4JQuery(update, Map(
      "answerId" -> answerId.toString, "userId" -> userId.toString)))
  }

  private def upvoteAnswer(answerId: UUID, userId: UUID): Seq[Neo4JQuery] = {
    val update =
      """MATCH (a:Answer { id: $answerId }), (u:User
        { id: $userId }) CREATE (u)-[ha:UPVOTES]->(a)-[il:IS_UPVOTED]->(u)"""
    Seq(Neo4JQuery(update, Map(
      "answerId" -> answerId.toString, "userId" -> userId.toString)))
  }
}
