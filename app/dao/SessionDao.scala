package dao

import java.util.UUID
import model.UserSession
import scalikejdbc._

import scala.concurrent.Future


class SessionDao {

  import util.ThreadPools.IO
  def insertSession(userSession: UserSession): Future[UUID] = Future {
    NamedDB(Symbol("auth")).localTx { implicit session =>
      sql"""insert into sessions values(${userSession.sessionId},
        ${userSession.token}, ${userSession.userId},
        ${userSession.created}, ${userSession.updated},
        ${userSession.ttl})""".update().apply()
      userSession.sessionId
    }
  }

  def findSession(token: String): Future[Option[UserSession]] = Future {
    NamedDB(Symbol("auth")).readOnly { implicit session =>
      sql"select * from sessions where token = $token".
        map(UserSession.fromDb).headOption().apply()
    }
  }

  def deleteSession(token: String): Future[Unit] = Future {
    NamedDB(Symbol("auth")).localTx { implicit session =>
      sql"delete from sessions where token = $token".update().apply()
    }
  }
}
