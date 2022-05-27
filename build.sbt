name := "OraclePool"

version := "0.1"

updateOptions := updateOptions.value.withLatestSnapshots(false)

scalaVersion := "2.12.10"

resolvers ++= Seq(
  "Sonatype Releases" at "https://s01.oss.sonatype.org/content/repositories/releases",
  "Sonatype Releases 2" at "https://oss.sonatype.org/content/repositories/releases/",
  "SonaType" at "https://oss.sonatype.org/content/groups/public",
  "SonaType Snapshots" at "https://s01.oss.sonatype.org/content/repositories/snapshots/",
  "SonaType Staging" at "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
)

libraryDependencies ++= Seq(
  "io.github.ergoplatform" %% "kiosk" % "1.0",
  "org.scala-lang.modules" %% "scala-collection-compat" % "2.1.3",
  "com.squareup.okhttp3" % "mockwebserver" % "3.14.9" % Test,
  "org.scalatest" %% "scalatest" % "3.0.8" % Test,
  "org.scalacheck" %% "scalacheck" % "1.14.+" % Test,
  "org.mockito" % "mockito-core" % "2.23.4" % Test
)

lazy val root = (project in file("."))
  .settings(
    updateOptions := updateOptions.value.withLatestSnapshots(false),
    assemblyMergeStrategy in assembly := {
      case PathList("reference.conf")    => MergeStrategy.concat
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case x                             => MergeStrategy.first
    }
  )
