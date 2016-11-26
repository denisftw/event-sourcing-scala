import controllers.Assets
import play.api.ApplicationLoader.Context
import play.api._
import play.api.db.{DBComponents, HikariCPComponents}
import play.api.db.evolutions.{DynamicEvolutions, EvolutionsComponents}
import play.api.routing.Router
import router.Routes
import com.softwaremill.macwire._
import controllers._
import dao._
import scalikejdbc.config.DBs
import security.{UserAuthAction, UserAwareAction}
import services._

import scala.concurrent.Future

class AppLoader extends ApplicationLoader {
  def load(context: Context) = {
    LoggerConfigurator(context.environment.classLoader).foreach { configurator =>
      configurator.configure(context.environment)
    }
    (new BuiltInComponentsFromContext(context) with AppComponents).application
  }
}

trait AppComponents extends BuiltInComponents
 with EvolutionsComponents with DBComponents with HikariCPComponents {
  lazy val assets: Assets = wire[Assets]
  lazy val prefix: String = "/"
  lazy val router: Router = wire[Routes]
  lazy val maybeRouter = Option(router)
  override lazy val httpErrorHandler = wire[ProdErrorHandler]

  lazy val mainController = wire[MainController]
  lazy val authController = wire[AuthController]

  lazy val sessionDao = wire[SessionDao]
  lazy val userDao = wire[UserDao]

  lazy val userService = wire[UserService]
  lazy val authService = wire[AuthService]
  lazy val userAuthAction = wire[UserAuthAction]
  lazy val userAwareAction = wire[UserAwareAction]

  override lazy val dynamicEvolutions = new DynamicEvolutions

  applicationLifecycle.addStopHook { () =>
    DBs.closeAll()
    Future.successful(Unit)
  }

  val onStart = {
    DBs.setupAll()
    applicationEvolutions
  }
}
