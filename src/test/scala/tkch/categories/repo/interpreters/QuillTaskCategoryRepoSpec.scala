package tkch.categories.repo.interpreters

import scalaz._, Scalaz._
import zio._
import zio.interop.catz._
import zio.interop.catz.implicits._
import cats.effect.Blocker
import doobie._
import doobie.implicits._
import doobie.util.ExecutionContexts

import org.scalatest._

class QuillTaskCategoryRepoSpec extends FlatSpec with Matchers with BeforeAndAfterAll {
  val rt = new DefaultRuntime {}
  val xa = Transactor.fromDriverManager[Task](
    "org.postgresql.Driver",
    "jdbc:postgresql:tkch_test",
    "postgres",
    "postgres",
    Blocker.liftExecutionContext(ExecutionContexts.synchronous)
  )

  override def beforeAll() {
    val tr = sql"TRUNCATE TABLE public.category RESTART IDENTITY CASCADE".update.run
    val inserts = sql"""
      INSERT INTO public.category(name, created_at, modified_at)
           VALUES ('category-1', '2019-10-13 12:47:34.039', '2019-10-13 12:47:34.039'),
                  ('category-2', '2019-10-13 12:47:34.039', '2019-10-13 12:47:34.039')
      """.update.run

    rt.unsafeRun(for {
      _ <- tr.transact(xa)
      _ <- inserts.transact(xa)
    } yield ())
  }

  val repo = new QuillTaskCategoryRepo(xa)

  "getById" should "return some of category for existing category" in {
    val res = rt.unsafeRun(repo.getById(1L))
    res.isEmpty shouldEqual false
    res.flatMap(_.id) shouldEqual 1L.some
    res.map(_.name) shouldEqual "category-1".some
  }

  it should "return none for not-existing category" in {
    val res = rt.unsafeRun(repo.getById(100L))
    res.isEmpty shouldEqual true
  }
}
