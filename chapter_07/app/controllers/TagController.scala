package controllers

import play.api.libs.json.Json
import security.UserAuthAction
import play.api.mvc.{AbstractController, ControllerComponents}
import services.{ReadService, TagEventProducer}
import scala.concurrent.Future

class TagController(components: ControllerComponents, tagEventProducer: TagEventProducer,
    userAuthAction: UserAuthAction, readService: ReadService) extends AbstractController(components) {

  import util.ThreadPools.CPU

  def createTag() = userAuthAction.async { implicit request =>
    createTagForm.bindFromRequest().fold(
      _ => Future.successful(BadRequest),
      data => {
        tagEventProducer.createTag(data.text, request.user.userId).map(_ => Ok)
      }
    )
  }

  def deleteTag() = userAuthAction.async { implicit request =>
    deleteTagForm.bindFromRequest().fold(
      _ => Future.successful(BadRequest),
      data => {
        tagEventProducer.deleteTag(data.id, request.user.userId).map(_ => Ok)
      }
    )
  }

  def getTags() = Action.async { implicit request =>
    readService.getAllTags.map { tags =>
      Ok(Json.toJson(tags))
    }
  }

  import play.api.data.Form
  import play.api.data.Forms._
  private val createTagForm = Form {
    mapping(
      "text" -> nonEmptyText
    )(CreateTagData.apply)(CreateTagData.unapply)
  }

  private val deleteTagForm = Form {
    mapping(
      "id" -> uuid
    )(DeleteTagData.apply)(DeleteTagData.unapply)
  }

  import java.util.UUID
  case class CreateTagData(text: String)
  case class DeleteTagData(id: UUID)
}
