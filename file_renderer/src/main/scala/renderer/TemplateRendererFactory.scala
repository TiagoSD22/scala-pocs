package renderer

object TemplateRendererFactory {
  def getRenderer(format: String): TemplateRenderer = {
    format.toLowerCase match {
      case "html" => new HtmlRenderer
      case "pdf"  => new PdfRenderer
      case "csv"  => new CsvRenderer
      case _      => throw new IllegalArgumentException(s"Unknown format: $format")
    }
  }
}
