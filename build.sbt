name := "slack-streaming-commits"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies += "org.saegesser" %% "scalawebsocket" % "0.1.2"
libraryDependencies += "org.apache.spark" %% "spark-streaming" % "1.5.1"
libraryDependencies += "org.apache.spark" %% "spark-core" % "1.5.1"
