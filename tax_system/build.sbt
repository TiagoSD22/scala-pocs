ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.5"

lazy val root = (project in file("."))
  .settings(
    name := "tax_system"
  )

libraryDependencies += "dev.zio" %% "zio" % "2.0.0"
