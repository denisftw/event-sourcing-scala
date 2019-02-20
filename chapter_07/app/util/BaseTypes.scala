package util

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

import play.api.data.Forms.of
import play.api.data.Mapping
import play.api.data.format.Formats.parsing
import play.api.data.format.Formatter
import play.api.libs.json.{Format, JsString, JsValue, Writes}

/**
  * Created by denis on 12/10/16.
  */
object BaseTypes {
  private val ISO8601Format = DateTimeFormatter.ISO_OFFSET_DATE_TIME

  def formatISO8601(ts: ZonedDateTime): String = ISO8601Format.format(ts)

  def parseISO8601(str: String): ZonedDateTime =
    ZonedDateTime.from(ISO8601Format.parse(str))

  implicit val zonedDateTimeWrites = new Writes[ZonedDateTime] {
    override def writes(o: ZonedDateTime) = JsString(formatISO8601(o))
  }

  implicit val zonedDateTimeFormatter = new Formatter[ZonedDateTime] {
    def bind(key: String, data: Map[String, String]) =
      parsing(parseISO8601, "error.zonedDateTime", Nil)(key, data)
    def unbind(key: String, value: ZonedDateTime) = Map(key ->
      formatISO8601(value))
  }

  val zonedDateTimeMapping: Mapping[ZonedDateTime] = of[ZonedDateTime]
}
