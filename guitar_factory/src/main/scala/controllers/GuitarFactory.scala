package controllers

import cats.effect.IO
import io.circe.generic.auto.*
import io.circe.syntax.*
import models.Guitar
import org.http4s.HttpRoutes
import org.http4s.dsl.io.*
import org.http4s.circe.*
import utils.Inventory

object GuitarFactory {
  import org.http4s.circe.CirceEntityCodec.circeEntityDecoder

  def routes(inventory: Inventory): HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req @ POST -> Root / "create-guitar" =>
      for {
        guitar <- req.as[Guitar]
        _ <- inventory.addGuitar(guitar)
        resp <- Ok(guitar.asJson)
      } yield resp

    case GET -> Root / "inventory" =>
      for {
        guitars <- inventory.getInventory
        resp <- Ok(guitars.asJson)
      } yield resp
  }
}