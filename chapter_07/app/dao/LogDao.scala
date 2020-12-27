package dao


import akka.NotUsed
import akka.stream.scaladsl.Source

import java.util.UUID
import com.appliedscala.events.LogRecord

import java.time.ZonedDateTime
import play.api.libs.json.Json
import scalikejdbc._

import scala.concurrent.Future
import scala.util.Try

/**
  * Created by denis on 11/27/16.
  */
class LogDao {
  import util.ThreadPools.IO

  def insertLogRecord(event: LogRecord): Try[Unit] = Try {
    NamedDB(Symbol("eventstore")).localTx { implicit session =>
      val jsonStr = event.data.toString()
      sql"""insert into logs(record_id, action_name, event_data, timestamp)
            values(${event.id}, ${event.action}, $jsonStr, ${event.timestamp})""".
        update().apply()
    }
  }

  def getLogRecordStream(maybeUpTo: Option[ZonedDateTime]): Future[Source[LogRecord, NotUsed]] = Future {
    import scalikejdbc.streams._
    val upTo = maybeUpTo.getOrElse(ZonedDateTime.now())
    val publisher = NamedDB(Symbol("eventstore")).readOnlyStream {
      sql"""select * from logs where timestamp <= $upTo order by timestamp""".
        map(rs2LogRecord).iterator
    }
    Source.fromPublisher(publisher)
  }

  private def rs2LogRecord(rs: WrappedResultSet): LogRecord = {
    LogRecord(UUID.fromString(rs.string("record_id")),
      rs.string("action_name"), Json.parse(rs.string("event_data")),
      rs.dateTime("timestamp"))
  }
}

