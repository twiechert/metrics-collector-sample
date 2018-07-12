name := "metrics-collector-server"

version := "0.1"

scalaVersion := "2.12.6"


lazy val commonLib = RootProject(file("../common"))
val main = Project(id = "application", base = file(".")).dependsOn(commonLib)

libraryDependencies ++= Seq(
  "com.twitter" %% "finatra-http" % "18.6.0",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.paulgoldbaum" %% "scala-influxdb-client" % "0.6.1"
)
