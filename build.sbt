import Dependencies._

ThisBuild / scalaVersion     := "2.12.9"
ThisBuild / version          := "0.1.0"
ThisBuild / organization     := "tkch"
ThisBuild / organizationName := "tkch"

lazy val root = (project in file("."))
  .settings(
    name := "tkch",
    libraryDependencies ++= Seq(
      scalazCore,
      zio,
      zioInteropScalaz,
      zioInteropCats,
      quillJdbc,
      postgres,
      doobieCore,
      doobieHikari,
      doobiePostgres,
      doobieQuill,
      http4sDsl,
      http4sServer,
      http4sCirce,
      circeCore,
      circeGeneric,
      sourcecode,
      scalaTest % Test
    )
  )
