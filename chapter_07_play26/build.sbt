name := """practical-event-sourcing-07-play26"""

val commonSettings = Seq(
  version := "1.0-SNAPSHOT",
  scalaVersion := "2.12.2",
  organization := "com.appliedscala.streaming"
)

lazy val events = (project in file("events")).settings(commonSettings).
  settings(Seq(libraryDependencies := Seq(
    "com.typesafe.play" %% "play-json" % "2.6.3"
  )))

lazy val root = (project in file(".")).settings(commonSettings).enablePlugins(PlayScala)
  .aggregate(events)
  .dependsOn(events)

pipelineStages := Seq(digest)
routesGenerator := InjectedRoutesGenerator

val scalikeJdbcVersion = "3.0.1"

libraryDependencies ++= Seq(
  jdbc,
  evolutions,
  "com.softwaremill.macwire" %% "macros" % "2.3.0" % "provided",
  "org.postgresql" % "postgresql" % "42.1.1",
  "org.scalikejdbc" %% "scalikejdbc"       % scalikeJdbcVersion,
  "org.scalikejdbc" %% "scalikejdbc-config"  % scalikeJdbcVersion,
  "org.scalikejdbc" %% "scalikejdbc-streams"  % scalikeJdbcVersion,
  "ch.qos.logback"  %  "logback-classic"   % "1.2.3",
  "de.svenkubiak" % "jBCrypt" % "0.4.1",
  "org.neo4j.driver" % "neo4j-java-driver" % "1.0.6",
  "com.typesafe.akka" %% "akka-stream-kafka" % "0.13"
)
