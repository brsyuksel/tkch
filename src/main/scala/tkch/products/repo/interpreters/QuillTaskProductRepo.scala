package tkch.products.repo.interpreters

import java.time.Instant

import scalaz._, Scalaz._
import zio._
import zio.interop.scalaz72._
import zio.interop.catz._
import doobie._
import doobie.implicits._
import doobie.quill.DoobieContext
import io.getquill.{idiom => _, _}

import tkch.common.db._
import tkch.products.Product
import tkch.products.repo.ProductRepo

final class QuillTaskProductRepo(xa: Transactor[Task]) extends ProductRepo[Task] {
  import QuillTaskProductRepo._, dc._

  def create(p: Product): Task[Product] =
    dc.run(stmt.create(p)).transact(xa) >>= (id => p.copy(id = id).pure[Task])

  def getById(id: Long): Task[Option[Product]] =
    dc.run(stmt.getById(id)).transact(xa).map(_.headOption)

  def list(offset: Int, limit: Int): Task[List[Product]] =
    dc.run(stmt.list(offset, limit)).transact(xa)

  def listByCategoryId(cId: Long, offset: Int, limit: Int): Task[List[Product]] =
    dc.run(stmt.listByCategoryId(cId, offset, limit)).transact(xa)

  def update(p: Product): Task[Product] =
    p.copy(modifiedAt = now).pure[Task] >>= (u => dc.run(stmt.update(u)).transact(xa).map(_ => u))

  def delete(id: Long): Task[Unit] =
    dc.run(stmt.delete(id)).transact(xa) >>= (_ => Task.unit)
}

object QuillTaskProductRepo {
  val dc = new DoobieContext.Postgres(SnakeCase)
  import dc._

  private def now = Instant.now

  object stmt {
    def create(p: Product) = quote {
      query[Product].insert(lift(p)).returningGenerated(p => p.id)
    }

    def getById(id: Long) = quote {
      query[Product].filter(_.id == lift(id.some))
    }

    def list(offset: Int, limit: Int) = quote {
      query[Product].drop(lift(offset)).take(lift(limit))
    }

    def listByCategoryId(cId: Long, offset: Int, limit: Int) = quote {
      query[Product].filter(_.categoryId == lift(cId)).drop(lift(offset)).take(lift(limit))
    }

    def update(p: Product) = quote {
      query[Product].filter(_.id == lift(p.id)).update(lift(p))
    }

    def delete(id: Long) = quote {
      query[Product].filter(_.id == lift(id.some)).delete
    }
  }
}
