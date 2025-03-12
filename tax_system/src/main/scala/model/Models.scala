package model

case class Product(id: String, name: String, price: BigDecimal)
case class State(name: String)
case class TaxRate(state: State, year: Int, rate: BigDecimal)
case class TaxCalculation(product: Product, state: State, year: Int, taxRate: BigDecimal, taxAmount: BigDecimal)
