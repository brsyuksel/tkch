package tkch.categories

import java.time.Instant

case class Category(
  id: Option[Long],
  name: String,
  createdAt: Instant,
  modifiedAt: Instant
)
