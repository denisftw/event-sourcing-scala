package com.appliedscala.events.answer

import java.util.UUID

import com.appliedscala.events.EventData
import play.api.libs.json.{JsValue, Json, Reads}

/**
  * Created by denis on 12/23/16.
  */
case class AnswerDeleted(answerId: UUID, questionId: UUID,
                        deletedBy: UUID) extends EventData {
  override def action: String = AnswerDeleted.actionName
  override def json: JsValue = Json.writes[AnswerDeleted].writes(this)
}

object AnswerDeleted {
  val actionName = "answer-deleted"
  implicit val reads: Reads[AnswerDeleted] = Json.reads[AnswerDeleted]
}