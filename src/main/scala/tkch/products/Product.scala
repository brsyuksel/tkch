package tkch.products

import java.time.Instant

import scalaz._, Scalaz._
import io.circe.{Encoder, Decoder}

case class Product(
  id: Option[Long],
  categoryId: Long,
  name: String,
  description: String,
  price: Double,
  createdAt: Instant,
  modifiedAt: Instant
)

object Product {
  implicit val encProd: Encoder[Product] =
    Encoder.forProduct7("id", "category_id", "name", "description", "price", "created_at", "modified_at")(
      p => (p.id, p.categoryId, p.name, p.description, p.price, p.createdAt, p.modifiedAt)
    )
  implicit val decProd: Decoder[Product] =
    Decoder.forProduct4("category_id", "name", "description", "price")(Product.apply)

  def apply(c: Long, n: String, d: String, p: Double): Product = {
    val now = Instant.now
    Product(none, c, n, d, p, now, now)
  }

  object validation {
    private def now = Instant.now

    private def catId(id: Long): ValidationNel[String, Long] =
      if (id <= 0) "category id must be greater than zero".failureNel
      else id.success

    private def name(s: String): ValidationNel[String, String] = {
      val c = s.trim

      if (c.isEmpty) "name must not be empty".failureNel
      else if (c.length > 255) "name length must not exceed 255".failureNel
      else c.success
    }

    private def desc(s: String): ValidationNel[String, String] = {
      val c = s.trim

      if (c.isEmpty) "description must not be empty".failureNel
      else if (c.length > 1024) "description length must not exceed 1024".failureNel
      else c.success
    }

    private def price(p: Double): ValidationNel[String, Double] =
      if (p <= 0d) "price must be greater than zero".failureNel
      else p.success

    def validate(c: Long, n: String, d: String, p: Double): ValidationNel[String, Product] =
      (catId(c) |@| name(n) |@| desc(d) |@| price(p))(Product(_, _, _, _))

    def validateU(c: Long, n: String, d: String, p: Double): ValidationNel[String, Unit] =
      (catId(c) |@| name(n) |@| desc(d) |@| price(p))((_, _, _, _) => ())
  }
}
