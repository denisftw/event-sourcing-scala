package services

import java.util.UUID

import akka.actor.ActorSystem
import com.appliedscala.events.LogRecord
import com.appliedscala.events.question.{QuestionCreated, QuestionDeleted}

import play.api.Configuration
import util.{BaseTypes, EventValidator, ServiceKafkaProducer}

import scala.concurrent.Future

/**
  * Created by denis on 12/14/16.
  */
class QuestionEventProducer(actorSystem: ActorSystem, configuration: Configuration,
                       eventValidator: EventValidator) {

  val kafkaProducer = new ServiceKafkaProducer("questions",
    actorSystem, configuration)

  def createQuestion(title: String, details: Option[String], tags: Seq[UUID],
       createdBy: UUID): Future[Option[String]] = {
    val questionId = UUID.randomUUID()
    val created = BaseTypes.dateTimeNow
    val event = QuestionCreated(title, details, tags,
      questionId, createdBy, created)
    val record = LogRecord.fromEvent(event)
    eventValidator.validateAndSend(createdBy, record, kafkaProducer)
  }

  def deleteQuestion(questionId: UUID, deletedBy: UUID):
  Future[Option[String]] = {
    val event = QuestionDeleted(questionId, deletedBy)
    val record = LogRecord.fromEvent(event)
    eventValidator.validateAndSend(deletedBy, record, kafkaProducer)
  }
}