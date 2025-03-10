import model.{Product, State, TaxRate}
import service.{TaxCalculator, TaxService}
import zio.{ExitCode, Task, ULayer, ZIO, ZLayer}

object App extends zio.ZIOAppDefault {
  val taxServiceLayer: ULayer[TaxService] = ZLayer.succeed(new TaxService {
    override def getTaxRate(state: State, year: Int): ZIO[Any, Nothing, TaxRate] = {
      ZIO.succeed(TaxRate(state, year, BigDecimal(0.07)))
    }
  })

  val program: ZIO[TaxService, Throwable, Unit] = for {
    product <- ZIO.succeed(Product("1", "Laptop", BigDecimal(1000)))
    state <- ZIO.succeed(State("California"))
    year <- ZIO.succeed(2023)
    taxCalculator = new TaxCalculator(new TaxService {
      override def getTaxRate(state: State, year: Int): Task[TaxRate] = TaxService.getTaxRate(state, year)
    })
    taxCalculation <- taxCalculator.calculateTax(product, state, year)
    _ <- ZIO.succeed(println(s"Tax Calculation: $taxCalculation"))
  } yield ()

  override def run: ZIO[zio.ZEnv, Nothing, ExitCode] =
    program.provideLayer(taxServiceLayer).exitCode
}