package actors

import java.util.UUID

import akka.actor.{Actor, ActorRef, Props}
import play.api.libs.json.{JsObject, JsString, JsValue}


/**
  * Created by denis on 12/1/16.
  */
class EventStreamActor(out: ActorRef) extends Actor {
  import EventStreamActor._

  override def receive: Receive = {
    case DataUpdated(js) => onNext(js)
    case ErrorOccurred(message) =>
      onNext(JsObject(Seq("error" -> JsString(message))))
  }

  private def onNext(js: JsValue): Unit = {
    out ! js
  }
}

object EventStreamActor {
  def props(out: ActorRef) = Props(new EventStreamActor(out))

  case class DataUpdated(jsValue: JsValue)
  case class ErrorOccurred(message: String)

  val name = "event-stream-actor"
  val pathPattern = s"/user/$name-*"

  def name(maybeUserId: Option[UUID]): String = {
    val randomPart = UUID.randomUUID().toString.split("-").apply(0)
    val userPart = maybeUserId.map(_.toString).
      getOrElse("unregistered")
    s"$name-$userPart-$randomPart"
  }
  def userSpecificPathPattern(userId: UUID) =
    s"/user/$name-${userId.toString}-*"
}