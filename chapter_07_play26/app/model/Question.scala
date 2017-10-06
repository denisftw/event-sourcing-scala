package model

import java.util.UUID

import java.time.ZonedDateTime
import play.api.libs.json.Json

case class QuestionNaive(id: UUID, title: String,
    details: Option[String], tags: Seq[UUID],
    created: ZonedDateTime, authorId: UUID)


case class Question(id: UUID, title: String,
    details: Option[String], tags: Seq[Tag],
    created: ZonedDateTime, authorId: UUID,
    authorFullName: Option[String])

object Question {
  implicit val writes = Json.writes[Question]
}