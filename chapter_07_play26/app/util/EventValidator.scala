package util

import java.util.UUID

import actors.{EventStreamActor, WSStreamActor}
import akka.actor.ActorSystem
import com.appliedscala.events.LogRecord
import services.ValidationService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by denis on 12/12/16.
  */
class EventValidator(validationService: ValidationService,
    actorSystem: ActorSystem) {

  def validateAndSend(userId: UUID, event: LogRecord,
      kafkaProducer: ServiceKafkaProducer): Future[Option[String]] = {
    val maybeErrorMessageF = validationService.validate(event)
    maybeErrorMessageF.map {
      case Some(errorMessage) =>
        val actorSelection = actorSystem.actorSelection(
          WSStreamActor.userSpecificPathPattern(userId))
        actorSelection ! WSStreamActor.ErrorOccurred(errorMessage)
      case None =>
        kafkaProducer.send(event.encode)
    }
    maybeErrorMessageF
  }
}
