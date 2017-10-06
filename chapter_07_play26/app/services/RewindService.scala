package services

import actors.{EventStreamActor, ValidationActor, WSStreamActor}
import dao.{LogDao, Neo4JReadDao}
import model.ServerSentMessage
import java.time.ZonedDateTime
import play.api.libs.json.JsBoolean

import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}


/**
  * Created by denis on 12/12/16.
  */

import akka.actor.ActorSystem

class RewindService(actorSystem: ActorSystem,
                    validationService: ValidationService,
                    neo4JReadDao: Neo4JReadDao, logDao: LogDao) {

  private def message2Try(errorMessage: Option[String]): Try[Unit] = {
    errorMessage match {
      case None => Success(())
      case Some(msg) => Failure(new Exception(msg))
    }
  }

  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.duration._

  private def recreateState(upTo: Option[ZonedDateTime]): Try[Unit] = Try {
    val timeout = 5.seconds
    val bufferSize = 200

    // Resetting the state
    val readResetT = neo4JReadDao.refreshState(Nil, fromScratch = true)
    val validationResetF = validationService.refreshState(Nil,
      fromScratch = true)
    val validationResetT = message2Try(Await.result(validationResetF, timeout))
    val resetT = for {
      readReset <- readResetT; validationReset <- validationResetT
    } yield (readReset, validationReset)

    resetT match {
      case Success(_) => ()
      case Failure(th) => throw th
    }

    // Rewinding the state
    val rewindT = logDao.iterateLogRecords(upTo)(bufferSize) { events =>
      val readRefreshT = neo4JReadDao.refreshState(events,
        fromScratch = false)
      val validationRefreshF = validationService.refreshState(events,
        fromScratch = false)
      val validationRefreshT = message2Try(Await.result(
        validationRefreshF, timeout))

      val refreshT = for {
        readRefresh <- readRefreshT; validationRefresh <- validationRefreshT
      } yield (readRefresh, validationRefresh)

      refreshT match {
        case Success(_) => ()
        case Failure(th) => throw th
      }
    }

    rewindT match {
      case Success(_) => ()
      case Failure(th) => throw th
    }
  }

  import play.api.Logger

  def refreshState(upTo: Option[ZonedDateTime]): Try[Unit] = {
    val resultT = recreateState(upTo)
    resultT match {
      case Failure(th) =>
        Logger.error("Error occurred while rewinding the state", th);
      case Success(_) =>
        Logger.info("The state was successfully rebuild")
        val update = ServerSentMessage.create("stateRebuilt",
          JsBoolean.apply(true))
        val esActor = actorSystem.actorSelection(WSStreamActor.pathPattern)
        esActor ! WSStreamActor.DataUpdated(update.json)
    }
    resultT
  }
}
