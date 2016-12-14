package services

import java.util.UUID

import actors.EventStreamActor
import akka.actor.ActorSystem
import com.appliedscala.events.tag.{TagCreated, TagDeleted}
import com.appliedscala.events.{EventData, LogRecord}
import org.joda.time.DateTime
import play.api.Configuration
import util.{EventValidator, ServiceKafkaProducer}

/**
  * Created by denis on 11/28/16.
  */
class TagEventProducer(actorSystem: ActorSystem, configuration: Configuration,
    eventValidator: EventValidator, validationService: ValidationService) {

  val kafkaProducer = new ServiceKafkaProducer("tags",
    actorSystem, configuration)


  def createTag(text: String, createdBy: UUID): Unit = {
    val tagId = UUID.randomUUID()
    val event = TagCreated(tagId, text, createdBy)
    val record = createLogRecord(event)
    eventValidator.validateAndSend(createdBy, record, kafkaProducer)
  }

  def deleteTag(tagId: UUID, deletedBy: UUID): Unit = {
    val event = TagDeleted(tagId, deletedBy)
    val record = createLogRecord(event)
    eventValidator.validateAndSend(deletedBy, record, kafkaProducer)
  }

  private def createLogRecord(eventData: EventData): LogRecord = {
    LogRecord(UUID.randomUUID(), eventData.action,
      eventData.json, DateTime.now())
  }


}