package model

import java.util.UUID

import org.joda.time.DateTime
import play.api.libs.json.Json

case class QuestionNaive(id: UUID, title: String,
    details: Option[String], tags: Seq[UUID],
    created: DateTime, authorId: UUID)


case class Question(id: UUID, title: String,
    details: Option[String], tags: Seq[Tag],
    created: DateTime, authorId: UUID,
    authorFullName: Option[String])

object Question {
  implicit val writes = Json.writes[Question]
}