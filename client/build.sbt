
name := "metrics-client"

version := "0.1"

scalaVersion := "2.12.6"


lazy val commonLib = RootProject(file("../common"))
val main = Project(id = "application", base = file(".")).dependsOn(commonLib)

libraryDependencies += "com.github.oshi" % "oshi-core" % "3.6.2"
libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.5.6"
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-core" % "2.9.6"
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.6"
libraryDependencies += "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.9.6"