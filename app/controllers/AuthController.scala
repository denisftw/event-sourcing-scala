package controllers

import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import security.UserAuthAction
import services.AuthService

import scala.concurrent.Future

class AuthController(components: ControllerComponents, authService: AuthService,
                     userAuthAction: UserAuthAction) extends AbstractController(components) {

  import util.ThreadPools.CPU
  def logout() = userAuthAction { request =>
    authService.destroySession(request.request)
    Redirect("/").discardingCookies(DiscardingCookie(authService.cookieHeader))
  }

  def login() = Action { request =>
    Ok(views.html.security.login(None))
  }

  def register() = Action {
    Ok(views.html.security.signUp(None))
  }

  def doLogin() = Action.async(parse.anyContent) { implicit request =>
    userLoginDataForm.bindFromRequest().fold(
      formWithErrors => Future.successful(Ok(views.html.security.login(Some("Wrong data")))),
      userData => {
        authService.login(userData.username, userData.password).map { cookie =>
          UserAuthAction.redirectAfterLogin(request, cookie)
        }.recover { case th =>
          Ok(views.html.security.login(Some(th.getMessage)))
        }
      }
    )
  }

  def registerUser() = Action.async(parse.anyContent) { implicit request =>
    userRegistrationDataForm.bindFromRequest().fold(
      formWithErrors => Future.successful(Ok(views.html.security.signUp(Some("Wrong data")))),
      userData => {
        val passwordsMatch = userData.password1 == userData.password2
        if (!passwordsMatch) {
          Future.successful(Ok(views.html.security.signUp(
            Some("Passwords don't match"))))
        }
        else {
          authService.register(userData.username,
            userData.fullName, userData.password1).map { cookie =>
            Redirect("/").withCookies(cookie)
          }.recover { case th =>
            Ok(views.html.security.signUp(Some(th.getMessage)))
          }
        }
      }
    )
  }

  private val userLoginDataForm = Form {
    mapping(
      "username" -> email,
      "password" -> nonEmptyText
    )(UserLoginData.apply)(UserLoginData.unapply)
  }

  private val userRegistrationDataForm = Form {
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
