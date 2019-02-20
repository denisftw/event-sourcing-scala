package services

import play.api.routing.Router
import play.api.{ApplicationLoader, Configuration, Environment, Logger}
import play.api.http.DefaultHttpErrorHandler
import play.api.mvc._

import scala.concurrent._


class ProdErrorHandler(environment: Environment, configuration: Configuration,
                       devContext: Option[ApplicationLoader.DevContext] = None, router: => Option[Router] = None)
  extends DefaultHttpErrorHandler(environment, configuration, devContext.map(_.sourceMapper), router) {

  private val log = Logger(this.getClass)

  override protected def onNotFound(request: RequestHeader, message: String):
  Future[Result] = {
    implicit val req = request
    Future.successful(Results.NotFound(views.html.errorPage()))
  }

  override def onServerError(request: RequestHeader, exception: Throwable):
  Future[Result] = {
    log.error("Exception occurred", exception)
    implicit val req = request
    Future.successful(Results.InternalServerError(views.html.errorPage()))
  }

  override protected def onBadRequest(request: RequestHeader, message: String):
  Future[Result] = {
    super.onBadRequest(request, message)
  }
}
