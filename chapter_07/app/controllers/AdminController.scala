package controllers

import model.{NavigationData, WebPageData}
import java.time.ZonedDateTime

import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{AbstractController, ControllerComponents}
import security.UserAuthAction
import services.RewindService
import util.BaseTypes

import scala.util.{Failure, Success}

/**
  * Created by denis on 12/29/16.
  */
class AdminController(components: ControllerComponents, userAuthAction: UserAuthAction,
    rewindService: RewindService) extends AbstractController(components) {

  val log = Logger(this.getClass)

  def admin = userAuthAction { request =>
    if (request.user.isAdmin) {
      Ok(views.html.pages.admin(NavigationData(Some(request.user),
        isLoggedIn = true), WebPageData("Admin")))
    } else Forbidden
  }

  def rewind() = userAuthAction { implicit request =>
    if (request.user.isAdmin) {
      rewindRequestForm.bindFromRequest.fold(
        errors => BadRequest,
        data => {
          val resultT = rewindService.refreshState(Some(data.destination))
          resultT match {
            case Failure(th) =>
              log.error("Error occurred while rewinding the events", th)
              InternalServerError(views.html.errorPage())
            case Success(result) => Ok
          }
        }
      )
    } else Forbidden
  }

  val rewindRequestForm = Form {
    mapping(
      "destination" -> BaseTypes.zonedDateTimeMapping
    )(RewindRequestData.apply)(RewindRequestData.unapply)
  }

  case class RewindRequestData(destination: ZonedDateTime)
}