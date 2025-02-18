package utils

import cats.effect.IO
import cats.effect.kernel.Ref
import models.Guitar

class Inventory {
  private val inventory: Ref[IO, List[Guitar]] = Ref.unsafe[IO, List[Guitar]](List.empty)

  def addGuitar(guitar: Guitar): IO[Unit] = inventory.update(_ :+ guitar)

  def getInventory: IO[List[Guitar]] = inventory.get
}
