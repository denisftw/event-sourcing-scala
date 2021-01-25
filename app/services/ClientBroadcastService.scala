package services

import akka.{Done, NotUsed}
import akka.stream.scaladsl.{Flow, Sink, Source}
import io.reactivex.rxjava3.processors.PublishProcessor
import model.QuestionThreadIdChanged
import org.reactivestreams.Subscriber
import play.api.libs.json.{JsObject, JsString, JsValue}

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import scala.concurrent.Future

class ClientBroadcastService {
  private val connectedClients = new ConcurrentHashMap[UUID, ConnectedClient]()
  import util.ThreadPools.CPU

  def createWsStream(userId: Option[UUID]): Flow[JsValue, JsValue, NotUsed] = {
    val publisher = PublishProcessor.create[JsValue]()
    val clientId = UUID.randomUUID()
    val toClient = Source.fromPublisher(publisher).
      watchTermination() { (_, done) =>
        done.andThen { case _ =>
          removeDisconnectedClient(clientId)
        }
    }
    val client = ConnectedClient(userId, None, publisher)
    addConnectedClient(clientId, client)
    val fromClient = Sink.foreach[JsValue](clientMessageReceived(clientId))
    Flow.fromSinkAndSource(fromClient, toClient)
  }

  def createEventStream(userId: Option[UUID]): Source[JsValue, Future[Done]] = {
    val publisher = PublishProcessor.create[JsValue]()
    val clientId = UUID.randomUUID()
    val toClient = Source.fromPublisher(publisher).
      watchTermination() { (_, done) =>
        done.andThen { case _ =>
          removeDisconnectedClient(clientId)
        }
      }
    val client = ConnectedClient(userId, None, publisher)
    addConnectedClient(clientId, client)
    toClient
  }

  def broadcastUpdate(data: JsValue): Future[Unit] = Future {
    connectedClients.values().forEach { client =>
      client.out.onNext(data)
    }
  }

  def broadcastQuestionThreadUpdate(questionThreadId: UUID, data: JsValue):
  Future[Unit] = Future {
    connectedClients.values().stream().filter(
      _.questionThreadId.contains(questionThreadId)).forEach { client =>
      client.out.onNext(data)
    }
  }

  def sendErrorMessage(userId: UUID, message: String): Future[Unit] = Future {
    connectedClients.values().stream().filter(_.userId.contains(userId)).forEach { client =>
      client.out.onNext(JsObject(Seq("error" -> JsString(message))))
    }
  }

  private def clientMessageReceived(clientId: UUID)(msg: JsValue): Unit = {
    msg.validate[QuestionThreadIdChanged].foreach { changed =>
      updateQuestionThread(clientId, Some(changed.questionThreadId))
    }
  }

  private def updateQuestionThread(clientId: UUID, newQuestionThreadId: Option[UUID]): Unit = {
    connectedClients.computeIfPresent(clientId, (_, client) => {
      client.copy(questionThreadId = newQuestionThreadId)
    })
  }

  private def addConnectedClient(clientId: UUID, connectedClient: ConnectedClient): Unit = {
    connectedClients.put(clientId, connectedClient)
  }

  private def removeDisconnectedClient(clientId: UUID): Unit = {
    connectedClients.remove(clientId)
  }

  case class ConnectedClient(userId: Option[UUID], questionThreadId: Option[UUID],
                             out: Subscriber[JsValue])

}


