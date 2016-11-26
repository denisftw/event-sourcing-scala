name := """play-event-sourcing-starter"""
organization := "com.appliedscala.seeds.es"
version := "1.0-SNAPSHOT"
scalaVersion := "2.11.8"

lazy val root = (project in file(".")).
  enablePlugins(PlayScala)

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
  "de.svenkubiak" % "jBCrypt" % "0.4.1"
)