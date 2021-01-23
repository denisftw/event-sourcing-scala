package services

import com.appliedscala.events.LogRecord
import dao.Neo4JReadDao
import messaging.{IMessageConsumer, IMessageProcessingRegistry}
import play.api.Logger

import scala.concurrent.Future

/**
  * Created by denis on 12/26/16.
  */
class UserEventConsumer(neo4JReadDao: Neo4JReadDao, registry: IMessageProcessingRegistry) extends IMessageConsumer {
  private val log = Logger(this.getClass)
  import util.ThreadPools.CPU

  registry.registerConsumer("read.users", this)

  override def messageReceived(event: Array[Byte]): Unit = {
    adjustReadState(LogRecord.decode(event)).recover { case th =>
      log.error("Error occurred while adjusting read state", th)
    }
  }

  private def adjustReadState(logRecord: LogRecord): Future[Unit] = {
    neo4JReadDao.processEvent(logRecord)
  }
}
