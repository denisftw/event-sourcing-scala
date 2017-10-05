package services

import java.util.UUID

import akka.actor.ActorSystem
import com.appliedscala.events.LogRecord
import com.appliedscala.events.answer._

import play.api.Configuration
import util.{BaseTypes, EventValidator, ServiceKafkaProducer}

import scala.concurrent.Future

/**
  * Created by denis on 12/23/16.
  */
class AnswerEventProducer(actorSystem: ActorSystem, configuration: Configuration,
    eventValidator: EventValidator) {

  val kafkaProducer = new ServiceKafkaProducer("answers",
    actorSystem, configuration)

  def createAnswer(questionId: UUID, answerText: String,
                   createdBy: UUID): Future[Option[String]] = {
    val answerId = UUID.randomUUID()
    val created = BaseTypes.dateTimeNow
    val event = AnswerCreated(answerId, answerText,
      questionId, createdBy, created)
    val record = LogRecord.fromEvent(event)
    eventValidator.validateAndSend(createdBy, record, kafkaProducer)
  }

  def deleteAnswer(questionId: UUID, answerId: UUID,
         deletedBy: UUID): Future[Option[String]] = {
    val event = AnswerDeleted(answerId, questionId, deletedBy)
    val record = LogRecord.fromEvent(event)
    eventValidator.validateAndSend(deletedBy, record, kafkaProducer)
  }

  def updateAnswer(questionId: UUID, answerId: UUID,
         updatedBy: UUID, answerText: String): Future[Option[String]] = {
    val updated = BaseTypes.dateTimeNow
    val event = AnswerUpdated(answerId, answerText,
      questionId, updatedBy, updated)
    val record = LogRecord.fromEvent(event)
    eventValidator.validateAndSend(updatedBy, record, kafkaProducer)
  }

  def upvoteAnswer(questionId: UUID, answerId: UUID,
         userId: UUID): Future[Option[String]] = {
    val event = AnswerUpvoted(answerId, questionId, userId)
    val record = LogRecord.fromEvent(event)
    eventValidator.validateAndSend(userId, record, kafkaProducer)
  }

  def downvoteAnswer(questionId: UUID, answerId: UUID,
         userId: UUID): Future[Option[String]] = {
    val event = AnswerDownvoted(answerId, questionId, userId)
    val record = LogRecord.fromEvent(event)
    eventValidator.validateAndSend(userId, record, kafkaProducer)
  }
}