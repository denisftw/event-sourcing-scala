package controllers

import controllers.Assets.Asset
import model._
import play.api.mvc._
import security.{UserAuthAction, UserAwareAction, UserAwareRequest}


class MainController(components: ControllerComponents, assets: Assets, userAuthAction: UserAuthAction,
                     userAwareAction: UserAwareAction) extends AbstractController(components) {

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

  def versioned(path: String, file: Asset) = assets.versioned(path, file)
}
