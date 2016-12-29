package services

import actors.EventStreamActor
import akka.actor.ActorSystem
import akka.stream.Materializer
import com.appliedscala.events.LogRecord
import dao.Neo4JReadDao
import model.{ServerSentMessage, Tag}
import play.api.Configuration
import util.ServiceKafkaConsumer

import scala.concurrent.Future

/**
  * Created by denis on 12/3/16.
  */
class TagEventConsumer(neo4JReadDao: Neo4JReadDao, actorSystem: ActorSystem,
    configuration: Configuration, materializer: Materializer) {

  val topicName = "tags"
  val serviceKafkaConsumer = new ServiceKafkaConsumer(Set(topicName),
    "read", materializer, actorSystem, configuration, handleEvent)

  private def handleEvent(event: String): Unit = {
    val maybeLogRecord = LogRecord.decode(event)
    maybeLogRecord.foreach(adjustReadState)
  }

  private def adjustReadState(logRecord: LogRecord): Unit = {
    neo4JReadDao.handleEvent(logRecord)
    val tagsT = neo4JReadDao.getAllTags
    tagsT.foreach { tags =>
      val update = ServerSentMessage.create("tags", tags)
      val esActor = actorSystem.actorSelection(EventStreamActor.pathPattern)
      esActor ! EventStreamActor.DataUpdated(update.json)
    }
  }
}
