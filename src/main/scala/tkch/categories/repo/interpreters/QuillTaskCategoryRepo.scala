package tkch.categories.repo.interpreters

import scalaz._, Scalaz._
import zio._
import zio.interop.catz._
import doobie._
import doobie.implicits._
import doobie.quill.DoobieContext
import io.getquill.{idiom => _, _}

import tkch.common.db._
import tkch.categories.Category
import tkch.categories.repo.CategoryRepo

final class QuillTaskCategoryRepo(xa: Transactor[Task]) extends CategoryRepo[Task] {
  import QuillTaskCategoryRepo._, dc._

  def getById(id: Long): Task[Option[Category]] =
    dc.run(stmt.getById(id)).transact(xa).map(_.headOption)
}

object QuillTaskCategoryRepo {
  val dc = new DoobieContext.Postgres(SnakeCase)
  import dc._

  object stmt {
    def getById(id: Long) = quote {
      query[Category].filter(_.id == lift(id.some))
    }
  }
}

