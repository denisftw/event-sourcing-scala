package security

import model.User
import play.api.Logger
import play.api.mvc._
import services.AuthService

import scala.concurrent.{ExecutionContext, Future}

case class UserAuthRequest[A](user: User,
  request: Request[A]) extends  WrappedRequest[A](request)

class UserAuthAction(authService: AuthService, ec: ExecutionContext,
                     playBodyParsers: PlayBodyParsers)
  extends ActionBuilder[UserAuthRequest, AnyContent] {

  override implicit val executionContext = ec
  override def parser = playBodyParsers.defaultBodyParser

  def invokeBlock[A](request: Request[A],
            block: UserAuthRequest[A] => Future[Result]): Future[Result] = {
    authService.checkCookie(request).flatMap {
      case None => UserAuthAction.unauthorized(request)
      case Some(user) => block(UserAuthRequest(user, request))
    }
  }
}

object UserAuthAction {
  val log = Logger("security.UserAuthAction")

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
    log.error("Exception occurred while invoking authenticated action", exc)
    Future.successful(Results.Redirect("/500"))
  }
}