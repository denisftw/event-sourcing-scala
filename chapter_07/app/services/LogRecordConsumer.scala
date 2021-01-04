package services

import com.appliedscala.events.LogRecord
import dao.LogDao
import messaging.{IMessageConsumer, IMessageProcessingRegistry}

/**
  * Created by denis on 12/4/16.
  */
class LogRecordConsumer(logDao: LogDao, registry: IMessageProcessingRegistry)
extends IMessageConsumer {
  registry.registerConsumer("log.*", this)

  override def messageReceived(event: Array[Byte]): Unit = {
    logDao.insertLogRecord(LogRecord.decode(event))
  }
}
