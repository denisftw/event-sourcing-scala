package util

import java.util.UUID
import com.appliedscala.events.LogRecord
import messaging.IMessageProducer
import services.{ClientBroadcastService, ValidationService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by denis on 12/12/16.
  */
class EventValidator(validationService: ValidationService,
    clientBroadcastService: ClientBroadcastService) {

  def validateAndSend(userId: UUID, event: LogRecord,
      messageProducer: IMessageProducer): Future[Option[String]] = {
    val maybeErrorMessageF = validationService.validate(event)
    maybeErrorMessageF.map {
      case Some(errorMessage) =>
        clientBroadcastService.sendErrorMessage(userId, errorMessage)
      case None =>
        messageProducer.send(event.encode)
    }
    maybeErrorMessageF
  }
}
