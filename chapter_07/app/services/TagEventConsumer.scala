package services

import akka.actor.ActorSystem
import com.appliedscala.events.LogRecord
import messaging.{IMessageConsumer, IMessageProcessingRegistry}
import model.ServerSentMessage
import play.api.Logger

import scala.concurrent.Future


/**
  * Created by denis on 12/3/16.
  */
class TagEventConsumer(readService: ReadService,
                       clientBroadcastService: ClientBroadcastService,
                       registry: IMessageProcessingRegistry)
  extends IMessageConsumer {
  private val log = Logger(this.getClass)

  registry.registerConsumer("read.tags", this)

  import util.ThreadPools.CPU
  override def messageReceived(event: Array[Byte]): Unit = {
    adjustReadState(LogRecord.decode(event)).recover { case th =>
      log.error("Error occurred while adjusting read state", th)
    }
  }

  private def adjustReadState(logRecord: LogRecord): Future[Unit] = {
    readService.adjustState(logRecord).flatMap { _ =>
      readService.getAllTags
    }.map { tags =>
      val update = ServerSentMessage.create("tags", tags)
      clientBroadcastService.broadcastUpdate(update.json)
    }
  }
}
