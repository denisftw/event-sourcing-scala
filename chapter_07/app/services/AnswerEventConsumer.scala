package services

import com.appliedscala.events.LogRecord
import dao.Neo4JReadDao
import messaging.{IMessageConsumer, IMessageProcessingRegistry}
import model.ServerSentMessage
import play.api.Logger

import scala.concurrent.Future

/**
  * Created by denis on 12/23/16.
  */
class AnswerEventConsumer(readDao: Neo4JReadDao,
                          clientBroadcastService: ClientBroadcastService,
                          registry: IMessageProcessingRegistry,
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
    readDao.handleEventWithUpdate(logRecord) { maybeUpdateId =>
      maybeUpdateId.map { updateId =>
        readService.getQuestionThread(updateId).map { maybeThread =>
          maybeThread.map { thread =>
            val update = ServerSentMessage.create("questionThread", thread)
            clientBroadcastService.broadcastQuestionThreadUpdate(
              thread.question.id, update.json)
          }
        }
      }
    }
  }
}