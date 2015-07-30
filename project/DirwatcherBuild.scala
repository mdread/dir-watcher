import sbt._
import sbt.Keys._

object DirwatcherBuild extends Build {

  lazy val dirwatcher = Project(
    id = "dir-watcher",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "dir-watcher",
      organization := "net.caoticode.dirwatcher",
      version := "0.1-SNAPSHOT",
      scalaVersion := "2.11.7",
      libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.12"
    )
  )
}
