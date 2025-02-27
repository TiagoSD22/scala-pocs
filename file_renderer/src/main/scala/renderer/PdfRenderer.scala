package renderer

class PdfRenderer extends TemplateRenderer {
  override def render(template: String): String = {
    s"PDF: $template"
  }
}
