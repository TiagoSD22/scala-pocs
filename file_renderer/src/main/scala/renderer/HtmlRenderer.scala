package renderer

class HtmlRenderer extends TemplateRenderer {
  override def render(template: String): String = {
    // Implement HTML rendering logic here
    s"<html><body>$template</body></html>"
  }
}
