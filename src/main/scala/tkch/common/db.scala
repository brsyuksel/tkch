package tkch.common

import java.time.Instant
import java.util.Date

import io.getquill.MappedEncoding

object db {
  implicit val encodeInstant = MappedEncoding[Instant, Date](Date.from)
  implicit val decodeInstant = MappedEncoding[Date, Instant](_.toInstant)
}
