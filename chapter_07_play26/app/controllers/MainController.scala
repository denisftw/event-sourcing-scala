package controllers

import model._
import play.api.libs.json.JsValue
import play.api.mvc._
import security.{UserAuthAction, UserAwareAction, UserAwareRequest}
import akka.actor.ActorSystem
import akka.stream.{Materializer, OverflowStrategy}
import services.{ConsumerAggregator, RewindService}

class MainController(controllerComponents: ControllerComponents, userAuthAction: UserAuthAction,
    userAwareAction: UserAwareAction, actorSystem: ActorSystem,
    consumerAggregator: ConsumerAggregator,
    mat: Materializer) extends AbstractController(controllerComponents) {

  def index = userAwareAction { request =>
    Ok(views.html.pages.react(buildNavData(request),
      WebPageData("Home")))
  }

  def indexParam(unused: String) = index

  def error500 = Action {
    InternalServerError(views.html.errorPage())
  }

  private def buildNavData(request: UserAwareRequest[_]): NavigationData = {
    NavigationData(request.user, isLoggedIn = request.user.isDefined)
  }

  import actors.EventStreamActor
  import akka.stream.actor.ActorPublisher
  import play.api.libs.EventSource
  import akka.stream.scaladsl._

  def serverEventStream = userAwareAction { request =>
    implicit val materializer = mat
    implicit val actorFactory = actorSystem
    val maybeUser = request.user
    val maybeUserId = maybeUser.map(_.userId)
    val actorRef = actorSystem.actorOf(EventStreamActor.props(),
      EventStreamActor.name(maybeUserId))
    val eventStorePublisher = Source.
      fromPublisher(ActorPublisher[JsValue](actorRef)).
      runWith(Sink.asPublisher(fanout = true))
    val source = Source.fromPublisher(eventStorePublisher)
    Ok.chunked(source.via(EventSource.flow)).as("text/event-stream")
  }

  def wsStream = WebSocket.accept[JsValue, JsValue] { request =>
    import actors.WSStreamActor
    implicit val materializer = mat
    implicit val actorFactory = actorSystem
    val maybeUserId = userAuthAction.checkUser(request).map(_.userId)

    val (out, publisher) = Source.actorRef[JsValue](
      bufferSize = 16, OverflowStrategy.dropNew)
      .toMat(Sink.asPublisher(fanout = false))(Keep.both).run()
    val actorRef = actorSystem.actorOf(
      WSStreamActor.props(out), WSStreamActor.name(maybeUserId))
    val sink = Sink.actorRef[JsValue](actorRef, akka.actor.Status.Success(()))
    val source = Source.fromPublisher(publisher)
    Flow.fromSinkAndSource(sink, source)
  }
}
