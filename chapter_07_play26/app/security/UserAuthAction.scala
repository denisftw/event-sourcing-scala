package security

import model.User
import play.api.Logger
import play.api.mvc._
import services.AuthService

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}


case class UserAuthRequest[A](user: User,
  request: Request[A]) extends  WrappedRequest[A](request)

class UserAuthAction(authService: AuthService,
  playBodyParsers: PlayBodyParsers,
  override val executionContext: ExecutionContext)
extends ActionBuilder[UserAuthRequest, AnyContent] {

  override def parser = playBodyParsers.defaultBodyParser

  def invokeBlock[A](request: Request[A],
            block: (UserAuthRequest[A]) => Future[Result]): Future[Result] = {
    val maybeUserD = authService.checkCookie(request)
    maybeUserD match {
      case Success(None) => UserAuthAction.unauthorized(request)
      case Success(Some(user)) => block(UserAuthRequest(user, request))
      case Failure(th) => UserAuthAction.exception(th)
    }
  }

  def checkUser[A](request: RequestHeader): Option[User] = {
    authService.checkCookie(request) match {
      case Success(Some(user)) => Some(user)
      case _ => None
    }
  }
}

object UserAuthAction {
  val RequestUrl = "X-Auth-Request-Url"

  def redirectAfterLogin(request : Request[AnyContent], cookie: Cookie): Result = {
    val maybeRequestedUrl = request.cookies.get(RequestUrl)
    maybeRequestedUrl match {
      case Some(url) => Results.Redirect(url.value).discardingCookies(DiscardingCookie(RequestUrl)).withCookies(cookie)
      case None => Results.Redirect("/").withCookies(cookie)
    }
  }

  def unauthorized[A](request: Request[A]): Future[Result] = {
    val requestPath = request.path
    if (requestPath.startsWith("/api/")) {
      Future.successful(Results.Unauthorized)
    } else {
      val requestedUrl = request.path
      Future.successful(Results.Redirect("/login").withCookies(Cookie(UserAuthAction.RequestUrl, requestedUrl)))
    }
  }

  def exception(exc: Throwable): Future[Result] = {
    Logger.error("Exception occurred while invoking authenticated action", exc)
    Future.successful(Results.Redirect("/500"))
  }
}