package services

import dao.Neo4JReadDao
import model.{Question, Tag}

import scala.util.Try

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
}
