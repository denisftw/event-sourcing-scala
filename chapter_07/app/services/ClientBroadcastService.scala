package services

import actors.QuestionThreadIdChanged
import akka.NotUsed
import akka.stream.scaladsl.{Flow, Sink, Source}
import monix.reactive.subjects.BehaviorSubject
import play.api.libs.json.{JsNull, JsObject, JsString, JsValue}

import java.util.UUID
import scala.concurrent.Future

class ClientBroadcastService {
  private var connectedClients = Map.empty[UUID, ConnectedClient]
  import util.ThreadPools.CPUScheduler

  def registerClient(userId: Option[UUID]): Flow[JsValue, JsValue, NotUsed] = {
    val subject = BehaviorSubject.apply[JsValue](JsNull)
    val clientId = UUID.randomUUID()
    val toClient = Source.fromPublisher(subject.toReactivePublisher).
      watchTermination() { (_, done) =>
        done.andThen { case _ =>
          removeDisconnectedClient(clientId)
        }
    }
    val client = ConnectedClient(userId, None, subject)
    addConnectedClient(clientId, client)
    val fromClient = Sink.foreach[JsValue](clientMessageReceived(clientId))
    val flow = Flow.fromSinkAndSource(fromClient, toClient)
    flow
  }

  def broadcastUpdate(data: JsValue): Future[Unit] = Future {
    connectedClients.values.foreach { client =>
      client.out.onNext(data)
    }
  }

  def broadcastQuestionThreadUpdated(questionThreadId: UUID, data: JsValue): Future[Unit] = Future {
    connectedClients.values.filter(_.questionThreadId.contains(questionThreadId)).foreach { client =>
      client.out.onNext(data)
    }
  }

  def sendErrorMessage(userId: UUID, message: String): Future[Unit] = Future {
    connectedClients.values.filter(_.userId.contains(userId)).foreach { client =>
      client.out.onNext(JsObject(Seq("error" -> JsString(message))))
    }
  }

  private def clientMessageReceived(clientId: UUID)(msg: JsValue): Unit = {
    msg.validate[QuestionThreadIdChanged].foreach { changed =>
      updateQuestionThread(clientId, Some(changed.questionThreadId))
    }
  }

  private def updateQuestionThread(clientId: UUID, newQuestionThreadId: Option[UUID]): Unit = {
    connectedClients.synchronized {
      connectedClients.get(clientId).foreach { client =>
        client.copy(questionThreadId = newQuestionThreadId)
      }
    }
  }

  private def addConnectedClient(clientId: UUID, connectedClient: ConnectedClient): Unit = {
    connectedClients.synchronized {
      connectedClients += (clientId -> connectedClient)
    }
  }

  private def removeDisconnectedClient(clientId: UUID): Unit = {
    connectedClients.synchronized {
      connectedClients -= clientId
    }
  }

  case class ConnectedClient(userId: Option[UUID], questionThreadId: Option[UUID],
                             out: BehaviorSubject[JsValue])
}


