package services

import java.util.UUID
import com.appliedscala.events.LogRecord
import com.appliedscala.events.user.{UserActivated, UserDeactivated}
import messaging.IMessageProcessingRegistry

import scala.concurrent.Future

/**
  * Created by denis on 12/26/16.
  */
class UserEventProducer(registry: IMessageProcessingRegistry,
                        validationService: ValidationService) {

  private val producer = registry.createProducer("users")

  def activateUser(userId: UUID): Future[Option[String]] = {
    val event = UserActivated(userId)
    val record = LogRecord.fromEvent(event)
    validationService.validateAndSend(userId, record, producer)
  }

  def deactivateUser(userId: UUID): Future[Option[String]] = {
    val event = UserDeactivated(userId)
    val record = LogRecord.fromEvent(event)
    validationService.validateAndSend(userId, record, producer)
  }
}
