package model

import java.time.ZonedDateTime
import java.util.UUID

import scalikejdbc.WrappedResultSet


case class UserSession(sessionId: UUID, token: String, userId: UUID,
                       created: ZonedDateTime, updated: ZonedDateTime, ttl: Long)

object UserSession {
  def fromDb(rs: WrappedResultSet): UserSession = {
    UserSession(UUID.fromString(rs.string("session_id")),
      rs.string("token"), UUID.fromString(rs.string("user_id")),
      rs.dateTime("created"), rs.dateTime("updated"), rs.long("ttl"))
  }

  def create(user: User, token: String, seconds: Long): UserSession = {
    val time = ZonedDateTime.now()
    UserSession(UUID.randomUUID(), token, user.userId, time, time, seconds)
  }
}
