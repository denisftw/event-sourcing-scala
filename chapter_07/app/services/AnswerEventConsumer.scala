package services

import actors.WSStreamActor
import akka.actor.ActorSystem
import com.appliedscala.events.LogRecord
import dao.Neo4JReadDao
import model.ServerSentMessage
import util.{IMessageConsumer, IMessageProcessingRegistry}

/**
  * Created by denis on 12/23/16.
  */
class AnswerEventConsumer(neo4JReadDao: Neo4JReadDao,
                          actorSystem: ActorSystem, registry: IMessageProcessingRegistry,
                          readService: ReadService) extends IMessageConsumer {
  registry.registerConsumer("read.answers", this)

  override def messageReceived(event: Array[Byte]): Unit = {
    val maybeLogRecord = LogRecord.decode(event)
    maybeLogRecord.foreach(adjustReadState)
  }

  private def adjustReadState(logRecord: LogRecord): Unit = {
    neo4JReadDao.handleEventWithUpdate(logRecord) { maybeUpdateId =>
      maybeUpdateId.foreach { updateId =>
        val threadT = readService.getQuestionThread(updateId)
        threadT.foreach { maybeThread =>
          maybeThread.foreach { thread =>
            val update = ServerSentMessage.create("questionThread", thread)
            val esActor = actorSystem.actorSelection(WSStreamActor.pathPattern)
            esActor ! WSStreamActor.QuestionThreadDataUpdated(
              thread.question.id, update.json)
          }
        }
      }
    }
  }
}