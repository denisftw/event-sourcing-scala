package controllers

import java.util.UUID

import model._
import play.api.libs.json.JsValue
import play.api.mvc._
import security.{UserAuthAction, UserAwareAction, UserAwareRequest}
import akka.actor.ActorSystem
import akka.stream.{Materializer, OverflowStrategy}
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
  import play.api.libs.EventSource
  import akka.stream.scaladsl._
  import play.api.http.ContentTypes
  def serverEventStream = userAwareAction { request =>
    implicit val materializer = mat
    implicit val actorFactory = actorSystem
    val maybeUser = request.user
    val maybeUserId = maybeUser.map(_.userId)
    val (out, publisher) = Source.actorRef[JsValue](
      bufferSize = 16, OverflowStrategy.dropNew)
      .toMat(Sink.asPublisher(fanout = false))(Keep.both).run()
    actorSystem.actorOf(EventStreamActor.props(out),
      EventStreamActor.name(maybeUserId))
    val source = Source.fromPublisher(publisher)
    Ok.chunked(source.via(EventSource.flow)).as(ContentTypes.EVENT_STREAM)
  }
}
