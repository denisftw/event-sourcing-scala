package services

import java.util.UUID
import com.appliedscala.events.LogRecord
import com.appliedscala.events.question.{QuestionCreated, QuestionDeleted}

import java.time.ZonedDateTime
import util.{EventValidator, IMessageProcessingRegistry}

import scala.concurrent.Future

/**
  * Created by denis on 12/14/16.
  */
class QuestionEventProducer(registry: IMessageProcessingRegistry,
                            eventValidator: EventValidator) {

  private val producer = registry.createProducer("questions")

  def createQuestion(title: String, details: Option[String], tags: Seq[UUID],
       createdBy: UUID): Future[Option[String]] = {
    val questionId = UUID.randomUUID()
    val created = ZonedDateTime.now()
    val event = QuestionCreated(title, details, tags,
      questionId, createdBy, created)
    val record = LogRecord.fromEvent(event)
    eventValidator.validateAndSend(createdBy, record, producer)
  }

  def deleteQuestion(questionId: UUID, deletedBy: UUID):
  Future[Option[String]] = {
    val event = QuestionDeleted(questionId, deletedBy)
    val record = LogRecord.fromEvent(event)
    eventValidator.validateAndSend(deletedBy, record, producer)
  }
}