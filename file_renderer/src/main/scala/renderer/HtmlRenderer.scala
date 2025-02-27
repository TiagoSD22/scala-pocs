package renderer

class HtmlRenderer extends TemplateRenderer {
  override def render(template: String): String = {
    s"<html><body>$template</body></html>"
  }
}
