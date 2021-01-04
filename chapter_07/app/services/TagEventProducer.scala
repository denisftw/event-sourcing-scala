package services

import java.util.UUID
import com.appliedscala.events.tag.{TagCreated, TagDeleted}
import com.appliedscala.events.LogRecord
import messaging.IMessageProcessingRegistry
import util.EventValidator

import scala.concurrent.Future

/**
  * Created by denis on 11/28/16.
  */
class TagEventProducer(registry: IMessageProcessingRegistry,
                       eventValidator: EventValidator) {

  private val producer = registry.createProducer("tags")

  def createTag(text: String, createdBy: UUID): Future[Option[String]] = {
    val tagId = UUID.randomUUID()
    val event = TagCreated(tagId, text, createdBy)
    val record = LogRecord.fromEvent(event)
    eventValidator.validateAndSend(createdBy, record, producer)
  }

  def deleteTag(tagId: UUID, deletedBy: UUID): Future[Option[String]] = {
    val event = TagDeleted(tagId, deletedBy)
    val record = LogRecord.fromEvent(event)
    eventValidator.validateAndSend(deletedBy, record, producer)
  }
}