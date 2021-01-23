package com.appliedscala.events.user

import java.util.UUID

import com.appliedscala.events.EventData
import play.api.libs.json.{Json, Reads}

/**
  * Created by denis on 12/11/16.
  */
case class UserDeactivated(id: UUID) extends EventData {
  override def action = UserDeactivated.actionName
  override def json = Json.writes[UserDeactivated].writes(this)
}

object UserDeactivated {
  val actionName = "user-deactivated"
  implicit val reads: Reads[UserDeactivated] = Json.reads[UserDeactivated]
}