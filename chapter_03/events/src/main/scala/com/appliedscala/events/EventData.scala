package com.appliedscala.events

import play.api.libs.json.JsValue

/**
  * Created by denis on 11/27/16.
  */
trait EventData {
  def action: String
  def json: JsValue
}
