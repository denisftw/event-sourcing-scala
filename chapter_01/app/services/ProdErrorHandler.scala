package services

import play.api.routing.Router
import play.api.{Configuration, Environment, Logger}
import play.api.http.DefaultHttpErrorHandler
import play.api.mvc._
import play.core.SourceMapper
import scala.concurrent._


class ProdErrorHandler(environment: Environment, configuration: Configuration,
                       sourceMapper: Option[SourceMapper] = None,
                       router: => Option[Router] = None)
  extends DefaultHttpErrorHandler (
  environment, configuration, sourceMapper, router) {

  override protected def onNotFound(request: RequestHeader, message: String):
  Future[Result] = {
    implicit val req = request
    Future.successful(Results.NotFound(views.html.errorPage()))
  }

  override def onServerError(request: RequestHeader, exception: Throwable):
  Future[Result] = {
    Logger.error("Exception occurred", exception)
    implicit val req = request
    Future.successful(Results.InternalServerError(views.html.errorPage()))
  }

  override protected def onBadRequest(request: RequestHeader, message: String):
  Future[Result] = {
    super.onBadRequest(request, message)
  }
}
