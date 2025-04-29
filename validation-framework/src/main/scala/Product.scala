case class Product(
  @Positive price: Int,
  @NonEmpty name: String
)