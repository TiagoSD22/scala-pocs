import cats.effect.{ExitCode, IO, IOApp}
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.implicits.*
import controllers.GuitarFactory
import utils.Inventory

object App extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    val inventory = new Inventory

    BlazeServerBuilder[IO]
      .bindHttp(8080, "localhost")
      .withHttpApp(GuitarFactory.routes(inventory).orNotFound)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
  }
}
