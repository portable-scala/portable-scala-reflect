// shadow sbt-scalajs' crossProject and CrossType until Scala.js 1.0.0 is released
import sbtcrossproject.{crossProject, CrossType}

inThisBuild(Def.settings(
  crossScalaVersions := Seq("2.10.7", "2.11.12", "2.12.6"),
  scalaVersion := crossScalaVersions.value.last,
  version := "0.1.0-SNAPSHOT",

  scalacOptions ++= Seq(
    "-deprecation",
    "-feature",
    "-encoding",
    "utf-8",
    "-Xfatal-warnings",
  )
))

lazy val `portable-scala-reflect` = crossProject(JSPlatform, JVMPlatform)
  .in(file("."))
  .settings(
    scalacOptions in (Compile, doc) -= "-Xfatal-warnings",
  )
  .jvmSettings(
    // Macros
    libraryDependencies += scalaOrganization.value % "scala-reflect" % scalaVersion.value,

    // Speed up compilation a bit. Our .java files do not need to see the .scala files.
    compileOrder := CompileOrder.JavaThenScala,

    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test",
  )
  .jsConfigure(_.enablePlugins(ScalaJSJUnitPlugin))
