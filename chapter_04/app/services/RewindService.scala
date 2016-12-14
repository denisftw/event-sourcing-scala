package services

import actors.ValidationActor

import dao.{LogDao, Neo4JReadDao}




/**
  * Created by denis on 12/12/16.
  */
import akka.actor.ActorSystem
class RewindService(actorSystem: ActorSystem,
    neo4JReadDao: Neo4JReadDao, logDao: LogDao) {

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
        Logger.info("The state has been recreated from events")
      case Failure(th) =>
        Logger.error("Error occurred while rewinding the state", th)
    }
  }
}
