package services

import java.util.UUID

import dao.UserDao

import scala.util.{Failure, Success}


class UserService(userDao: UserDao) {
  def getUserFullName(userId: UUID): Option[String] = {
    userDao.findById(userId) match {
      case Success(maybeUser) => maybeUser.map(_.fullName)
      case Failure(_) => None
    }
  }
}
