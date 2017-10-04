package services

import actors.{EventStreamActor, WSStreamActor}
import akka.actor.ActorSystem
import akka.stream.Materializer
import com.appliedscala.events.LogRecord
import dao.Neo4JReadDao
import model.ServerSentMessage
import play.api.Configuration
import util.ServiceKafkaConsumer

/**
  * Created by denis on 12/23/16.
  */
class AnswerEventConsumer(neo4JReadDao: Neo4JReadDao,
    actorSystem: ActorSystem, configuration: Configuration,
    materializer: Materializer, readService: ReadService) {

  val topicName = "answers"
  val serviceKafkaConsumer = new ServiceKafkaConsumer(Set(topicName),
    "read", materializer, actorSystem, configuration, handleEvent)

  private def handleEvent(event: String): Unit = {
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