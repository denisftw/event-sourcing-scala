package actors

import akka.actor.{Actor, Props}
import com.appliedscala.events.LogRecord
import dao.InMemoryReadDao

/**
  * Created by denis on 11/28/16.
  */
class InMemoryReadActor(logRecords: Seq[LogRecord])
  extends Actor {
  import InMemoryReadActor._

  val readDao = new InMemoryReadDao(logRecords)

  override def receive: Receive = {
    case InitializeState => readDao.init()
    case GetTags => sender() ! readDao.getTags
    case ProcessEvent(event) => sender() ! readDao.processEvent(event)
  }
}

object InMemoryReadActor {
  case class ProcessEvent(event: LogRecord)
  case object InitializeState
  case object GetTags

  val name = "in-memory-read-actor"
  val path = s"/user/$name"
  def props(logRecords: Seq[LogRecord]) =
    Props(new InMemoryReadActor(logRecords))
}
