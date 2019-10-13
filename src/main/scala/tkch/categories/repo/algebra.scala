package tkch.categories.repo

import tkch.categories.Category

trait CategoryRepo[F[_]] {
  def getById(id: Long): F[Option[Category]]
}
