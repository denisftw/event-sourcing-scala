package model

import java.util.UUID
import scalikejdbc.WrappedResultSet

case class User(userId: UUID, userCode: String, fullName: String,
                password: String, isAdmin: Boolean) {
  def urlCode: String = userId.toString.split("-")(0)
}

object User {
  def fromRS(rs: WrappedResultSet): User = {
    User(UUID.fromString(rs.string("user_id")),
      rs.string("user_code"), rs.string("full_name"),
      rs.string("password"), rs.boolean("is_admin"))
  }
}
