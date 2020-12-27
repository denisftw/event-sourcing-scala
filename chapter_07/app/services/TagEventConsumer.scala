package services

import actors.WSStreamActor
import akka.actor.ActorSystem
import com.appliedscala.events.LogRecord
import dao.Neo4JReadDao
import model.ServerSentMessage
import util.{IMessageConsumer, IMessageProcessingRegistry}


/**
  * Created by denis on 12/3/16.
  */
class TagEventConsumer(neo4JReadDao: Neo4JReadDao, actorSystem: ActorSystem,
    registry: IMessageProcessingRegistry) extends IMessageConsumer {

  registry.registerConsumer("read.tags", this)

  override def messageReceived(event: Array[Byte]): Unit = {
    val maybeLogRecord = LogRecord.decode(event)
    maybeLogRecord.foreach(adjustReadState)
  }

  private def adjustReadState(logRecord: LogRecord): Unit = {
    neo4JReadDao.handleEvent(logRecord)
    val tagsT = neo4JReadDao.getAllTags
    tagsT.foreach { tags =>
      val update = ServerSentMessage.create("tags", tags)
      val esActor = actorSystem.actorSelection(WSStreamActor.pathPattern)
      esActor ! WSStreamActor.DataUpdated(update.json)
    }
  }
}
