package services

import java.util.UUID

import actors.InMemoryReadActor
import akka.actor.ActorSystem
import com.appliedscala.events.tag.{TagCreated, TagDeleted}
import com.appliedscala.events.{EventData, LogRecord}
import dao.LogDao
import model.Tag
import java.time.ZonedDateTime

import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Created by denis on 11/28/16.
  */
class TagEventProducer(actorSystem: ActorSystem,
    logDao: LogDao, readService: ReadService) {

  def createTag(text: String, createdBy: UUID): Future[Seq[Tag]] = {
    val tagId = UUID.randomUUID()
    val event = TagCreated(tagId, text, createdBy)
    val record = createLogRecord(event)
    logDao.insertLogRecord(record) match {
      case Failure(th) => Future.failed(th)
      case Success(_) => adjustReadState(record)
    }
  }

  def deleteTag(tagId: UUID, deletedBy: UUID): Future[Seq[Tag]] = {
    val event = TagDeleted(tagId, deletedBy)
    val record = createLogRecord(event)
    logDao.insertLogRecord(record) match {
      case Failure(th) => Future.failed(th)
      case Success(_) => adjustReadState(record)
    }
  }

  private def createLogRecord(eventData: EventData): LogRecord = {
    LogRecord(UUID.randomUUID(), eventData.action,
      eventData.json, ZonedDateTime.now())
  }

  import java.util.concurrent.TimeUnit
  import akka.pattern.ask
  import akka.util.Timeout
  import scala.concurrent.ExecutionContext.Implicits.global
  private def adjustReadState(logRecord: LogRecord): Future[Seq[Tag]] = {
    implicit val timeout = Timeout.apply(5, TimeUnit.SECONDS)
    val actor = actorSystem.actorSelection(InMemoryReadActor.path)
    (actor ? InMemoryReadActor.ProcessEvent(logRecord)).flatMap { _ =>
      readService.getTags
    }
  }
}