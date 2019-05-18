name := "Akka-Bookstore"

version := "0.1"

scalaVersion := "2.12.8"

lazy val akkaVersion = "2.5.22"
lazy val liftVersion = "3.3.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-remote" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
  "net.liftweb" %% "lift-json" % liftVersion,
)