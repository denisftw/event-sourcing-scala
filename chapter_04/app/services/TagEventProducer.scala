package services

import java.util.UUID

import akka.actor.ActorSystem
import com.appliedscala.events.tag.{TagCreated, TagDeleted}
import com.appliedscala.events.{EventData, LogRecord}
import org.joda.time.DateTime
import play.api.Configuration
import util.ServiceKafkaProducer

/**
  * Created by denis on 11/28/16.
  */
class TagEventProducer(actorSystem: ActorSystem,
    configuration: Configuration) {

  val kafkaProducer = new ServiceKafkaProducer("tags",
    actorSystem, configuration)

  def createTag(text: String, createdBy: UUID): Unit = {
    val tagId = UUID.randomUUID()
    val event = TagCreated(tagId, text, createdBy)
    val record = createLogRecord(event)
    kafkaProducer.send(record.encode)
  }

  def deleteTag(tagId: UUID, deletedBy: UUID): Unit = {
    val event = TagDeleted(tagId, deletedBy)
    val record = createLogRecord(event)
    kafkaProducer.send(record.encode)
  }

  private def createLogRecord(eventData: EventData): LogRecord = {
    LogRecord(UUID.randomUUID(), eventData.action,
      eventData.json, DateTime.now())
  }
}