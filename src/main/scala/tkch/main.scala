package tkch

import scala.concurrent.ExecutionContext

import scalaz._, Scalaz._
import zio._
import zio.interop.scalaz72._
import zio.interop.catz._
import zio.interop.catz.implicits._
import cats.effect.Blocker
import doobie._
import doobie.implicits._
import doobie.hikari._

import tkch.categories.repo.interpreters.QuillTaskCategoryRepo
import tkch.products.Product
import tkch.products.repo.interpreters.QuillTaskProductRepo
import tkch.products.ProductModule

object main extends ManagedApp {
  implicit val rt = new DefaultRuntime {}

  def mkXa(connEc: ExecutionContext, xaEc: ExecutionContext): Managed[Throwable, HikariTransactor[Task]] = {
    val xa = HikariTransactor.newHikariTransactor[Task](
      "org.postgresql.Driver",
      "jdbc:postgresql:tkch",
      "postgres",
      "postgres",
      connEc,
      Blocker.liftExecutionContext(xaEc)
    )

    ZIO.runtime[Any].toManaged_.flatMap {implicit rt => xa.toManaged}
  }

  override def run(args: List[String]): ZManaged[Environment, Nothing, Int] = {
    val prog = for {
      cEC <- ExecutionContexts.fixedThreadPool[Task](32).toManaged
      xaEC <- blocking.blockingExecutor.map(_.asEC).toManaged_
      xa <- mkXa(cEC, xaEC)
      catRepo = new QuillTaskCategoryRepo(xa)
      prodRepo = new QuillTaskProductRepo(xa)
      module = new ProductModule(prodRepo, catRepo)
      _ <- server.make(module).toManaged_
    } yield ()

    (ZManaged.environment[Environment] >>= (e => prog.provideSome[Environment] { _ => e}))
      .foldM(
        t => zio.console.putStrLn(s"err: ${t.getMessage}").const(1).toManaged_,
        _ => ZManaged.succeed(0)
      )
  }
}
