
name := "tw_collector"
version := "1.0"
scalaVersion := "2.11.11"


// https://mvnrepository.com/artifact/org.apache.spark/spark-core_2.11
libraryDependencies += "org.apache.spark" % "spark-core_2.11"      % "1.5.2"
libraryDependencies += "org.apache.spark" % "spark-streaming_2.11" % "1.5.2"
libraryDependencies += "com.typesafe"     % "config"               % "1.3.0"
libraryDependencies += "org.slf4j"        % "slf4j-api"            % "1.7.12"
libraryDependencies += "ch.qos.logback"   % "logback-classic"      % "1.1.3"


// https://mvnrepository.com/artifact/org.apache.spark/spark-streaming-twitter_2.11
libraryDependencies += "org.apache.spark" % "spark-streaming-twitter_2.11" % "1.6.3"

libraryDependencies += "org.apache.ignite" % "ignite-core" % "2.0.0"

// https://mvnrepository.com/artifact/org.apache.ignite/ignite-log4j
libraryDependencies += "org.apache.ignite" % "ignite-log4j" % "2.0.0"

// https://mvnrepository.com/artifact/com.google.code.gson/gson
libraryDependencies += "com.google.code.gson" % "gson" % "2.8.1"

