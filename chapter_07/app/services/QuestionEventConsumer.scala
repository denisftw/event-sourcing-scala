package services

import akka.actor.ActorSystem
import com.appliedscala.events.LogRecord
import dao.Neo4JReadDao
import model.ServerSentMessage
import play.api.Logger
import util.{IMessageConsumer, IMessageProcessingRegistry}

import scala.concurrent.Future

/**
  * Created by denis on 12/14/16.
  */
class QuestionEventConsumer(neo4JReadDao: Neo4JReadDao,
                            actorSystem: ActorSystem, clientBroadcastService: ClientBroadcastService,
                            registry: IMessageProcessingRegistry, readService: ReadService) extends IMessageConsumer {
  private val log = Logger(this.getClass)
  import util.ThreadPools.CPU
  registry.registerConsumer("read.questions", this)

  override def messageReceived(event: Array[Byte]): Unit = {
    adjustReadState(LogRecord.decode(event)).recover { case th =>
      log.error("Error occurred while adjusting read state", th)
    }
  }

  private def adjustReadState(logRecord: LogRecord): Future[Unit] = {
    for {
      _ <- neo4JReadDao.handleEvent(logRecord)
      questions <- readService.getAllQuestions
    } yield {
      val update = ServerSentMessage.create("questions", questions)
      clientBroadcastService.broadcastUpdate(update.json)
    }
  }
}
