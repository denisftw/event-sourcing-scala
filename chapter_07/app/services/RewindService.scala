package services

import akka.stream.Materializer
import dao.{LogDao, Neo4JReadDao}
import play.api.Logger
import model.ServerSentMessage

import java.time.ZonedDateTime
import play.api.libs.json.JsBoolean

import scala.concurrent.Future
import scala.util.{Failure, Success}


/**
  * Created by denis on 12/12/16.
  */
import akka.actor.ActorSystem
class RewindService(clientBroadcastService: ClientBroadcastService,
                    validationService: ValidationService,
                    neo4JReadDao: Neo4JReadDao, logDao: LogDao)(implicit mat: Materializer) {

  val log = Logger(this.getClass)

  private def message2Future(errorMessage: Option[String]): Future[Unit] = {
    errorMessage match {
      case None => Future.successful(())
      case Some(msg) => Future.failed(new Exception(msg))
    }
  }

  import util.ThreadPools.CPU

  private def recreateState(upTo: ZonedDateTime): Future[Unit] = {
    // Resetting the state
    val readResetF = neo4JReadDao.refreshState(Nil, fromScratch = true)
    val validationResetF = validationService.refreshState(Nil, fromScratch = true)
    val resetF = for {
      _ <- readResetF
      _ <- validationResetF
    } yield ()

    val bufferSize = 200
    resetF.flatMap { _ =>
      logDao.getLogRecordStream(Some(upTo)).flatMap { eventSource =>
        eventSource.grouped(bufferSize).map { events =>
          for {
            _ <- neo4JReadDao.refreshState(events, fromScratch = false)
            _ <- validationService.refreshState(events, fromScratch = true).map(message2Future)
          } yield ()
        }.run().map(_ => ())
      }
    }
  }

  def refreshState(upTo: ZonedDateTime): Future[Unit] = {
    recreateState(upTo).andThen {
      case Failure(th) =>
        // TODO: [LOGBACK-1027] [NEO4JJAVA-773]
        log.error("Error occurred while rewinding the state", th);
      case Success(_) =>
        log.info("The state was successfully rebuild")
        val update = ServerSentMessage.create("stateRebuilt",
          JsBoolean.apply(true))
        clientBroadcastService.broadcastUpdate(update.json)
    }
  }
}
