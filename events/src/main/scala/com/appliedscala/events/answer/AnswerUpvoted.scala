package com.appliedscala.events.answer

import java.util.UUID

import com.appliedscala.events.EventData
import play.api.libs.json.{JsValue, Json, Reads}

/**
  * Created by denis on 12/23/16.
  */
case class AnswerUpvoted(answerId: UUID, questionId: UUID,
    userId: UUID) extends EventData {
  override def action: String = AnswerUpvoted.actionName
  override def json: JsValue = Json.writes[AnswerUpvoted].writes(this)
}

object AnswerUpvoted {
  val actionName = "answer-upvoted"
  implicit val reads: Reads[AnswerUpvoted] = Json.reads[AnswerUpvoted]
}