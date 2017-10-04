package util

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

/**
  * Created by denis on 12/10/16.
  */
object BaseTypes {
  private val ISO8601Format = ISODateTimeFormat.dateTime()

  def formatISO8601(ts: DateTime): String = ISO8601Format.print(ts)

  def parseISO8601(str: String): DateTime = ISO8601Format.parseDateTime(str)
}
