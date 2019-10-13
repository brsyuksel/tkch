package tkch

import io.circe._
import io.circe.generic.semiauto._

case class ErrorResponse(code: Int, messages: List[String])

object ErrorResponse {
  implicit val encErr: Encoder[ErrorResponse] = deriveEncoder[ErrorResponse]
}
