package controllers

import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{Action, Controller, DiscardingCookie}
import security.UserAuthAction
import services.AuthService

import scala.util.{Failure, Success}

class AuthController(authService: AuthService,
                     userAuthAction: UserAuthAction) extends Controller {

  def logout = userAuthAction { request =>
    authService.destroySession(request.request)
    Redirect("/").discardingCookies(DiscardingCookie(authService.cookieHeader))
  }

  def login = Action { request =>
    Ok(views.html.security.login(None))
  }

  def register = Action {
    Ok(views.html.security.signUp(None))
  }

  def doLogin() = Action(parse.anyContent) { implicit request =>
    userLoginDataForm.bindFromRequest.fold(
      formWithErrors => Ok(views.html.security.login(Some("Wrong data"))),
      userData => {
        val cookieT = authService.login(userData.username, userData.password)
        cookieT match {
          case Success(cookie) =>
            UserAuthAction.redirectAfterLogin(request, cookie)
          case Failure(th) => Ok(views.html.security.login(Some(th.getMessage)))
        }
      }
    )
  }

  def registerUser() = Action(parse.anyContent) { implicit request =>
    userRegistrationDataForm.bindFromRequest.fold(
      formWithErrors => Ok(views.html.security.signUp(Some("Wrong data"))),
      userData => {
        val passwordsMatch = userData.password1 == userData.password2
        if (!passwordsMatch) Ok(views.html.security.signUp(
          Some("Passwords don't match")))
        else {
          val cookieT = authService.register(userData.username,
            userData.fullName, userData.password1)
          cookieT match {
            case Success(cookie) => Redirect("/").withCookies(cookie)
            case Failure(th) =>
              Ok(views.html.security.signUp(Some(th.getMessage)))
          }
        }
      }
    )
  }

  val userLoginDataForm = Form {
    mapping(
      "username" -> email,
      "password" -> nonEmptyText
    )(UserLoginData.apply)(UserLoginData.unapply)
  }

  val userRegistrationDataForm = Form {
    mapping(
      "username" -> email,
      "fullName" -> nonEmptyText,
      "password1" -> nonEmptyText,
      "password2" -> nonEmptyText
    )(UserRegistrationData.apply)(UserRegistrationData.unapply)
  }

  case class UserLoginData(username: String, password: String)
  case class UserRegistrationData(username: String, fullName: String,
                                  password1: String, password2: String)
}
