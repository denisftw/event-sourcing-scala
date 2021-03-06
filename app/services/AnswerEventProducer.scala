package services

import java.util.UUID
import com.appliedscala.events.LogRecord
import com.appliedscala.events.answer._
import messaging.IMessageProcessingRegistry

import java.time.ZonedDateTime

import scala.concurrent.Future

/**
  * Created by denis on 12/23/16.
  */
class AnswerEventProducer(registry: IMessageProcessingRegistry, validationService: ValidationService) {

  private val producer = registry.createProducer("answers")

  def createAnswer(questionId: UUID, answerText: String,
                   createdBy: UUID): Future[Option[String]] = {
    val answerId = UUID.randomUUID()
    val created = ZonedDateTime.now()
    val event = AnswerCreated(answerId, answerText,
      questionId, createdBy, created)
    val record = LogRecord.fromEvent(event)
    validationService.validateAndSend(createdBy, record, producer)
  }

  def deleteAnswer(questionId: UUID, answerId: UUID,
         deletedBy: UUID): Future[Option[String]] = {
    val event = AnswerDeleted(answerId, questionId, deletedBy)
    val record = LogRecord.fromEvent(event)
    validationService.validateAndSend(deletedBy, record, producer)
  }

  def updateAnswer(questionId: UUID, answerId: UUID,
         updatedBy: UUID, answerText: String): Future[Option[String]] = {
    val updated = ZonedDateTime.now()
    val event = AnswerUpdated(answerId, answerText,
      questionId, updatedBy, updated)
    val record = LogRecord.fromEvent(event)
    validationService.validateAndSend(updatedBy, record, producer)
  }

  def upvoteAnswer(questionId: UUID, answerId: UUID,
         userId: UUID): Future[Option[String]] = {
    val event = AnswerUpvoted(answerId, questionId, userId)
    val record = LogRecord.fromEvent(event)
    validationService.validateAndSend(userId, record, producer)
  }

  def downvoteAnswer(questionId: UUID, answerId: UUID,
         userId: UUID): Future[Option[String]] = {
    val event = AnswerDownvoted(answerId, questionId, userId)
    val record = LogRecord.fromEvent(event)
    validationService.validateAndSend(userId, record, producer)
  }
}