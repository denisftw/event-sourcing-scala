package com.appliedscala.events

import java.util.UUID
import java.time.ZonedDateTime
import play.api.libs.json.{JsValue, Json}


/**
  * Created by denis on 11/27/16.
  */
case class LogRecord(id: UUID, action: String,
                     data: JsValue, timestamp: ZonedDateTime) {
  def encode: Array[Byte] = {
    Json.toJson(this)(LogRecord.format).toString().getBytes
  }
}

object LogRecord {
  val format = Json.format[LogRecord]
  def decode(bytes: Array[Byte]): LogRecord = {
    Json.parse(bytes).as[LogRecord](format)
  }
  def fromEvent(eventData: EventData): LogRecord = {
    LogRecord(UUID.randomUUID(), eventData.action,
      eventData.json, ZonedDateTime.now())
  }
}