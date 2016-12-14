package services

import akka.actor.ActorSystem
import com.appliedscala.events.LogRecord

import actors.ValidationActor



/**
  * Created by denis on 12/6/16.
  */
class ValidationService(actorSystem: ActorSystem) {

  val validationActor = actorSystem.actorOf(
    ValidationActor.props(), ValidationActor.name)

  def refreshState(events: Seq[LogRecord], fromScratch: Boolean): Unit = {
    validationActor ! ValidationActor.RefreshStateCommand(
      events, fromScratch)
  }

  import java.util.concurrent.TimeUnit
  import akka.util.Timeout
  import scala.concurrent.Future
  import akka.pattern.ask
  def validate(event: LogRecord): Future[Option[String]] = {
    implicit val timeout = Timeout.apply(5, TimeUnit.SECONDS)
    (validationActor ? ValidationActor.ValidateEventRequest(event)).
      mapTo[Option[String]]
  }
}
