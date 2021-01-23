package services

import akka.stream.Materializer
import dao.{LogDao, Neo4JReadDao}
import model.ServerSentMessage
import play.api.Logger
import play.api.libs.json.JsBoolean

import java.time.ZonedDateTime
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Created by denis on 12/12/16.
  */
class RewindService(validationService: ValidationService,
                    readDao: Neo4JReadDao, logDao: LogDao,
                    clientBroadcastService: ClientBroadcastService)
                   (implicit mat: Materializer) {
  private val log = Logger(this.getClass)

  private def message2Future(errorMessage: Option[String]): Future[Unit] = {
    errorMessage match {
      case None => Future.successful(())
      case Some(msg) => Future.failed(new Exception(msg))
    }
  }

  import util.ThreadPools.CPU

  private def recreateState(upTo: ZonedDateTime): Future[Unit] = {
    // Resetting the state
    val readResetF = readDao.refreshState(Nil, fromScratch = true)
    val validationResetF = validationService.refreshState(Nil, fromScratch = true)
    val resetF = for {
      _ <- readResetF
      _ <- validationResetF
    } yield ()

    resetF.flatMap { _ =>
      val bufferSize = 20
      logDao.getLogRecordStream(upTo).flatMap { eventSource =>
        eventSource.grouped(bufferSize).mapAsync(parallelism = 1) { events =>
          for {
            _ <- readDao.refreshState(events, fromScratch = false)
            _ <- validationService.refreshState(events, fromScratch = false).map(message2Future)
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
