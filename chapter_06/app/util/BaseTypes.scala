package util

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
  * Created by denis on 12/10/16.
  */
object BaseTypes {
  private val ISO8601Format = DateTimeFormatter.ISO_DATE_TIME

  def formatISO8601(ts: ZonedDateTime): String = ISO8601Format.format(ts)

  def parseISO8601(str: String): ZonedDateTime =
    ZonedDateTime.from(ISO8601Format.parse(str))
}
