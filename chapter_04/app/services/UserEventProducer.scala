package services

import java.util.UUID

import akka.actor.ActorSystem
import com.appliedscala.events.LogRecord
import com.appliedscala.events.user.{UserActivated, UserDeactivated}
import play.api.Configuration
import util.{EventValidator, ServiceKafkaProducer}


/**
  * Created by denis on 12/26/16.
  */
class UserEventProducer(actorSystem: ActorSystem, configuration: Configuration,
    eventValidator: EventValidator) {

  val kafkaProducer = new ServiceKafkaProducer("users",
    actorSystem, configuration)

  def activateUser(userId: UUID): Unit = {
    val event = UserActivated(userId)
    val record = LogRecord.fromEvent(event)
    eventValidator.validateAndSend(userId, record, kafkaProducer)
  }

  def deactivateUser(userId: UUID): Unit = {
    val event = UserDeactivated(userId)
    val record = LogRecord.fromEvent(event)
    eventValidator.validateAndSend(userId, record, kafkaProducer)
  }
}
