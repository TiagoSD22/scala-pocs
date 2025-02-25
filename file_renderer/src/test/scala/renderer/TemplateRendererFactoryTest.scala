package renderer

import org.scalatest.funsuite.AnyFunSuite
import renderer._

class TemplateRendererFactoryTest extends AnyFunSuite {

  test("TemplateRendererFactory should return HtmlRenderer for 'html' format") {
    val renderer = TemplateRendererFactory.getRenderer("html")
    assert(renderer.isInstanceOf[HtmlRenderer])
  }

  test("TemplateRendererFactory should return PdfRenderer for 'pdf' format") {
    val renderer = TemplateRendererFactory.getRenderer("pdf")
    assert(renderer.isInstanceOf[PdfRenderer])
  }

  test("TemplateRendererFactory should return CsvRenderer for 'csv' format") {
    val renderer = TemplateRendererFactory.getRenderer("csv")
    assert(renderer.isInstanceOf[CsvRenderer])
  }

  test("TemplateRendererFactory should throw IllegalArgumentException for unsupported format") {
    assertThrows[IllegalArgumentException] {
      TemplateRendererFactory.getRenderer("unsupported")
    }
  }
}
