package util

import java.time.format.DateTimeFormatter
import java.time.{ZoneId, ZonedDateTime}

import play.api.data.Forms.of
import play.api.data.Mapping
import play.api.data.format.Formats.parsing
import play.api.data.format.Formatter


/**
  * Created by denis on 12/10/16.
  */
object BaseTypes {
  private val ISO8601Format = DateTimeFormatter.ISO_DATE_TIME

  def formatISO8601(ts: ZonedDateTime): String = ISO8601Format.format(ts)

  def parseISO8601(str: String): ZonedDateTime =
    ZonedDateTime.parse(str, ISO8601Format)

  def dateTimeNow: ZonedDateTime = ZonedDateTime.now(ZoneId.of("UTC"))

  implicit val zonedDateTimeFormatter = new Formatter[ZonedDateTime] {
    def bind(key: String, data: Map[String, String]) =
      parsing(parseISO8601, "error.zonedDateTime", Nil)(key, data)
    def unbind(key: String, value: ZonedDateTime) = Map(key ->
      formatISO8601(value))
  }

  val zonedDateTimeMapping: Mapping[ZonedDateTime] = of[ZonedDateTime]
}
