package actors

import java.util.UUID

import akka.actor.{Actor, ActorRef, PoisonPill, Props, Status}
import play.api.libs.json.{JsObject, JsString, JsValue, Json}


case class QuestionThreadIdChanged(questionThreadId: UUID)

object QuestionThreadIdChanged {
  implicit val reads = Json.reads[QuestionThreadIdChanged]
}

/**
  * Created by denis on 12/30/16.
  */
class WSStreamActor(out: ActorRef) extends Actor {
  import actors.WSStreamActor._
  var questionThreadId: Option[UUID] = None

  def receive = {
    case msg: JsValue =>
      msg.validate[QuestionThreadIdChanged].foreach { changed =>
        questionThreadId = Some(changed.questionThreadId)
      }
    case QuestionThreadDataUpdated(id, js) =>
      if (questionThreadId.contains(id)) {
        out ! js
      }
    case DataUpdated(js) => out ! js
    case ErrorOccurred(message) =>
      out ! JsObject(Seq("error" -> JsString(message)))
    case Status.Success(_) => self ! PoisonPill
  }
}

object WSStreamActor {
  def props(out: ActorRef) = Props(new WSStreamActor(out))

  case class QuestionThreadDataUpdated(
     questionThreadId: UUID, jsValue: JsValue)
  case class DataUpdated(jsValue: JsValue)
  case class ErrorOccurred(message: String)

  val name = "ws-stream-actor"
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