package model

import play.api.libs.json.Json

import java.util.UUID

case class QuestionThreadIdChanged(questionThreadId: UUID)
object QuestionThreadIdChanged {
  implicit val reads = Json.reads[QuestionThreadIdChanged]
}