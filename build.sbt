name := "FacebookSimulator"

version := "1.0"

scalaVersion := "2.11.7"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
resolvers += "spray repo" at "http://repo.spray.io"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.12",
  "io.spray" %% "spray-json" % "1.3.2",
  "io.spray" %% "spray-routing" % "1.3.3",
  "io.spray" %% "spray-can" % "1.3.3",
  "io.spray" %% "spray-httpx" % "1.3.3",
  "io.spray" %% "spray-routing" % "1.3.3",
  "io.spray" %% "spray-client" % "1.3.3",
  "org.scala-lang.modules" %% "scala-xml" % "1.0.4",
  "org.jvnet.mimepull" % "mimepull" % "1.9.6"
)

fork in run := true
connectInput in run := true