package services

import actors.ValidationActor
import dao.{LogDao, Neo4JReadDao}
import play.api.Logger




/**
  * Created by denis on 12/12/16.
  */
import akka.actor.ActorSystem
class RewindService(actorSystem: ActorSystem,
    neo4JReadDao: Neo4JReadDao, logDao: LogDao) {

  val log = Logger(this.getClass)

  import play.api.Logger
  import scala.util.{Failure, Success}
  def refreshState(): Unit = {
    val eventsT = logDao.getLogRecords
    eventsT match {
      case Success(events) =>
        val validationActor = actorSystem.actorSelection(ValidationActor.path)
        validationActor ! ValidationActor.RefreshStateCommand(
          events, fromScratch = true)
        neo4JReadDao.refreshState(events, fromScratch = true)
        log.info("The state has been recreated from events")
      case Failure(th) =>
        log.error("Error occurred while rewinding the state", th)
    }
  }
}
