package services

import com.appliedscala.events.LogRecord
import dao.Neo4JReadDao
import model.{Question, QuestionThread, Tag}

import java.util.UUID
import scala.concurrent.Future

class ReadService(readDao: Neo4JReadDao, userService: UserService) {
  import util.ThreadPools.CPU

  def getAllTags: Future[Seq[Tag]] = {
    readDao.getAllTags
  }

  def getAllQuestions: Future[Seq[Question]] = {
    val namesF = userService.getUserFullNameMap
    val questionsF = readDao.getQuestions
    for {
      names <- namesF
      questions <- questionsF
    } yield {
      questions.map { question =>
        question.copy(authorFullName = names.get(question.authorId))
      }
    }
  }

  def getQuestionThread(questionId: UUID): Future[Option[QuestionThread]] = {
    val maybeThreadF = readDao.getQuestionThread(questionId)
    val namesF = userService.getUserFullNameMap
    for {
      names <- namesF
      maybeThread <- maybeThreadF
    } yield {
      maybeThread.map { thread =>
        val sourceQuestion = thread.question
        val sourceAnswers = thread.answers
        val updatedQuestion = sourceQuestion.copy(authorFullName =
          names.get(sourceQuestion.authorId))
        val updatedAnswers = sourceAnswers.map { answer =>
          answer.copy(authorFullName = names.get(answer.authorId))
        }
        QuestionThread(updatedQuestion, updatedAnswers)
      }
    }
  }

  def adjustState(event: LogRecord): Future[Unit] = {
    readDao.processEvent(event)
  }
}
