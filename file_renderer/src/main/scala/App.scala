import renderer.TemplateRendererFactory

object App extends App {
  val template = "Hello, World!"
  val format = "html" // Change to "pdf" or "csv" as needed

  val renderer = TemplateRendererFactory.getRenderer(format)
  val renderedOutput = renderer.render(template)

  println(renderedOutput)
}
