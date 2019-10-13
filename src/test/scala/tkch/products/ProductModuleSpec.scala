package tkch.products

import scalaz._, Scalaz._

import org.scalatest._

import tkch.categories.Category

class ProductModuleSpec extends FlatSpec with Matchers {
  type E[A] = Throwable \/ Id[A]

  val now = java.time.Instant.now

  val catList = List(
    Category(1L.some, "category-1", now, now),
    Category(2L.some, "category-2", now, now),
    Category(3L.some, "category-3", now, now)
  )

  val prodList = List(
    Product(1L.some, 1L, "c1p1", "d1", 0.11, now, now),
    Product(2L.some, 1L, "c1p2", "d2", 2.02, now, now),
    Product(3L.some, 1L, "c1p3", "d3", 2.33, now, now),
    Product(4L.some, 1L, "c1p4", "d4", 0.44, now, now),
    Product(5L.some, 1L, "c1p5", "d5", 5.11, now, now),
    Product(6L.some, 2L, "c2p6", "d6", 16.96, now, now),
    Product(7L.some, 2L, "c2p7", "d7", 77.00, now, now),
    Product(8L.some, 2L, "c2p8", "d8", 15.86, now, now),
  )

  val catRepoMock = new mock.CategoryRepoMock[E](catList)
  val prodRepoMock = new mock.ProductRepoMock[E](prodList)
  val prodModuleMock = new ProductModule[E](prodRepoMock, catRepoMock)

  "createProduct" should "return created product" in {
    val p = Product(none, 3L, "newp", "new product", 20.10, now, now)
    val res = prodModuleMock.createProduct(p).run
    val c = res.map(_._2)
    c.map(_.id.isEmpty) shouldEqual false.right
    c.map(_.categoryId) shouldEqual p.categoryId.right
    c.map(_.name) shouldEqual p.name.right
    c.map(_.description) shouldEqual p.description.right
    c.map(_.price).map(_ == 20.10) shouldEqual true.right

    val l = res.map(_._1)
    l.map(_.size) shouldEqual 2.right
  }

  it should "fail with CategoryNotFound if a category doesnt exist for provided id" in {
    val p = Product(none, 4L, "n1", "d1", 10.10, now, now)
    val res = prodModuleMock.createProduct(p).run
    res shouldBe ProductModule.exc.categoryNotFound.left
  }

  "listProducts" should "return list of products by offset and limit" in {
    val res = prodModuleMock.listProducts(0, 3).run
    res.map(_._2) shouldBe prodList.drop(0).take(3).right
    res.map(_._1.length) shouldEqual 1.right

    val res2 = prodModuleMock.listProducts(3, 1).run
    res2.map(_._2) shouldBe prodList.drop(3).take(1).right

    val res3 = prodModuleMock.listProducts(100, 2).run
    res3.map(_._2.isEmpty) shouldEqual true.right
  }

  "getProduct" should "return a product for provided valid id" in {
    val res = prodModuleMock.getProduct(4L).run
    res.map(_._2) shouldEqual prodList.filter(_.id == 4L.some).head.right
    res.map(_._1.length) shouldEqual 1.right
  }

  it should "fail with ProductNotFound if a product doesnt exist for provided id" in {
    val res = prodModuleMock.getProduct(100L).run
    res shouldEqual ProductModule.exc.productNotFound.left
  }

  "listProductByCategoryId" should "return list of products which are children of category by offset and limit" in {
    val res = prodModuleMock.listProductByCategoryId(1L, 0, 3).run
    res.map(_._1.length) shouldEqual 2.right
    res.map(_._2) shouldBe prodList.filter(_.categoryId == 1L).take(3).right

    val res2 = prodModuleMock.listProductByCategoryId(1L, 3, 3).run
    res2.map(_._2) shouldBe prodList.filter(_.categoryId == 1L).drop(3).take(2).right

    val res3 = prodModuleMock.listProductByCategoryId(2L, 0, 10).run
    res3.map(_._2) shouldBe prodList.filter(_.categoryId == 2L).right

    val res4 = prodModuleMock.listProductByCategoryId(3L, 0, 10).run
    res4.map(_._2.isEmpty) shouldEqual true.right
  }

  it should "fail with CategoryNotFound if a category doesnt exist for provided id" in {
    val res = prodModuleMock.listProductByCategoryId(4L, 0, 100).run
    res shouldBe ProductModule.exc.categoryNotFound.left
  }

  "updateProduct" should "return updated product" in {
    val old = prodList.filter(_.id == 1L.some).head
    val res = prodModuleMock.updateProduct(1L, 1L, "uc1p1", "ud1", 0.20).run
    val u = res.map(_._2)
    u.map(_.id) shouldEqual 1L.some.right
    u.map(_.categoryId) shouldEqual 1L.right
    u.map(_.name) shouldEqual "uc1p1".right
    u.map(_.description) shouldEqual "ud1".right
    u.map(_.price == 0.20) shouldEqual true.right
    u.map(_.createdAt) shouldEqual old.createdAt.right
    u.map(_.modifiedAt.getEpochSecond).toOption.filter(_ != old.modifiedAt.getEpochSecond).isEmpty shouldEqual false
    res.map(_._1.length) shouldEqual 3.right

    val res2 = prodModuleMock.updateProduct(1L, 2L, "uc2p1", "ud1", 0.20).run
    val u2 = res2.map(_._2)
    u2.map(_.categoryId) shouldEqual 2L.right
  }

  it should "fail with ProductNotFound if a product doesnt exist for updating product id" in {
    val res = prodModuleMock.updateProduct(100L, 1L, "nonexist", "d", 10).run
    res shouldBe ProductModule.exc.productNotFound.left
  }

  it should "fail with CategoryNotFound if a category doesnt exist for provided id" in {
    val res = prodModuleMock.updateProduct(1L, 100L, "nonexist", "catnofound", 10).run
    res shouldBe ProductModule.exc.categoryNotFound.left
  }
}
