package tkch.products

import scalaz._, Scalaz._

import org.scalatest._

class ProductValidationSpec extends FlatSpec with Matchers {
  val catIdZero = "category id must be greater than zero"
  val nameEmpty = "name must not be empty"
  val descEmpty = "description must not be empty"
  val priceZero = "price must be greater than zero"

  "validate" should "return success product if validation is succeeded" in {
    val res = Product.validation.validate(1L, "name", "desc", 10)
    res.isSuccess shouldEqual true
    res.map(_.id) shouldEqual none.success
    res.map(_.categoryId) shouldEqual 1L.success
    res.map(_.name) shouldEqual "name".success
    res.map(_.description) shouldEqual "desc".success
    res.map(_.price == 10) shouldEqual true.success
  }

  it should "fail for invalid value combinations" in {
    val p1 = Product.validation.validate(0L, "name", "desc", 10)
    p1.isSuccess shouldEqual false
    p1 shouldBe catIdZero.failureNel

    val p2 = Product.validation.validate(0L, " ", "desc", 10)
    p2.isSuccess shouldEqual false
    p2 shouldBe NonEmptyList(catIdZero, nameEmpty).failure

    val p3 = Product.validation.validate(0L, " ", " ", 10)
    p3.isSuccess shouldEqual false
    p3 shouldBe NonEmptyList(catIdZero, nameEmpty, descEmpty).failure

    val p4 = Product.validation.validate(0L, " ", " ", 0)
    p4.isSuccess shouldEqual false
    p4 shouldBe NonEmptyList(catIdZero, nameEmpty, descEmpty, priceZero).failure
  }

  "validateU" should "return unit if validation is succeeded" in {
    val res = Product.validation.validateU(1L, "name", "desc", 10)
    res.isSuccess shouldEqual true
    res shouldEqual ().success
  }
}
