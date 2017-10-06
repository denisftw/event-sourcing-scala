package model




/**
  * Created by denis on 12/23/16.
  */
import java.util.UUID
import java.time.ZonedDateTime
case class Answer(answerId: UUID, questionId: UUID, answerText: String,
                  authorId: UUID, authorFullName: Option[String],
                  upvotes: Seq[UUID], updated: ZonedDateTime)

import play.api.libs.json.Json
object Answer {
  implicit val writes = Json.writes[Answer]
}