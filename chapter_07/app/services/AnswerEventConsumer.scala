package services

import akka.actor.ActorSystem
import com.appliedscala.events.LogRecord
import dao.Neo4JReadDao
import model.ServerSentMessage
import play.api.Logger
import util.{IMessageConsumer, IMessageProcessingRegistry}

import scala.concurrent.Future

/**
  * Created by denis on 12/23/16.
  */
class AnswerEventConsumer(neo4JReadDao: Neo4JReadDao, clientBroadcastService: ClientBroadcastService,
                          actorSystem: ActorSystem, registry: IMessageProcessingRegistry,
                          readService: ReadService) extends IMessageConsumer {
  private val log = Logger(this.getClass)
  import util.ThreadPools.CPU
  registry.registerConsumer("read.answers", this)

  override def messageReceived(event: Array[Byte]): Unit = {
    adjustReadState(LogRecord.decode(event)).recover { case th =>
      log.error("Error occurred while adjusting read state", th)
    }
  }

  private def adjustReadState(logRecord: LogRecord): Future[Unit] = {
    neo4JReadDao.handleEventWithUpdate(logRecord) { maybeUpdateId =>
      maybeUpdateId.map { updateId =>
        readService.getQuestionThread(updateId).map { maybeThread =>
          maybeThread.map { thread =>
            val update = ServerSentMessage.create("questionThread", thread)
            clientBroadcastService.broadcastQuestionThreadUpdate(thread.question.id, update.json)
          }
        }
      }
    }
  }
}