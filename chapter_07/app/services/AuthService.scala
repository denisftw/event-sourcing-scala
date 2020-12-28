package services

import java.security.MessageDigest
import java.util.{Base64, UUID}
import java.util.concurrent.TimeUnit
import dao.{SessionDao, UserDao}
import model.{User, UserSession}
import play.api.mvc.{Cookie, RequestHeader}

import scala.concurrent.Future
import scala.concurrent.duration.Duration

class AuthService(sessionDao: SessionDao, userDao: UserDao,
    userEventProducer: UserEventProducer) {

  val mda = MessageDigest.getInstance("SHA-512")
  val cookieHeader = "X-Auth-Token"

  import util.ThreadPools.CPU
  def login(userCode: String, password: String): Future[Cookie] = {
    val userT = userDao.checkUser(userCode, password)
    userT.flatMap { user =>
      createCookie(user)
    }
  }

  def register(userCode: String, fullName: String, password: String): Future[Cookie] = {
    val userT = userDao.insertUser(userCode, fullName, password)
    userT.flatMap { user =>
      userEventProducer.activateUser(user.userId)
      createCookie(user)
    }
  }

  def checkCookie(header: RequestHeader): Future[Option[User]] = {
    val maybeCookie = header.cookies.get(cookieHeader)
    val maybeUserT = maybeCookie match {
      case Some(cookie) =>
        val maybeUserSessionT = sessionDao.findSession(cookie.value)
        maybeUserSessionT.flatMap {
          case Some(userSession) => userDao.findById(userSession.userId)
          case None => Future.successful(None)
        }
      case None => Future.successful(None)
    }
    maybeUserT
  }

  def destroySession(header: RequestHeader): Future[Unit] = {
    val maybeCookie = header.cookies.get(cookieHeader)
    maybeCookie match {
      case Some(cookie) => sessionDao.deleteSession(cookie.value)
      case None => Future.successful(())
    }
  }

  private def createCookie(user: User): Future[Cookie] = {
    val randomPart = UUID.randomUUID().toString.toUpperCase
    val userPart = user.userId.toString.toUpperCase
    val key = s"$randomPart|$userPart"
    val token = Base64.getEncoder.encodeToString(mda.digest(key.getBytes))
    val duration = Duration.create(10, TimeUnit.HOURS)

    val userSession = UserSession.create(user, token, duration.toSeconds)
    val insertT = sessionDao.insertSession(userSession)
    insertT.map { insert =>
      Cookie(cookieHeader, token, maxAge = Some(duration.toSeconds.toInt), httpOnly = true)
    }
  }
}
