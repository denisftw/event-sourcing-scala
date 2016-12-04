package model

import play.api.libs.json.{JsValue, Json, Writes}

/**
  * Created by denis on 12/2/16.
  */
case class ServerSentMessage(updateType: String, updateData: JsValue) {
  def json: JsValue = Json.toJson(this)(ServerSentMessage.writes)
}

object ServerSentMessage {
  val writes = Json.writes[ServerSentMessage]
  def create[T](updateType: String, updateData: T)
               (implicit encoder: Writes[T]) =
    ServerSentMessage(updateType, encoder.writes(updateData))
}
