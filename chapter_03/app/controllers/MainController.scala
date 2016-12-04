package controllers

import java.util.UUID

import model._
import play.api.libs.json.JsValue
import play.api.mvc._
import security.{UserAuthAction, UserAwareAction, UserAwareRequest}
import akka.actor.ActorSystem
import akka.stream.Materializer
import services.ConsumerAggregator
class MainController(userAuthAction: UserAuthAction,
                     userAwareAction: UserAwareAction,
                     actorSystem: ActorSystem,
                     consumerAggregator: ConsumerAggregator,
                     mat: Materializer) extends Controller {

  def index = userAwareAction { request =>
    Ok(views.html.pages.react(buildNavData(request),
      WebPageData("Home")))
  }

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
}
