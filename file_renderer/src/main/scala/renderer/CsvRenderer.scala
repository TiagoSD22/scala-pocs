package renderer

class CsvRenderer extends TemplateRenderer {
  override def render(template: String): String = {
    template.split("\n").map(_.split(",").mkString(",")).mkString("\n")
  }
}
