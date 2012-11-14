import sbt._
import sbt.Keys._
import sbtassembly.Plugin._
import AssemblyKeys._

object DirwatcherBuild extends Build {

  lazy val dirwatcher = Project(
    id = "dir-watcher",
    base = file("."),
    settings = Project.defaultSettings ++ assemblySettings ++ Seq(
      name := "dir-watcher",
      organization := "net.caoticode.dirwatcher",
      version := "0.1-SNAPSHOT",
      scalaVersion := "2.9.2",
      resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases",
      resolvers += "Twitter Repository" at "http://maven.twttr.com/",
      libraryDependencies += "com.typesafe.akka" % "akka-actor" % "2.0.3",
      libraryDependencies += "com.twitter"   % "util-eval"   % "5.3.13"
    )
  )
}
