scalaVersion := "2.12.3"

fork in run := true

libraryDependencies ++= Seq(
  "com.github.fomkin" %% "pushka-json" % "0.8.0",
  "org.scalaj" %% "scalaj-http" % "2.3.0",
  "biz.enef" %% "slogging" % "0.5.3"
)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
