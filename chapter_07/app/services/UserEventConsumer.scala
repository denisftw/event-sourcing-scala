package services

import com.appliedscala.events.LogRecord
import dao.Neo4JReadDao
import util.{IMessageConsumer, IMessageProcessingRegistry}

/**
  * Created by denis on 12/26/16.
  */
class UserEventConsumer(neo4JReadDao: Neo4JReadDao, registry: IMessageProcessingRegistry) extends IMessageConsumer {

  registry.registerConsumer("read.users", this)

  override def messageReceived(event: Array[Byte]): Unit = {
    val maybeLogRecord = LogRecord.decode(event)
    maybeLogRecord.foreach(adjustReadState)
  }

  private def adjustReadState(logRecord: LogRecord): Unit = {
    neo4JReadDao.handleEvent(logRecord)
  }
}
