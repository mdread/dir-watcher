import sbt._
import sbt.Keys._
import com.typesafe.sbt.pgp.PgpKeys._

object DirwatcherBuild extends Build {

  lazy val dirwatcher = Project(
    id = "dir-watcher",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name          := "dir-watcher",
      organization  := "net.caoticode.dirwatcher",
      version       := "0.1.0",
      scalaVersion  := "2.11.7",
      homepage      := Some(url("https://github.com/mdread/dir-watcher")),
      description   := "A scriptable app and library for watching files and directories for filesystem events",
      licenses      := Seq("MIT License" -> url("http://www.opensource.org/licenses/mit-license.php")),

      // dependencies
      libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.12",
      libraryDependencies += "com.twitter" %% "util-eval" % "6.26.0",

      // deployment settings
      useGpg := true,
      publishMavenStyle := true,
      publishTo := {
        val nexus = "https://oss.sonatype.org/"
        if (isSnapshot.value)
          Some("snapshots" at nexus + "content/repositories/snapshots")
        else
          Some("releases"  at nexus + "service/local/staging/deploy/maven2")
      },
      publishArtifact in Test := false,
      pomIncludeRepository := { _ => false },
      pomExtra := (
        <scm>
          <url>git@github.com:mdread/dir-watcher.git</url>
          <connection>scm:git:git@github.com:mdread/dir-watcher.git</connection>
        </scm>
        <developers>
          <developer>
            <id>mdread</id>
            <name>Daniel Camarda</name>
            <url>https://github.com/mdread</url>
          </developer>
        </developers>)

    )
  )
}
