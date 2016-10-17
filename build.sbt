name := "generator"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "com.typesafe.play" % "twirl-compiler_2.11" % "1.2.0",
  "org.postgresql" % "postgresql" % "9.4.1211"

)

mainClass in Compile := Some("generator.Main")