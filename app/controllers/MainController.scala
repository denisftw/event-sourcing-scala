package controllers

import controllers.Assets.Asset
import model._
import play.api.http.ContentTypes
import play.api.libs.EventSource
import play.api.libs.json.JsValue
import play.api.mvc._
import security.{UserAuthAction, UserAwareAction, UserAwareRequest}
import services.{AuthService, ClientBroadcastService, ConsumerAggregator}


class MainController(components: ControllerComponents, assets: Assets,
                     clientBroadcastService: ClientBroadcastService,
                     consumerAggregator: ConsumerAggregator,
                     userAuthAction: UserAuthAction,
                     authService: AuthService,
                     userAwareAction: UserAwareAction)
  extends AbstractController(components) {

  import util.ThreadPools.CPU

  def index() = userAwareAction { request =>
    Ok(views.html.pages.react(buildNavData(request),
      WebPageData("Home")))
  }

  def indexParam(unused: String) = index()

  def error500() = Action {
    InternalServerError(views.html.errorPage())
  }

  private def buildNavData(request: UserAwareRequest[_]): NavigationData = {
    NavigationData(request.user, isLoggedIn = request.user.isDefined)
  }

  def serverEventStream() = userAwareAction { request =>
    val source = clientBroadcastService.createEventStream(
      request.user.map(_.userId))
    Ok.chunked(source.via(EventSource.flow)).as(ContentTypes.EVENT_STREAM)
  }

  def wsStream() = WebSocket.acceptOrResult[JsValue, JsValue] { request =>
    authService.checkCookie(request).map(_.map(_.userId)).map { maybeUserId =>
      Right(clientBroadcastService.createWsStream(maybeUserId))
    }
  }

  def versioned(path: String, file: Asset) = assets.versioned(path, file)
}
