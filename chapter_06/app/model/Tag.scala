package model

import java.util.UUID



/**
  * Created by denis on 11/27/16.
  */
case class Tag(id: UUID, text: String)

import play.api.libs.json.Json
object Tag {
  implicit val writes = Json.writes[Tag]
}
