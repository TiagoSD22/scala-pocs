package models

import annotations.{Email, NonEmpty}

case class User(
  @Email email: String,
  @NonEmpty name: String
)
