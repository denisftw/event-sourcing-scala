package services

import actors.InMemoryReadActor
import akka.actor.ActorSystem

import dao.LogDao
import model.Tag
import play.api.Logger

import scala.util.{Failure, Success}

class ReadService(actorSystem: ActorSystem, logDao: LogDao) {

  init()

  private def init(): Unit = {
    val logRecordsT = logDao.getLogRecords
    logRecordsT match {
      case Failure(th) =>
        Logger.error("Error while initializing the read service", th)
        throw th
      case Success(logRecords) =>
        val actor = actorSystem.actorOf(
          InMemoryReadActor.props(logRecords), InMemoryReadActor.name)
        actor ! InMemoryReadActor.InitializeState
    }
  }

  import java.util.concurrent.TimeUnit
  import akka.util.Timeout
  import scala.concurrent.Future
  import akka.pattern.ask

  def getTags: Future[Seq[Tag]] = {
    implicit val timeout = Timeout.apply(5, TimeUnit.SECONDS)
    val actor = actorSystem.actorSelection(InMemoryReadActor.path)
    (actor ? InMemoryReadActor.GetTags).mapTo[Seq[Tag]]
  }
}
