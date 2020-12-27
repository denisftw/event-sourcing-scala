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
    Json.toJson(this)(LogRecord.writes).toString().getBytes
  }
}

object LogRecord {
  val writes = Json.writes[LogRecord]
  val reads = Json.reads[LogRecord]
  def decode(bytes: Array[Byte]): Option[LogRecord] = {
    Json.parse(bytes).asOpt[LogRecord](reads)
  }
  def fromEvent(eventData: EventData): LogRecord = {
    LogRecord(UUID.randomUUID(), eventData.action,
      eventData.json, ZonedDateTime.now())
  }
}