package services

import java.util.UUID

import dao.Neo4JReadDao
import model.{Question, QuestionThread, Tag}

import scala.util.{Failure, Success, Try}

class ReadService(neo4JReadDao: Neo4JReadDao, userService: UserService) {

  def getAllTags: Try[Seq[Tag]] = {
    neo4JReadDao.getAllTags
  }

  def getAllQuestions: Try[Seq[Question]] = {
    val namesT = userService.getUserFullNameMap
    val questionsT = neo4JReadDao.getQuestions
    for {
      names <- namesT
      questions <- questionsT
    } yield {
      questions.map { question =>
        question.copy(authorFullName = names.get(question.authorId))
      }
    }
  }

  def getQuestionThread(questionId: UUID): Try[Option[QuestionThread]] = {
    val maybeThreadT = neo4JReadDao.getQuestionThread(questionId)
    val namesT = userService.getUserFullNameMap
    for {
      names <- namesT
      maybeThread <- maybeThreadT
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
}
