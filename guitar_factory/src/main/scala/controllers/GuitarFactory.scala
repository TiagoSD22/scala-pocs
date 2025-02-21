package controllers

import cats.effect.IO
import io.circe.generic.auto.*
import io.circe.syntax.*
import models.Guitar
import org.http4s.HttpRoutes
import org.http4s.dsl.io.*
import org.http4s.circe.*
import utils.Inventory
import org.log4s.getLogger

object GuitarFactory {
  private[this] val logger = getLogger

  import org.http4s.circe.CirceEntityCodec.circeEntityDecoder

  def routes(inventory: Inventory): HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req @ POST -> Root / "create-guitar" =>
      for {
        guitar <- req.as[Guitar]
        _ = logger.info(s"Received request to create guitar: $$guitar")
        _ <- inventory.addGuitar(guitar)
        resp <- Ok(guitar.asJson)
      } yield {
        logger.info(s"Guitar created: $$guitar")
        resp
      }

    case GET -> Root / "inventory" =>
      for {
        guitars <- inventory.getInventory
        _ = logger.info("Received request to get inventory")
        resp <- Ok(guitars.asJson)
      } yield {
        logger.info(s"Inventory retrieved: $$guitars")
        resp
      }
  }
}