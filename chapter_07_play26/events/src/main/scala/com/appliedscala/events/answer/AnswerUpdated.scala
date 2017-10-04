package com.appliedscala.events.answer

import java.util.UUID

import com.appliedscala.events.EventData
import java.time.{ZonedDateTime => DateTime}
import play.api.libs.json.{JsValue, Json, Reads}

/**
  * Created by denis on 12/23/16.
  */
case class AnswerUpdated(answerId: UUID, answerText: String,
    questionId: UUID, updatedBy: UUID, updated: DateTime) extends EventData {
  override def action: String = AnswerUpdated.actionName
  override def json: JsValue = Json.writes[AnswerUpdated].writes(this)
}

object AnswerUpdated {
  val actionName = "answer-updated"
  implicit val reads: Reads[AnswerUpdated] = Json.reads[AnswerUpdated]
}