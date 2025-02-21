package controllers

package controllers

import cats.effect.IO
import munit.CatsEffectSuite
import io.circe.syntax.*
import io.circe.generic.auto.*
import models.Guitar
import org.http4s._
import org.http4s.implicits._
import org.http4s.circe._
import utils.Inventory

class GuitarFactorySpec extends CatsEffectSuite {

  private val inventory = new Inventory
  private val routes = GuitarFactory.routes(inventory).orNotFound

  test("POST /create-guitar should create a guitar") {
    val guitar = Guitar("Mahogany", "Maple", "Humbucker", "Red", "Right")
    val request = Request[IO](Method.POST, uri"/create-guitar").withEntity(guitar.asJson)

    for {
      response <- routes.run(request)
      body <- response.as[Guitar]
    } yield {
      assertEquals(response.status, Status.Ok)
      assertEquals(body, guitar)
    }
  }

  test("GET /inventory should return the inventory") {
    val request = Request[IO](Method.GET, uri"/inventory")

    for {
      response <- routes.run(request)
      body <- response.as[List[Guitar]]
    } yield {
      assertEquals(response.status, Status.Ok)
      assert(body.nonEmpty)
    }
  }
}
