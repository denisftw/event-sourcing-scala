package model


import java.util.UUID
import java.time.ZonedDateTime
import play.api.libs.json.Json

/**
  * Created by denis on 12/23/16.
  */
case class Answer(answerId: UUID, questionId: UUID, answerText: String,
                  authorId: UUID, authorFullName: Option[String],
                  upvotes: Seq[UUID], updated: ZonedDateTime)

object Answer {
  import util.BaseTypes.zonedDateTimeWrites
  implicit val writes = Json.writes[Answer]
}