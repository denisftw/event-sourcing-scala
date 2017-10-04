package com.appliedscala.events.question

import java.util.UUID
import com.appliedscala.events.EventData
import java.time.{ZonedDateTime => DateTime}
import play.api.libs.json.{JsValue, Json, Reads}

/**
  * Created by denis on 12/5/16.
  */
case class QuestionCreated(title: String, details: Option[String], tags: Seq[UUID],
    questionId: UUID, createdBy: UUID, created: DateTime) extends EventData {
  override def action: String = QuestionCreated.actionName
  override def json: JsValue = Json.writes[QuestionCreated].writes(this)
}

object QuestionCreated {
  val actionName = "question-created"
  implicit val reads: Reads[QuestionCreated] = Json.reads[QuestionCreated]
}
