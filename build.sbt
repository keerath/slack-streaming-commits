name := "slack-streaming-commits"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies += "org.saegesser" %% "scalawebsocket" % "0.1.2"

libraryDependencies += "org.apache.spark" % "spark-core_2.11" % "1.5.1"
libraryDependencies += "org.apache.spark" % "spark-streaming_2.11" % "1.5.1"

//libraryDependencies += "org.codehaus.jackson" % "jackson-mapper-asl" % "1.9.13"
libraryDependencies += "org.apache.spark" % "spark-sql_2.11" % "1.5.1"



