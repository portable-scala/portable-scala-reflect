// shadow sbt-scalajs' crossProject and CrossType until Scala.js 1.0.0 is released
import sbtcrossproject.{crossProject, CrossType}

val previousVersion = "1.0.0"

inThisBuild(Def.settings(
  crossScalaVersions := Seq("2.12.10", "2.10.7", "2.11.12", "2.13.1"),
  scalaVersion := crossScalaVersions.value.head,
  version := "1.0.1-SNAPSHOT",
  organization := "org.portable-scala",

  scalacOptions ++= Seq(
    "-deprecation",
    "-feature",
    "-encoding",
    "utf-8",
    "-Xfatal-warnings",
  ),

  homepage := Some(url("https://github.com/portable-scala/portable-scala-reflect")),
  licenses += ("BSD New",
    url("https://github.com/portable-scala/portable-scala-reflect/blob/master/LICENSE")),
  scmInfo := Some(ScmInfo(
    url("https://github.com/portable-scala/portable-scala-reflect"),
    "scm:git:git@github.com:portable-scala/portable-scala-reflect.git",
    Some("scm:git:git@github.com:portable-scala/portable-scala-reflect.git"))),
))

lazy val `portable-scala-reflect` =
    crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .in(file("."))
  .settings(
    scalacOptions in (Compile, doc) -= "-Xfatal-warnings",

    mimaPreviousArtifacts +=
      organization.value %%% moduleName.value % previousVersion,

    publishMavenStyle := true,
    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
    },
    pomExtra := (
      <developers>
        <developer>
          <id>sjrd</id>
          <name>Sébastien Doeraene</name>
          <url>https://github.com/sjrd/</url>
        </developer>
        <developer>
          <id>gzm0</id>
          <name>Tobias Schlatter</name>
          <url>https://github.com/gzm0/</url>
        </developer>
        <developer>
          <id>densh</id>
          <name>Denys Shabalin</name>
          <url>https://github.com/densh/</url>
        </developer>
      </developers>
    ),
    pomIncludeRepository := { _ => false },
  )
  .jvmSettings(
    // Macros
    libraryDependencies += scalaOrganization.value % "scala-reflect" % scalaVersion.value,

    // Speed up compilation a bit. Our .java files do not need to see the .scala files.
    compileOrder := CompileOrder.JavaThenScala,

    libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test",
  )
  .jsConfigure(_.enablePlugins(ScalaJSJUnitPlugin))
  .jsSettings(
    crossScalaVersions ~= { prev =>
      if (scalaJSVersion.startsWith("0.6.")) prev
      else prev.filter(v => !v.startsWith("2.10."))
    }
  )
  .nativeSettings(
    crossScalaVersions := Seq("2.11.12"),
    scalaVersion := "2.11.12",
    libraryDependencies ++= Seq(
      "org.scala-native" %%% "test-interface" % nativeVersion
    )
  )
