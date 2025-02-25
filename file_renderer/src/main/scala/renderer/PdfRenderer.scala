package renderer

class PdfRenderer extends TemplateRenderer {
  override def render(template: String): String = {
    // Implement PDF rendering logic here
    s"PDF: $template"
  }
}
