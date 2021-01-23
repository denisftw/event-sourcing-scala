package dao

import akka.stream.scaladsl.{Sink, Source, SourceQueueWithComplete}
import akka.stream.{Materializer, OverflowStrategy}
import com.appliedscala.events.LogRecord
import com.appliedscala.events.tag.{TagCreated, TagDeleted}
import model.{ApplicationState, Tag}

import java.util.UUID
import scala.concurrent.{Future, Promise}

class InMemoryReadDao(implicit mat: Materializer) {
  import scala.collection.mutable.{Map => MMap}
  private object State {
    val tags = MMap.empty[UUID, Tag]
  }

  import util.ThreadPools.CPU
  def processEvents(events: Seq[LogRecord]): Future[Unit] = {
    Source(events).mapAsync(parallelism = 1) { event =>
      ReadQueue.offer(UpdateStateRequest(event))
    }.runWith(Sink.ignore).map(_ => ())
  }

  def processEvent(event: LogRecord): Future[Unit] = {
    ReadQueue.offer(UpdateStateRequest(event)).map(_ => ())
  }

  def getState: Future[ApplicationState] = {
    val promise = Promise[ApplicationState]()
    ReadQueue.offer(GetStateRequest(state => {
      promise.success(state)
    })).flatMap { _ =>
      promise.future
    }
  }

  @deprecated
  def processEventsNaive(events: Seq[LogRecord]): Unit = {
    events.foreach(updateState)
  }

  private val ReadQueue = {
    Source.queue[ReadServiceRequest](bufferSize = 100,
      OverflowStrategy.dropNew).map {
      case GetStateRequest(callback) =>
        callback.apply(getCurrentState)
      case UpdateStateRequest(event) =>
        updateState(event)
    }.to(Sink.ignore).run()
  }

  private def getCurrentState: ApplicationState = {
    val tags = State.tags.toSeq.map { case (_, tag) => tag }
    ApplicationState(tags)
  }

  private def updateState(record: LogRecord): Unit = {
    record.action match {
      case TagCreated.actionName =>
        val event = record.data.as[TagCreated]
        State.tags += (event.id -> Tag(event.id, event.text))
      case TagDeleted.actionName =>
        val event = record.data.as[TagDeleted]
        State.tags -= event.id
      case _ => ()
    }
  }


  sealed trait ReadServiceRequest
  case class GetStateRequest(callback: ApplicationState => Unit)
    extends ReadServiceRequest
  case class UpdateStateRequest(event: LogRecord) extends ReadServiceRequest
}
