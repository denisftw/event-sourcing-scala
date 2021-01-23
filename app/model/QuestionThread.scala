package model

import play.api.libs.json.Json

/**
  * Created by denis on 12/23/16.
  */
case class QuestionThread(question: Question, answers: Seq[Answer])

object QuestionThread {
  implicit val writes = Json.writes[QuestionThread]
}
