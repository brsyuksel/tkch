import sbt._

object Dependencies {
  lazy val scalazCore = "org.scalaz" %% "scalaz-core" % "7.2.27"
  lazy val zio = "dev.zio" %% "zio" % "1.0.0-RC14"
  lazy val zioInteropScalaz = "dev.zio" %% "zio-interop-scalaz7x" % "7.2.27.0-RC4"
  lazy val zioInteropCats = "dev.zio" %% "zio-interop-cats" % "2.0.0.0-RC5"
  lazy val quillJdbc = "io.getquill" %% "quill-jdbc" % "3.4.9"
  lazy val postgres = "org.postgresql" % "postgresql" % "42.2.8"
  lazy val doobieCore = "org.tpolecat" %% "doobie-core" % "0.8.4"
  lazy val doobieHikari = "org.tpolecat" %% "doobie-hikari" % "0.8.4"
  lazy val doobiePostgres = "org.tpolecat" %% "doobie-postgres" % "0.8.4"
  lazy val doobieQuill = "org.tpolecat" %% "doobie-quill" % "0.8.4"
  lazy val http4sDsl = "org.http4s" %% "http4s-dsl" % "0.20.11"
  lazy val http4sServer = "org.http4s" %% "http4s-blaze-server" % "0.20.11"
  lazy val http4sCirce = "org.http4s" %% "http4s-circe" % "0.20.11"
  lazy val circeCore = "io.circe" %% "circe-core" % "0.11.1"
  lazy val circeGeneric = "io.circe" %% "circe-generic" % "0.11.1"
  lazy val sourcecode = "com.lihaoyi" %% "sourcecode" % "0.1.7"
  lazy val scalaTest = "org.scalatest" %% "scalatest" % "3.0.8"
}
