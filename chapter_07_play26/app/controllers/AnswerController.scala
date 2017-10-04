package controllers

import java.util.UUID

import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{AbstractController, ControllerComponents}
import security.UserAuthAction
import services.AnswerEventProducer

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Created by denis on 12/23/16.
  */
class AnswerController(controllerComponents: ControllerComponents, userAuthAction: UserAuthAction,
    answerEventProducer: AnswerEventProducer) extends AbstractController(controllerComponents) {

  def createAnswer() = userAuthAction.async { implicit request =>
    createAnswerForm.bindFromRequest.fold(
      formWithErrors =>
        Future.successful(BadRequest),
      data => {
        answerEventProducer.createAnswer(data.questionId,
          data.answerText, request.user.userId).map {
          case Some(error) => InternalServerError
          case None => Ok
        }
      }
    )
  }

  def deleteAnswer() = userAuthAction.async { implicit request =>
    deleteAnswerForm.bindFromRequest.fold(
      formWithErrors =>
        Future.successful(BadRequest),
      data => {
        answerEventProducer.deleteAnswer(data.questionId,
          data.answerId, request.user.userId).map {
          case Some(error) => InternalServerError
          case None => Ok
        }
      }
    )
  }

  def updateAnswer() = userAuthAction.async { implicit request =>
    updateAnswerForm.bindFromRequest.fold(
      formWithErrors =>
        Future.successful(BadRequest),
      data => {
        answerEventProducer.updateAnswer(data.questionId,
          data.answerId, request.user.userId, data.answerText).map {
          case Some(error) => InternalServerError
          case None => Ok
        }
      }
    )
  }

  def upvoteAnswer() = userAuthAction.async { implicit request =>
    upvoteAnswerForm.bindFromRequest.fold(
      formWithErrors =>
        Future.successful(BadRequest),
      data => {
        answerEventProducer.upvoteAnswer(data.questionId, data.answerId,
          request.user.userId).map {
          case Some(error) => InternalServerError
          case None => Ok
        }
      }
    )
  }

  def downvoteAnswer() = userAuthAction.async { implicit request =>
    upvoteAnswerForm.bindFromRequest.fold(
      formWithErrors =>
        Future.successful(BadRequest),
      data => {
        answerEventProducer.downvoteAnswer(data.questionId, data.answerId,
          request.user.userId).map {
          case Some(error) => InternalServerError
          case None => Ok
        }
      }
    )
  }

  val createAnswerForm = Form {
    mapping(
      "questionId" -> uuid,
      "answerText" -> nonEmptyText
    )(CreateAnswerData.apply)(CreateAnswerData.unapply)
  }

  val deleteAnswerForm = Form {
    mapping(
      "questionId" -> uuid,
      "answerId" -> uuid
    )(DeleteAnswerData.apply)(DeleteAnswerData.unapply)
  }

  val updateAnswerForm = Form {
    mapping(
      "questionId" -> uuid,
      "answerId" -> uuid,
      "answerText" -> nonEmptyText
    )(UpdateAnswerData.apply)(UpdateAnswerData.unapply)
  }

  val upvoteAnswerForm = Form {
    mapping(
      "questionId" -> uuid,
      "answerId" -> uuid
    )(UpvoteAnswerData.apply)(UpvoteAnswerData.unapply)
  }

  case class CreateAnswerData(questionId: UUID, answerText: String)
  case class DeleteAnswerData(questionId: UUID, answerId: UUID)
  case class UpdateAnswerData(questionId: UUID,
      answerId: UUID, answerText: String)
  case class UpvoteAnswerData(questionId: UUID, answerId: UUID)
}
