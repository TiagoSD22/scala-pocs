ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.5"

lazy val root = (project in file("."))
  .settings(
    name := "guitar_factory"
  )

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-effect" % "3.3.14",
  "org.http4s" %% "http4s-blaze-server" % "0.23.12",
  "org.http4s" %% "http4s-circe" % "0.23.12",
  "org.http4s" %% "http4s-dsl" % "0.23.12",
  "io.circe" %% "circe-generic" % "0.14.2",
  "org.log4s" %% "log4s" % "1.10.0"
)
