package dao

import java.util.UUID
import model.User
import org.mindrot.jbcrypt.BCrypt
import scalikejdbc._

import scala.concurrent.Future


class UserDao {

  import util.ThreadPools.IO
  def findById(userId: UUID): Future[Option[User]] = Future {
    NamedDB(Symbol("auth")).readOnly { implicit session =>
      sql"select * from users where user_id = $userId".
        map(User.fromRS).headOption().apply()
    }
  }

  def getUsers: Future[Seq[User]] = Future {
    NamedDB(Symbol("auth")).readOnly { implicit session =>
      sql"select * from users".map(User.fromRS).list().apply()
    }
  }

  def insertUser(userCode: String, fullName: String,
    password: String): Future[User] = Future {
    val passwordHash = BCrypt.hashpw(password, BCrypt.gensalt())
    val user = User(UUID.randomUUID(), userCode, fullName, passwordHash, isAdmin = false)
    NamedDB(Symbol("auth")).localTx { implicit session =>
      sql"""insert into users(user_id, user_code, full_name, password, is_admin)
       values (${user.userId}, ${user.userCode}, ${user.fullName},
       ${user.password}, false)""".update().apply()
    }
    user
  }

  def checkUser(userCode: String, password: String): Future[User] = Future {
    NamedDB(Symbol("auth")).readOnly { implicit session =>
      val maybeUser = sql"select * from users where user_code = $userCode".
        map(User.fromRS).single().apply()
      maybeUser match {
        case Some(user) =>
          if (BCrypt.checkpw(password, user.password)) user
          else throw new Exception("Password doesn't match")
        case None => throw new Exception("User is not found in the database")
      }
    }
  }
}
