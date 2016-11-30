package dao

import java.util.UUID

import model.UserSession
import scalikejdbc._

import scala.util.Try


class SessionDao {

  def insertSession(userSession: UserSession): Try[UUID] = Try {
    NamedDB('auth).localTx { implicit session =>
      sql"""insert into sessions values(${userSession.sessionId},
        ${userSession.token}, ${userSession.userId},
        ${userSession.created}, ${userSession.updated},
        ${userSession.ttl})""".update().apply()
      userSession.sessionId
    }
  }

  def findSession(token: String): Try[Option[UserSession]] = Try {
    NamedDB('auth).readOnly { implicit session =>
      sql"select * from sessions where token = $token".
        map(UserSession.fromDb).headOption().apply()
    }
  }

  def deleteSession(token: String): Try[Unit] = Try {
    NamedDB('auth).localTx { implicit session =>
      sql"delete from sessions where token = $token".update().apply()
    }
  }
}
