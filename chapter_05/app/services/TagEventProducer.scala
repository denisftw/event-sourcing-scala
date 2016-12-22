package services

import java.util.UUID

import akka.actor.ActorSystem
import com.appliedscala.events.tag.{TagCreated, TagDeleted}
import com.appliedscala.events.LogRecord
import play.api.Configuration
import util.{EventValidator, ServiceKafkaProducer}

import scala.concurrent.Future

/**
  * Created by denis on 11/28/16.
  */
class TagEventProducer(actorSystem: ActorSystem, configuration: Configuration,
    eventValidator: EventValidator) {

  val kafkaProducer = new ServiceKafkaProducer("tags",
    actorSystem, configuration)


  def createTag(text: String, createdBy: UUID): Future[Option[String]] = {
    val tagId = UUID.randomUUID()
    val event = TagCreated(tagId, text, createdBy)
    val record = LogRecord.fromEvent(event)
    eventValidator.validateAndSend(createdBy, record, kafkaProducer)
  }

  def deleteTag(tagId: UUID, deletedBy: UUID): Future[Option[String]] = {
    val event = TagDeleted(tagId, deletedBy)
    val record = LogRecord.fromEvent(event)
    eventValidator.validateAndSend(deletedBy, record, kafkaProducer)
  }
}