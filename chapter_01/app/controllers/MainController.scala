package controllers

import java.util.UUID

import model._
import play.api.libs.json.JsValue
import play.api.mvc._
import security.{UserAuthAction, UserAwareAction, UserAwareRequest}


class MainController(userAuthAction: UserAuthAction,
                     userAwareAction: UserAwareAction) extends Controller {

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
}
