import renderer.TemplateRendererFactory
import scala.util.{Try, Success, Failure}

object App extends App {
  val template = "Hello, World!"
  val format = "html"

  val renderer: Unit = Try(TemplateRendererFactory.getRenderer(format)) match {
    case Success(r) =>
      val renderedOutput = r.render(template)
      println(renderedOutput)
    case Failure(e) =>
      println(s"Error: ${e.getMessage}")
      sys.exit(1)
  }
}
