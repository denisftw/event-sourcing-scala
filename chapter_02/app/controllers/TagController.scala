package controllers



import play.api.libs.json.Json
import security.UserAuthAction
import play.api.mvc.{AbstractController, ControllerComponents}
import services.{ReadService, TagEventProducer}


class TagController(components: ControllerComponents, tagEventProducer: TagEventProducer,
    userAuthAction: UserAuthAction, readService: ReadService) extends AbstractController(components) {

  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.Future
  def createTag() = userAuthAction.async { implicit request =>
    createTagForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest),
      data => {
        tagEventProducer.createTag(data.text, request.user.userId).
          map { tags => Ok(Json.toJson(tags)) }
      }
    )
  }

  {
    import java.util.UUID
    import model.Tag
    import play.api.libs.json.Json

    val tag = Tag(UUID.randomUUID(), "Scala")
    Json.toJson(tag)
  }

  def deleteTag() = userAuthAction.async { implicit request =>
    deleteTagForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest),
      data => {
        tagEventProducer.deleteTag(data.id, request.user.userId).
          map { tags => Ok(Json.toJson(tags)) }
      }
    )
  }

  import play.api.mvc.Action
  def getTags = Action.async { implicit request =>
    val tagsF = readService.getTags
    tagsF.map { tags => Ok(Json.toJson(tags)) }
  }

  import play.api.data.Form
  import play.api.data.Forms._
  val createTagForm = Form {
    mapping(
      "text" -> nonEmptyText
    )(CreateTagData.apply)(CreateTagData.unapply)
  }

  val deleteTagForm = Form {
    mapping(
      "id" -> uuid
    )(DeleteTagData.apply)(DeleteTagData.unapply)
  }

  import java.util.UUID
  case class CreateTagData(text: String)
  case class DeleteTagData(id: UUID)
}
