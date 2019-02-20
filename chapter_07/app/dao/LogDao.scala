package dao


import java.util.UUID
import com.appliedscala.events.LogRecord
import java.time.ZonedDateTime
import play.api.libs.json.Json
import scalikejdbc._

import scala.util.Try

/**
  * Created by denis on 11/27/16.
  */
class LogDao {

  def insertLogRecord(event: LogRecord): Try[Unit] = Try {
    NamedDB('eventstore).localTx { implicit session =>
      val jsonStr = event.data.toString()
      sql"""insert into logs(record_id, action_name, event_data, timestamp)
            values(${event.id}, ${event.action}, $jsonStr, ${event.timestamp})""".
        update().apply()
    }
  }

  import scala.collection.mutable.ListBuffer
  def iterateLogRecords(maybeUpTo: Option[ZonedDateTime])(chunkSize: Int)
                       (handler: (Seq[LogRecord]) => Unit): Try[Unit] = Try {
    NamedDB('eventstore).readOnly { implicit session =>
      val upTo = maybeUpTo.getOrElse(ZonedDateTime.now())
      val buffer = ListBuffer[LogRecord]()
      sql"select * from logs where timestamp <= $upTo order by timestamp".
        foreach { wrs =>
          val event = rs2LogRecord(wrs)
          buffer.append(event)
          if (buffer.size >= chunkSize) {
            handler(buffer.toList)
            buffer.clear()
          }
        }
      if (buffer.nonEmpty) {
        handler(buffer.toList)
      }
    }
  }

  def getLogRecords: Try[Seq[LogRecord]] = Try {
    NamedDB('eventstore).readOnly { implicit session =>
      sql"""select * from logs order by timestamp""".
        map(rs2LogRecord).list().apply()
    }
  }

  private def rs2LogRecord(rs: WrappedResultSet): LogRecord = {
    LogRecord(UUID.fromString(rs.string("record_id")),
      rs.string("action_name"), Json.parse(rs.string("event_data")),
      rs.dateTime("timestamp"))
  }
}

