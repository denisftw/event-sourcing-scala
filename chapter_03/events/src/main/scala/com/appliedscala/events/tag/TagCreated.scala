package com.appliedscala.events.tag

import java.util.UUID

import com.appliedscala.events.{EventData, LogRecord}
import play.api.libs.json.{JsValue, Json, Reads}

/**
  * Created by denis on 11/27/16.
  */
case class TagCreated(id: UUID, text: String, createdBy: UUID) extends EventData {
  override def action: String = TagCreated.actionName
  override def json: JsValue = Json.writes[TagCreated].writes(this)
}

object TagCreated {
  val actionName = "tag-created"
  implicit val reads: Reads[TagCreated] = Json.reads[TagCreated]
}
