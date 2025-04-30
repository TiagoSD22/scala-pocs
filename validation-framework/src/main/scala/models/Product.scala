package models

import annotations.{NonEmpty, Positive}

case class Product(
  @Positive price: Int,
  @NonEmpty name: String
)