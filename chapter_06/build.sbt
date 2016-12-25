name := """practical-event-sourcing-06"""

val commonSettings = Seq(
  version := "1.0-SNAPSHOT",
  scalaVersion := "2.11.8",
  organization := "com.appliedscala.streaming"
)

lazy val events = (project in file("events")).settings(commonSettings).
  settings(Seq(libraryDependencies := Seq(
    "com.typesafe.play" %% "play-json" % "2.5.9"
  )))

lazy val root = (project in file(".")).settings(commonSettings).enablePlugins(PlayScala)
  .aggregate(events)
  .dependsOn(events)

pipelineStages := Seq(digest)
routesGenerator := InjectedRoutesGenerator

libraryDependencies ++= Seq(
  jdbc,
  evolutions,
  "com.softwaremill.macwire" %% "macros" % "2.2.5" % "provided",
  "org.postgresql" % "postgresql" % "9.4.1207.jre7",
  "org.scalikejdbc" %% "scalikejdbc"       % "2.4.2",
  "org.scalikejdbc" %% "scalikejdbc-config"  % "2.4.2",
  "ch.qos.logback"  %  "logback-classic"   % "1.1.7",
  "de.svenkubiak" % "jBCrypt" % "0.4.1",
  "org.neo4j.driver" % "neo4j-java-driver" % "1.0.6",
  "com.typesafe.akka" %% "akka-stream-kafka" % "0.13"
)
