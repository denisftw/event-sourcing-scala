package services

import akka.actor.ActorSystem
import com.appliedscala.events.LogRecord
import dao.Neo4JReadDao
import messaging.{IMessageConsumer, IMessageProcessingRegistry}
import model.ServerSentMessage
import play.api.Logger

import scala.concurrent.Future


/**
  * Created by denis on 12/3/16.
  */
class TagEventConsumer(readDao: Neo4JReadDao, clientBroadcastService: ClientBroadcastService,
                       registry: IMessageProcessingRegistry) extends IMessageConsumer {
  private val log = Logger(this.getClass)
  import util.ThreadPools.CPU
  registry.registerConsumer("read.tags", this)

  override def messageReceived(event: Array[Byte]): Unit = {
    adjustReadState(LogRecord.decode(event)).recover { case th =>
      log.error("Error occurred while adjusting read state", th)
    }
  }

  private def adjustReadState(logRecord: LogRecord): Future[Unit] = {
    for {
      _ <- readDao.processEvent(logRecord)
      tags <- readDao.getAllTags
    } yield {
      val update = ServerSentMessage.create("tags", tags)
      clientBroadcastService.broadcastUpdate(update.json)
    }
  }
}
