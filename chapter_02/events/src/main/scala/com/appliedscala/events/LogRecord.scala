package com.appliedscala.events

import java.util.UUID
import java.time.ZonedDateTime
import play.api.libs.json.JsValue

/**
  * Created by denis on 11/27/16.
  */
case class LogRecord(id: UUID, action: String, data: JsValue, timestamp: ZonedDateTime)
