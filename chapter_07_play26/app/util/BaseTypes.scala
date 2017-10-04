package util

import java.time.format.DateTimeFormatter
import java.time.{ZonedDateTime => DateTime}


//import org.joda.time.format.ISODateTimeFormat

/**
  * Created by denis on 12/10/16.
  */
object BaseTypes {
  private val ISO8601Format = DateTimeFormatter.ISO_DATE_TIME

  def formatISO8601(ts: DateTime): String = ISO8601Format.format(ts)

  def parseISO8601(str: String): DateTime = DateTime.parse(str, ISO8601Format)
}
