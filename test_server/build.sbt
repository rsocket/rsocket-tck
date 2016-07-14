
import AssemblyKeys._

name := "test_server"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies += "com.google.guava" % "guava" % "19.0"
libraryDependencies += "org.reactivestreams" % "reactive-streams" % "1.0.0"
libraryDependencies += "com.jcraft" % "jzlib" % "1.1.3"
libraryDependencies += "io.netty" % "netty-all" % "4.1.0.CR7"
libraryDependencies += "io.reactivesocket" % "reactivesocket" % "0.1.3"
libraryDependencies += "io.reactivesocket" % "reactivesocket-transport-tcp" % "0.2.2-SNAPSHOT"
libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.12"
/*libraryDependencies += "io.reactivex" % "rxjava" % "1.1.5"
libraryDependencies += "io.reactivex" % "rxjava-reactive-streams" % "1.0.1"*/
// right now we need to use snapshot but once the release for 2.x comes out, we should switch
// to the real repository
libraryDependencies += "io.reactivex" % "rxjava-reactive-streams" % "1.1.0"
libraryDependencies += "io.reactivex.rxjava2" % "rxjava" % "2.0.0-DP0-SNAPSHOT"
libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.4"
libraryDependencies += "io.reactivesocket" % "reactivesocket-netty" % "0.1.9-SNAPSHOT" // wait for release
libraryDependencies += "org.json4s" %% "json4s-native" % "3.3.0"
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-core" % "2.8.0.rc2"
libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.8.0.rc2"
libraryDependencies += "org.scala-lang" % "scala-reflect" % "2.11.8"

resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
resolvers += "jfrog" at "https://oss.jfrog.org/libs-snapshot"

assemblySettings

mergeStrategy in assembly := {
  case m if m.toLowerCase.endsWith("manifest.mf")          => MergeStrategy.discard
  case m if m.toLowerCase.matches("meta-inf.*\\.sf$")      => MergeStrategy.discard
  case "log4j.properties"                                  => MergeStrategy.discard
  case m if m.toLowerCase.startsWith("meta-inf/services/") => MergeStrategy.filterDistinctLines
  case "reference.conf"                                    => MergeStrategy.concat
  case _                                                   => MergeStrategy.first
}
