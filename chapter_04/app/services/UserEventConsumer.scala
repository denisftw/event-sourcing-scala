package services

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.appliedscala.events.LogRecord
import dao.Neo4JReadDao
import play.api.Configuration
import util.ServiceKafkaConsumer

/**
  * Created by denis on 12/26/16.
  */
class UserEventConsumer(neo4JReadDao: Neo4JReadDao, actorSystem: ActorSystem,
    configuration: Configuration, materializer: Materializer) {

  val topicName = "users"
  val serviceKafkaConsumer = new ServiceKafkaConsumer(Set(topicName),
    "read", materializer, actorSystem, configuration, handleEvent)

  private def handleEvent(event: String): Unit = {
    val maybeLogRecord = LogRecord.decode(event)
    maybeLogRecord.foreach(adjustReadState)
  }

  private def adjustReadState(logRecord: LogRecord): Unit = {
    neo4JReadDao.handleEvent(logRecord)
  }
}
