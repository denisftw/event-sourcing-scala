package services

import actors.WSStreamActor
import akka.actor.ActorSystem
import com.appliedscala.events.LogRecord
import dao.Neo4JReadDao
import model.ServerSentMessage
import util.{IMessageConsumer, IMessageProcessingRegistry}

/**
  * Created by denis on 12/14/16.
  */
class QuestionEventConsumer(neo4JReadDao: Neo4JReadDao,
                            actorSystem: ActorSystem,
                            registry: IMessageProcessingRegistry, readService: ReadService) extends IMessageConsumer {

  registry.registerConsumer("read.questions", this)

  override def messageReceived(event: Array[Byte]): Unit = {
    val maybeLogRecord = LogRecord.decode(event)
    maybeLogRecord.foreach(adjustReadState)
  }

  private def adjustReadState(logRecord: LogRecord): Unit = {
    neo4JReadDao.handleEvent(logRecord)
    val questionsT = readService.getAllQuestions
    questionsT.foreach { questions =>
      val update = ServerSentMessage.create("questions", questions)
      val esActor = actorSystem.actorSelection(WSStreamActor.pathPattern)
      esActor ! WSStreamActor.DataUpdated(update.json)
    }
  }
}
