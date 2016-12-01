package dao

import java.util.UUID

import com.appliedscala.events.LogRecord
import com.appliedscala.events.tag.{TagCreated, TagDeleted}
import model.Tag

class InMemoryReadDao(records: Seq[LogRecord]) {
  import scala.collection.mutable.{Map => MMap}
  val tags = MMap.empty[UUID, Tag]

  def init(): Unit = records.foreach(processEvent)

  def processEvent(record: LogRecord): Unit = {
    record.action match {
      case TagCreated.actionName =>
        val event = record.data.as[TagCreated]
        tags += (event.id -> Tag(event.id, event.text))
      case TagDeleted.actionName =>
        val event = record.data.as[TagDeleted]
        tags -= event.id
      case _ => ()
    }
  }

  def getTags: Seq[Tag] = {
    tags.values.toList.sortWith(_.text < _.text)
  }
}
