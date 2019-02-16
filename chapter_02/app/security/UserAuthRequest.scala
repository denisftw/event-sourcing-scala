package security

import model.User
import play.api.Logger
import play.api.mvc._
import services.AuthService

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

case class UserAwareRequest[A](user: Option[User],
    request: Request[A]) extends WrappedRequest[A](request)

class UserAwareAction(authService: AuthService, ec: ExecutionContext,
                      playBodyParsers: PlayBodyParsers)
  extends ActionBuilder[UserAwareRequest, AnyContent] {

  val log = Logger(this.getClass)
  override val executionContext = ec
  override def parser = playBodyParsers.defaultBodyParser

  def invokeBlock[A](request: Request[A],
                     block: UserAwareRequest[A] => Future[Result]): Future[Result] = {
    val maybeUserT = authService.checkCookie(request)
    maybeUserT match {
      case Success(maybeUser) => block(UserAwareRequest(maybeUser, request))
      case Failure(exc) =>
        log.error("Exception occurred while invoking user-aware action", exc)
        Future.successful(Results.Redirect("/500"))
    }
  }
}
