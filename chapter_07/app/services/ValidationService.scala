package services

import com.appliedscala.events.LogRecord
import dao.ValidationDao
import messaging.IMessageProducer

import java.util.UUID
import scala.concurrent.Future
import scala.util.{Success}


/**
  * Created by denis on 12/6/16.
  */
class ValidationService(validationDao: ValidationDao, clientBroadcastService: ClientBroadcastService) {

  import util.ThreadPools.CPU
  def refreshState(events: Seq[LogRecord], fromScratch: Boolean):
  Future[Option[String]] = {
    validationDao.refreshState(events, fromScratch)
  }

  def validateAndSend(userId: UUID, event: LogRecord,
    messageProducer: IMessageProducer): Future[Option[String]] = {
    validationDao.validateSingle(event).andThen {
      case Success(Some(errorMessage)) =>
        clientBroadcastService.sendErrorMessage(userId, errorMessage)
      case Success(None) =>
        messageProducer.send(event.encode)
    }
  }
}
