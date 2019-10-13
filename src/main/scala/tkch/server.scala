package tkch

import scalaz._, Scalaz._
import zio._
import zio.interop.catz._
import zio.interop.catz.implicits._
import org.http4s.{HttpApp, HttpRoutes}
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import org.http4s.server.blaze._
import org.http4s.circe.CirceEntityEncoder._
import org.http4s.circe.CirceEntityDecoder._

import tkch.products.{Product, ProductModule}

object server {
  import ProductModule.exc.{ProductNotFound, CategoryNotFound}

  val dsl = Http4sDsl[Task]
  import dsl._

  private def ?(offset: Option[Int], limit: Option[Int]) =
    (offset | 0, limit | 10)

  private def wLogs(l: List[String]): Task[Unit] =
    Task.sequence(l.map(s => Task.effect(println(s)))) *> Task.unit

  private def !!![T](r: =>Task[(List[String], T)]): Task[T] = {
    lazy val res = r

    for {
      lr <- res
      (logs, o) = lr
      _ <- wLogs(logs)
    } yield o
  }

  private def listProds(offset: Int, limit: Int)(implicit m: ProductModule[Task]) = {
    val res = !!![List[Product]](m.listProducts(offset, limit).run)
    (res >>= (l => Ok(l))) catchSome {
      case _ =>
        InternalServerError()
    }
  }

  private def getProd(id: Long)(implicit m: ProductModule[Task]) = {
    val res = !!![Product](m.getProduct(id).run)
    (res >>= (p => Ok(p))) catchSome {
      case ProductNotFound =>
        NotFound(ErrorResponse(404, List("product not found")))
      case _ =>
        InternalServerError()
    }
  }

  private def listProdForCat(cId: Long, offset: Int, limit: Int)(implicit m: ProductModule[Task]) = {
    val res = !!![List[Product]](m.listProductByCategoryId(cId, offset, limit).run)
    (res >>= (l => Ok(l))) catchSome {
      case CategoryNotFound =>
        NotFound(ErrorResponse(404, List("category not found")))
      case _ =>
        InternalServerError()
    }
  }

  private def createProd(p: Product)(implicit m: ProductModule[Task]) = {
    Product.validation.validate(p.categoryId, p.name, p.description, p.price) match {
      case Failure(err) =>
        BadRequest(ErrorResponse(400, err.toList))
      case Success(prod) =>
        val res = !!![Product](m.createProduct(prod).run)
        (res >>= (p => Created(p))) catchSome {
          case CategoryNotFound =>
            NotFound(ErrorResponse(404, List("category not found")))
          case _ =>
            InternalServerError()
        }
    }
  }

  private def updateProd(id: Long, p: Product)(implicit m: ProductModule[Task]) = {
    Product.validation.validateU(p.categoryId, p.name, p.description, p.price) match {
      case Failure(err) =>
        BadRequest(ErrorResponse(400, err.toList))
      case Success(_) =>
        val res = !!![Product](m.updateProduct(id, p.categoryId, p.name, p.description, p.price).run)
        (res >>= (p => Ok(p))) catchSome {
          case CategoryNotFound =>
            NotFound(ErrorResponse(404, List("category not found")))
          case ProductNotFound =>
            NotFound(ErrorResponse(404, List("product not found")))
          case _ =>
            InternalServerError()
        }
    }
  }

  private def route(implicit module: ProductModule[Task]) = {
    case object OptionalOffsetQPM extends OptionalQueryParamDecoderMatcher[Int]("offset")
    case object OptionalLimitQPM extends OptionalQueryParamDecoderMatcher[Int]("limit")

    HttpRoutes.of[Task] {
      case GET -> Root / "products" :? OptionalOffsetQPM(offset) +& OptionalLimitQPM(limit) =>
        val (o, l) = ?(offset, limit)
        listProds(o, l)
      case req @ POST -> Root / "products" =>
        req.as[Product] >>= (p => createProd(p))
      case GET -> Root / "products" / LongVar(id) =>
        getProd(id)
      case req @ POST -> Root / "products" / LongVar(id) =>
        req.as[Product] >>= (p => updateProd(id, p))
      case GET -> Root / "categories" / LongVar(id) / "products" :? OptionalOffsetQPM(offset) +& OptionalLimitQPM(limit) =>
        val (o, l) = ?(offset, limit)
        listProdForCat(id, o, l)
    }.orNotFound
  }

  def make[R](m: ProductModule[Task])(implicit rt: Runtime[R]) = BlazeServerBuilder[Task]
    .bindHttp(8080, "localhost")
    .withHttpApp(route(m))
    .serve
    .compile[Task, Task, cats.effect.ExitCode]
    .drain
}
