import model.{Product, State, TaxRate}
import service.{TaxCalculator, TaxService}
import zio.{ExitCode, ULayer, ZIO, ZLayer}

object App extends zio.ZIOAppDefault {

  val product = Product("1", "Laptop", 1000.00)
  val state = State("New York")
  val year = 2023

  val taxRatesConfig: Map[(String, Int), BigDecimal] = Map(
    ("California", 2023) -> 0.075,
    ("New York", 2023) -> 0.085
  )

  val taxServiceLayer: ULayer[TaxService] = ZLayer.succeed(new TaxService {
    override def getTaxRate(state: State, year: Int): ZIO[Any, Nothing, TaxRate] = {
      val rate = taxRatesConfig.getOrElse((state.name, year), BigDecimal(0.0))
      ZIO.succeed(TaxRate(state, year, rate))
    }
  })

  val taxCalculatorLayer: ULayer[TaxCalculator] = taxServiceLayer >>> ZLayer.fromFunction(new TaxCalculator(_))

  override def run: ZIO[Any, Nothing, ExitCode] = {
    val taxCalculation = for {
      taxCalculator <- ZIO.service[TaxCalculator]
      calculation <- taxCalculator.calculateTax(product, state, year)
      _ <- ZIO.succeed(println(s"Product: ${product.name}, Value: ${product.price}, " +
                               s"State: ${state.name}, Tax Rate: ${calculation.taxRate}, " +
                               s"Tax Amount: ${calculation.taxAmount}"))
    } yield ()

    taxCalculation.provideLayer(taxCalculatorLayer).exitCode
  }
}