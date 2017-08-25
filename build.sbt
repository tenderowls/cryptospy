import com.typesafe.sbt.packager.docker._

enablePlugins(UniversalPlugin, DockerPlugin, AshScriptPlugin, GitVersioning)

scalaVersion       := "2.12.3"
git.useGitDescribe := true

fork in run := true

libraryDependencies ++= Seq(
  "com.github.fomkin" %% "pushka-json" % "0.8.0",
  "org.scalaj" %% "scalaj-http" % "2.3.0",
  "biz.enef" %% "slogging" % "0.5.3"
)

dockerUsername        := Some("tenderowls")
dockerBaseImage       := "openjdk:8-jre-alpine"
dockerUpdateLatest    := true
dockerExposedVolumes  := Seq("/data")
dockerCommands        := {
  val xs = dockerCommands.value
  val setEnv = ExecCmd("CMD", "/data")
  xs.dropRight(1) :+ setEnv
}

packageName in Docker := "cryptospy"

mainClass             := Some("com.tenderowls.cryptospy.Cryptospy")
name                  := "cryptospy"
packageSummary        := "Monitoring of crypto-currency exchanges"
packageDescription    := "Cryptopsy collects orders from various cryptocurrency exchanges using their API"
maintainer            := "Aleksey Fomkin <aleksey.fomkin@gmail.com>"

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
