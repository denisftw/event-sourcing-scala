package com.appliedscala.events.answer

import java.util.UUID

import com.appliedscala.events.EventData
import play.api.libs.json.{JsValue, Json, Reads}

/**
  * Created by denis on 12/23/16.
  */
case class AnswerDownvoted(answerId: UUID, questionId: UUID,
    userId: UUID) extends EventData {
  override def action: String = AnswerDownvoted.actionName
  override def json: JsValue = Json.writes[AnswerDownvoted].writes(this)
}

object AnswerDownvoted {
  val actionName = "answer-downvoted"
  implicit val reads: Reads[AnswerDownvoted] = Json.reads[AnswerDownvoted]
}