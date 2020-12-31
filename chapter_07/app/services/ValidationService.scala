package services

import com.appliedscala.events.LogRecord
import dao.ValidationDao

import scala.concurrent.Future


/**
  * Created by denis on 12/6/16.
  */
class ValidationService(validationDao: ValidationDao) {

  def refreshState(events: Seq[LogRecord], fromScratch: Boolean):
  Future[Option[String]] = {
    validationDao.refreshState(events, fromScratch)
  }

  def validate(event: LogRecord): Future[Option[String]] = {
    validationDao.validateSingle(event)
  }
}
