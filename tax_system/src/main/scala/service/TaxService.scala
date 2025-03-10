package service

import model.{State, TaxRate}
import zio.*

trait TaxService {
  def getTaxRate(state: State, year: Int): Task[TaxRate]
}

object TaxService {
  def getTaxRate(state: State, year: Int): ZIO[TaxService, Throwable, TaxRate] =
    ZIO.serviceWithZIO[TaxService](_.getTaxRate(state, year))
}
