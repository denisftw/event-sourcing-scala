package com.appliedscala.events.question

import java.util.UUID

import com.appliedscala.events.EventData
import play.api.libs.json.{JsValue, Json, Reads}

/**
  * Created by denis on 12/5/16.
  */
case class QuestionDeleted(questionId: UUID, deletedBy: UUID) extends EventData {
  override def action: String = QuestionDeleted.actionName
  override def json: JsValue = Json.writes[QuestionDeleted].writes(this)
}

object QuestionDeleted {
  val actionName = "question-deleted"
  implicit val reads: Reads[QuestionDeleted] = Json.reads[QuestionDeleted]
}
