name := "3-RabbitMQ-Hospital"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
  "com.newmotion" %% "akka-rabbitmq" % "5.0.4-beta",
  "com.typesafe.akka" %% "akka-actor" % "2.5.21",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.21" % Test
)