package renderer

class CsvRenderer extends TemplateRenderer {
  override def render(template: String): String = {
    // Implement CSV rendering logic here
    template.split("\n").map(_.split(",").mkString(",")).mkString("\n")
  }
}
