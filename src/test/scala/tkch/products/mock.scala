package tkch.products

import java.time.Instant
import java.time.temporal.ChronoUnit

import scalaz._, Scalaz._

import tkch.categories.Category
import tkch.categories.repo.CategoryRepo
import tkch.products.repo.ProductRepo

object mock {
  class CategoryRepoMock[F[_]: Applicative](l: List[Category]) extends CategoryRepo[F] {
    def getById(id: Long): F[Option[Category]] =
      l.filter(_.id == id.some).headOption.pure[F]
  }

  class ProductRepoMock[F[_]: Applicative](l: List[Product]) extends ProductRepo[F] {
    def create(p: Product): F[Product] =
      p.copy(id = 100L.some).pure[F]

    def getById(id: Long): F[Option[Product]] =
      l.filter(_.id == id.some).headOption.pure[F]

    def list(offset: Int, limit: Int): F[List[Product]] =
      l.drop(offset).take(limit).pure[F]

    def listByCategoryId(cId: Long, offset: Int, limit: Int): F[List[Product]] =
      l.filter(_.categoryId == cId).drop(offset).take(limit).pure[F]

    def update(p: Product): F[Product] =
      p.copy(modifiedAt = Instant.now.plus(10L, ChronoUnit.SECONDS)).pure[F]

    def delete(id: Long): F[Unit] =
      ().pure[F]
  }
}
