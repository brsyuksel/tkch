package tkch.products.repo.interpreters

import scalaz._, Scalaz._
import zio._
import zio.interop.catz._
import zio.interop.catz.implicits._
import cats.effect.Blocker
import doobie._
import doobie.implicits._
import doobie.util.ExecutionContexts

import org.scalatest._

import tkch.products.Product

class QuillTaskProductRepoSpec extends FlatSpec with Matchers with BeforeAndAfterAll {
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
    val insertCats = sql"""
      INSERT INTO public.category(name, created_at, modified_at)
           VALUES ('category-1', '2019-10-13 12:47:34.039', '2019-10-13 12:47:34.039'),
                  ('category-2', '2019-10-13 12:47:34.039', '2019-10-13 12:47:34.039'),
                  ('category-3', '2019-10-13 12:47:34.039', '2019-10-13 12:47:34.039')
      """.update.run
    val insertProds = sql"""
      INSERT INTO public.product(category_id, name, description, price, created_at, modified_at)
           VALUES (1, 'product-1', 'product-1 of category-1', 1.01, '2019-10-13 12:47:34.039', '2019-10-13 12:47:34.039'),
                  (1, 'product-2', 'product-2 of category-1', 1.02, '2019-10-13 12:47:34.039', '2019-10-13 12:47:34.039'),
                  (1, 'product-3', 'product-3 of category-1', 1.03, '2019-10-13 12:47:34.039', '2019-10-13 12:47:34.039'),
                  (2, 'product-4', 'product-4 of category-2', 2.01, '2019-10-13 12:47:34.039', '2019-10-13 12:47:34.039')
      """.update.run

    rt.unsafeRun(for {
      _ <- tr.transact(xa)
      _ <- insertCats.transact(xa)
      _ <- insertProds.transact(xa)
    } yield ())
  }

  val repo = new QuillTaskProductRepo(xa)

  "create" should "return recently created product with id" in {
    val p = Product(2L, "created-product", "newly created product for category-2", 100.10)
    val res = rt.unsafeRun(repo.create(p))
    res.id.isEmpty shouldEqual false
    res.categoryId shouldEqual p.categoryId
    res.name shouldEqual p.name
    res.description shouldEqual p.description
    res.price shouldEqual p.price

    val q = rt.unsafeRun(repo.getById(res.id.get))
    q.flatMap(_.id) shouldEqual res.id
    q.map(_.categoryId) shouldEqual p.categoryId.some
    q.map(_.name) shouldEqual p.name.some
    q.map(_.description) shouldEqual p.description.some
    q.map(_.price) shouldEqual p.price.some
    q.map(_.createdAt) shouldEqual res.createdAt.some
    q.map(_.modifiedAt) shouldEqual res.modifiedAt.some

    rt.unsafeRun(repo.delete(res.id.get))
  }

  "getById" should "some of product for existing product" in {
    val res = rt.unsafeRun(repo.getById(1L))
    res.isEmpty shouldEqual false
    res.map(_.categoryId) shouldEqual 1L.some
    res.map(_.name) shouldEqual "product-1".some
    res.map(_.description) shouldEqual "product-1 of category-1".some
    res.map(_.price) shouldEqual 1.01.some
  }

  it should "return none for not-existing product" in {
    val res = rt.unsafeRun(repo.getById(100L))
    res.isEmpty shouldEqual true
  }

  "list" should "return list of product by offset and limit" in {
    val res = rt.unsafeRun(repo.list(0, 100))
    res.isEmpty shouldEqual false

    val res2 = rt.unsafeRun(repo.list(0, 2))
    res2.length shouldEqual 2

    val res3 = rt.unsafeRun(repo.list(2, 2))
    res3.length shouldEqual 2

    val res4 = rt.unsafeRun(repo.list(100, 10))
    res4.isEmpty shouldEqual true
  }

  "listByCategoryId" should "return list of product for category by offset and limit" in {
    val res = rt.unsafeRun(repo.listByCategoryId(1L, 0, 10))
    res.map(_.categoryId == 1L).fold(true)((a, b) => a && b) shouldEqual true
    res.length shouldEqual 3

    val res2 = rt.unsafeRun(repo.listByCategoryId(1L, 0, 1))
    res2.length shouldEqual 1
    res2.head.categoryId == 1L
  }

  "update" should "return updated product" in {
    val p = Product(3L, "3l-product", "desc", 100.10)
    val c = rt.unsafeRun(repo.create(p))

    val uProd = c.copy(name = "updated", description = "updated-desc", price = 200.10)
    val u = rt.unsafeRun(repo.update(uProd))
    c.id shouldEqual u.id
    assert(c.name != u.name)
    assert(c.description != u.description)
    assert(c.price != u.price)
    assert(c.modifiedAt != u.modifiedAt)

    val uRes = rt.unsafeRun(repo.getById(c.id.get))
    uRes.flatMap(_.id) shouldEqual u.id
    uRes.map(_.name) shouldEqual u.name.some
    uRes.map(_.description) shouldEqual u.description.some
    uRes.map(_.price) shouldEqual u.price.some
    uRes.map(_.createdAt) shouldEqual u.createdAt.some
    uRes.map(_.modifiedAt) shouldEqual u.modifiedAt.some

    rt.unsafeRun(repo.delete(c.id.get))
  }
}
