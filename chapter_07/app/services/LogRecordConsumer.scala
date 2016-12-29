package services


import akka.actor.ActorSystem
import akka.stream.Materializer
import com.appliedscala.events.LogRecord
import dao.LogDao
import play.api.Configuration
import util.ServiceKafkaConsumer

/**
  * Created by denis on 12/4/16.
  */
class LogRecordConsumer(logDao: LogDao, actorSystem: ActorSystem,
    configuration: Configuration, materializer: Materializer) {

  val topics = Seq("tags", "users", "questions" ,"answers").toSet
  val serviceKafkaConsumer = new ServiceKafkaConsumer(topics,
    "log", materializer, actorSystem, configuration, handleEvent)

  private def handleEvent(event: String): Unit = {
    val maybeGenericEnvelope = LogRecord.decode(event)
    maybeGenericEnvelope.foreach { envelope =>
      logDao.insertLogRecord(envelope)
    }
  }
}