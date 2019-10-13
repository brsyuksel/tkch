package tkch.products

import java.time.Instant

import scalaz._, Scalaz._

import tkch.products.repo.ProductRepo
import tkch.categories.repo.CategoryRepo

final class ProductModule[F[_]: Monad](
  P: ProductRepo[F],
  C: CategoryRepo[F]
)(
  implicit F: MonadError[F, Throwable],
) {
  type L[G[_], A] = WriterT[G, List[String], A]
  type LF[A] = L[F, A]

  import ProductModule.msg
  import ProductModule.exc._

  def createProduct(p: Product): LF[Product] = for {
    _ <- (C.getById(p.categoryId) >>= (_.toMaybe.orError(categoryNotFound))) |> (_.liftM[L] :++> msg(s"fetched category id: ${p.categoryId}"))
    n <- P.create(p) |> (_.liftM[L] :++> msg(s"new product created with name: ${p.name}"))
  } yield n

  def listProducts(offset: Int, limit: Int): LF[List[Product]] =
    P.list(offset, limit) |> (_.liftM[L] :++> msg(s"products are listed from ${offset} with limit ${limit}"))

  def getProduct(id: Long): LF[Product] =
    (P.getById(id) >>= (_.toMaybe.orError(productNotFound))) |> (_.liftM[L] :++> msg(s"fetched product id: $id"))

  def listProductByCategoryId(cId: Long, offset: Int, limit: Int): LF[List[Product]] = for {
    _ <- (C.getById(cId) >>= (_.toMaybe.orError(categoryNotFound))) |> (_.liftM[L] :++> msg(s"fetched category id: $cId"))
    l <- P.listByCategoryId(cId, offset, limit) |> (_.liftM[L] :++> msg(s"products listed for category id: $cId"))
  } yield l

  def updateProduct(id: Long, catId: Long, name: String, desc: String, price: Double): LF[Product] = for {
    p <- getProduct(id)
    _ <- (C.getById(catId) >>= (_.toMaybe.orError(categoryNotFound))) |> (_.liftM[L] :++> msg(s"fetched category id: ${catId}"))
    u = p.copy(categoryId = catId, name = name, description = desc, price = price)
    updated <- P.update(u) |> (_.liftM[L] :++> msg(s"product updated with id: ${u.id}"))
  } yield updated
}

object ProductModule {
  private def msg(m: String)(implicit f: sourcecode.File, l: sourcecode.Line): List[String] =
    s"[${Instant.now}] [${f.value}:${l.value}] $m" :: Nil

  object exc {
    case object CategoryNotFound extends Exception("category is not found")
    case object ProductNotFound extends Exception("product is not found")

    val categoryNotFound: Throwable = CategoryNotFound
    val productNotFound: Throwable = ProductNotFound
  }
}

