package model

import java.util.UUID

import org.joda.time.DateTime
import scalikejdbc.WrappedResultSet


case class UserSession(sessionId: UUID, token: String, userId: UUID,
                       created: DateTime, updated: DateTime, ttl: Long)

object UserSession {
  def fromDb(rs: WrappedResultSet): UserSession = {
    UserSession(UUID.fromString(rs.string("session_id")),
      rs.string("token"), UUID.fromString(rs.string("user_id")),
      rs.jodaDateTime("created"), rs.jodaDateTime("updated"), rs.long("ttl"))
  }

  def create(user: User, token: String, seconds: Long): UserSession = {
    val time = DateTime.now()
    UserSession(UUID.randomUUID(), token, user.userId, time, time, seconds)
  }
}
