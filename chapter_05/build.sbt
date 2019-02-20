name := """practical-event-sourcing-05"""

val commonSettings = Seq(
  version := "1.0-SNAPSHOT",
  scalaVersion := "2.12.8",
  organization := "com.appliedscala.streaming"
)

lazy val events = (project in file("events")).settings(commonSettings).
  settings(Seq(libraryDependencies := Seq(
    "com.typesafe.play" %% "play-json" % "2.7.1"
  )))

lazy val root = (project in file(".")).settings(commonSettings).enablePlugins(PlayScala)
  .aggregate(events)
  .dependsOn(events)

pipelineStages := Seq(digest)

libraryDependencies ++= Seq(
  jdbc,
  evolutions,
  "com.softwaremill.macwire" %% "macros" % "2.3.0" % "provided",
  "org.postgresql" % "postgresql" % "42.2.5",
  "org.scalikejdbc" %% "scalikejdbc" % "3.3.2",
  "org.scalikejdbc" %% "scalikejdbc-config"  % "3.3.2",
  "ch.qos.logback"  %  "logback-classic" % "1.2.3",
  "de.svenkubiak" % "jBCrypt" % "0.4.1",
  "org.neo4j.driver" % "neo4j-java-driver" % "1.7.2",
  "com.typesafe.akka" %% "akka-stream-kafka" % "1.0-RC1"
)
