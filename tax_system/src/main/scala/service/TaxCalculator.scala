package service

import model.{Product, State, TaxCalculation}
import zio.Task

class TaxCalculator(taxService: TaxService) {
  def calculateTax(product: Product, state: State, year: Int): Task[TaxCalculation] = {
    for {
      taxRate <- taxService.getTaxRate(state, year)
      taxAmount = product.price * taxRate.rate
    } yield TaxCalculation(product, state, year, taxRate.rate, taxAmount)
  }
}
