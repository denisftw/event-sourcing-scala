package controllers

/**
  * Created by denis on 12/14/16.
  */

import java.util.UUID

import play.api.libs.json.Json
import security.UserAuthAction
import play.api.mvc.{AbstractController, ControllerComponents}
import services.{QuestionEventProducer, ReadService}

import scala.concurrent.Future


class QuestionController(components: ControllerComponents, questionEventProducer: QuestionEventProducer,
    userAuthAction: UserAuthAction, readService: ReadService) extends AbstractController(components) {

  import util.ThreadPools.CPU

  def createQuestion() = userAuthAction.async { implicit request =>
    createQuestionForm.bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest),
      data => {
        val resultF = questionEventProducer.createQuestion(
          data.title, data.details, data.tags, request.user.userId)
        resultF.map {
          case Some(error) => InternalServerError
          case None => Ok
        }
      }
    )
  }

  def deleteQuestion() = userAuthAction.async { implicit request =>
    deleteQuestionForm.bindFromRequest().fold(
      _ => Future.successful(BadRequest),
      data => {
        questionEventProducer.deleteQuestion(data.id, request.user.userId).map { _ =>
          Ok
        }
      }
    )
  }

  import scala.util.{Failure, Success}
  def getQuestions = Action.async { implicit request =>
    readService.getAllQuestions.map { questions =>
      Ok(Json.toJson(questions))
    }
  }

  def getQuestionThread(questionId: UUID) = Action.async {
    readService.getQuestionThread(questionId).map {
      case Some(thread) => Ok(Json.toJson(thread))
      case None => NotFound
    }
  }

  import play.api.data.Form
  import play.api.data.Forms._
  val createQuestionForm = Form {
    mapping(
      "title" -> nonEmptyText,
      "details" -> optional(text),
      "tags" -> seq(uuid)
    )(CreateQuestionData.apply)(CreateQuestionData.unapply)
  }

  val deleteQuestionForm = Form {
    mapping(
      "id" -> uuid
    )(DeleteQuestionData.apply)(DeleteQuestionData.unapply)
  }

  import java.util.UUID
  case class CreateQuestionData(title: String,
      details: Option[String], tags: Seq[UUID])
  case class DeleteQuestionData(id: UUID)
}
