package services

import java.util.UUID
import dao.UserDao

import scala.concurrent.Future


class UserService(userDao: UserDao) {
  import util.ThreadPools.CPU
  def getUserFullNameMap: Future[Map[UUID, String]] = {
    userDao.getUsers.map { users =>
      users.map { user =>
        user.userId -> user.fullName
      }.toMap
    }
  }
}
