package controllers

import controllers.Assets.Asset
import model._
import play.api.libs.json.JsValue
import play.api.mvc._
import security.{UserAuthAction, UserAwareAction, UserAwareRequest}
import akka.actor.ActorSystem
import akka.stream.{Materializer}
import services.{AuthService, ClientBroadcastService, ConsumerAggregator, RewindService}


class MainController(components: ControllerComponents, assets: Assets,
                     consumerAggregator: ConsumerAggregator, rewindService: RewindService,
                     authService: AuthService, clientBroadcastService: ClientBroadcastService,
                     userAuthAction: UserAuthAction, userAwareAction: UserAwareAction)
  extends AbstractController(components) {

  import util.ThreadPools.CPU

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

  def serverEventStream = userAwareAction { request =>
    ???
  }

  def wsStream() = WebSocket.acceptOrResult[JsValue, JsValue] { request =>
    authService.checkCookie(request).map(_.map(_.userId)).map { maybeUserId =>
      Right(clientBroadcastService.registerClient(maybeUserId))
    }
  }

  def versioned(path: String, file: Asset) = assets.versioned(path, file)
}
