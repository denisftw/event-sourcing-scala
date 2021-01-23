package com.appliedscala.events.answer

import java.util.UUID

import com.appliedscala.events.EventData
import java.time.ZonedDateTime
import play.api.libs.json.{JsValue, Json, Reads}

/**
  * Created by denis on 12/23/16.
  */
case class AnswerCreated(answerId: UUID, answerText: String, questionId: UUID,
    createdBy: UUID, created: ZonedDateTime) extends EventData {
  override def action: String = AnswerCreated.actionName
  override def json: JsValue = Json.writes[AnswerCreated].writes(this)
}

object AnswerCreated {
  val actionName = "answer-created"
  implicit val reads: Reads[AnswerCreated] = Json.reads[AnswerCreated]
}