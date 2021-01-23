package com.appliedscala.events.user

import java.util.UUID

import com.appliedscala.events.EventData
import play.api.libs.json.{Json, Reads}

/**
  * Created by denis on 12/11/16.
  */
case class UserActivated(id: UUID) extends EventData {
  override def action = UserActivated.actionName
  override def json = Json.writes[UserActivated].writes(this)
}

object UserActivated {
  val actionName = "user-activated"
  implicit val reads: Reads[UserActivated] = Json.reads[UserActivated]
}