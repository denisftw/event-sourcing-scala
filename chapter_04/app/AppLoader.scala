import play.api.ApplicationLoader.Context
import play.api._
import play.api.db.{DBComponents, HikariCPComponents}
import play.api.db.evolutions.{DynamicEvolutions, EvolutionsComponents}
import play.api.routing.Router
import router.Routes
import com.softwaremill.macwire._
import _root_.controllers._
import dao._
import play.api.mvc.DefaultControllerComponents
import scalikejdbc.config.DBs
import security.{UserAuthAction, UserAwareAction}
import services._
import util.EventValidator

import scala.concurrent.Future

class AppLoader extends ApplicationLoader {
  def load(context: Context) = {
    LoggerConfigurator(context.environment.classLoader).foreach { configurator =>
      configurator.configure(context.environment)
    }
    new AppComponents(context).application
  }
}

class AppComponents(context: Context) extends BuiltInComponentsFromContext(context)
  with EvolutionsComponents with DBComponents
  with HikariCPComponents with AssetsComponents {

  val log = Logger(this.getClass)

  override lazy val controllerComponents = wire[DefaultControllerComponents]
  lazy val prefix: String = "/"
  lazy val router: Router = wire[Routes]
  lazy val maybeRouter = Option(router)

  override lazy val httpErrorHandler = wire[ProdErrorHandler]
  override lazy val httpFilters = Seq()

  lazy val mainController = wire[MainController]
  lazy val authController = wire[AuthController]

  lazy val tagController = wire[TagController]
  lazy val logDao = wire[LogDao]
  lazy val readService = wire[ReadService]

  lazy val tagEventProducer = wire[TagEventProducer]
  lazy val tagEventConsumer = wire[TagEventConsumer]

  lazy val userEventConsumer = wire[UserEventConsumer]
  lazy val userEventProducer = wire[UserEventProducer]

  lazy val logRecordConsumer = wire[LogRecordConsumer]
  lazy val consumerAggregator = wire[ConsumerAggregator]

  lazy val sessionDao = wire[SessionDao]
  lazy val userDao = wire[UserDao]
  lazy val neo4JReadDao = wire[Neo4JReadDao]
  lazy val neo4JQueryExecutor = wire[Neo4JQueryExecutor]

  lazy val eventValidator = wire[EventValidator]
  lazy val validationService = wire[ValidationService]
  lazy val rewindService = wire[RewindService]
  lazy val userService = wire[UserService]
  lazy val authService = wire[AuthService]
  lazy val userAuthAction = wire[UserAuthAction]
  lazy val userAwareAction = wire[UserAwareAction]

  override lazy val dynamicEvolutions = new DynamicEvolutions

  applicationLifecycle.addStopHook { () =>
    DBs.closeAll()
    Future.successful(Unit)
  }

  val onStart: Unit = {
    DBs.setupAll()
    applicationEvolutions
  }
}
