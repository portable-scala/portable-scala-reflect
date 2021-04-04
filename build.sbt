// shadow sbt-scalajs' crossProject until Scala.js 1.0.0 is released
import com.typesafe.tools.mima.core._
import sbtcrossproject.crossProject

val previousVersion = "1.1.1"

inThisBuild(
    Def.settings(
        crossScalaVersions := Seq("2.13.4", "2.12.13", "2.11.12"),
        scalaVersion := crossScalaVersions.value.head,
        version := "1.1.2-SNAPSHOT",
        organization := "org.portable-scala",
        scalacOptions ++= Seq(
            "-deprecation",
            "-feature",
            "-encoding",
            "utf-8",
            "-Ywarn-unused"
        ) ++ {
          if (sys.env.contains("CI")) {
            List("-Xfatal-warnings")
          } else {
            List() // to enable Scalafix locally
          }
        },
        semanticdbEnabled := true,
        semanticdbOptions += "-P:semanticdb:synthetics:on",
        semanticdbVersion := scalafixSemanticdb.revision,
        ThisBuild / scalafixScalaBinaryVersion := CrossVersion
          .binaryScalaVersion(scalaVersion.value),
        ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.5.0",
        libraryDependencies ++= List(
            ("com.github.ghik" % "silencer-lib" % "1.7.3" % Provided).cross(CrossVersion.full),
            compilerPlugin(
                ("com.github.ghik" % "silencer-plugin" % "1.7.3").cross(CrossVersion.full)
            )
        ),
        homepage := Some(url("https://github.com/portable-scala/portable-scala-reflect")),
        licenses += ("BSD New",
        url("https://github.com/portable-scala/portable-scala-reflect/blob/master/LICENSE")),
        scmInfo := Some(ScmInfo(url("https://github.com/portable-scala/portable-scala-reflect"),
                "scm:git:git@github.com:portable-scala/portable-scala-reflect.git",
                Some("scm:git:git@github.com:portable-scala/portable-scala-reflect.git")))
    ))

lazy val `portable-scala-reflect` = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .in(file("."))
  .settings(
      scalacOptions in (Compile, doc) -= "-Xfatal-warnings",
      mimaPreviousArtifacts +=
        organization.value %%% moduleName.value % previousVersion,
      mimaBinaryIssueFilters ++= List(
          /* `MacroCompat` has been removed, it was needed for Scala 2.10 only */
          ProblemFilters.exclude[MissingClassProblem](
              "org.portablescala.reflect.internal.Macros$MacroCompat$"),
          ProblemFilters.exclude[MissingClassProblem](
              "org.portablescala.reflect.internal.Macros$MacroCompat$Scope1$"),
          ProblemFilters.exclude[MissingClassProblem](
              "org.portablescala.reflect.internal.Macros$MacroCompat$Scope2$"),
          ProblemFilters.exclude[MissingClassProblem](
              "org.portablescala.reflect.internal.Macros$MacroCompat$Scope2$Inner$"),
          ProblemFilters.exclude[MissingClassProblem](
              "org.portablescala.reflect.internal.Macros$MacroCompat$Scope1$blackbox$")
      ),
      publishMavenStyle := true,
      publishTo := {
        val nexus = "https://oss.sonatype.org/"
        if (isSnapshot.value)
          Some("snapshots".at(nexus + "content/repositories/snapshots"))
        else
          Some("releases".at(nexus + "service/local/staging/deploy/maven2"))
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
      pomIncludeRepository := { _ => false }
  )
  .jvmSettings(
      // Macros
      libraryDependencies += scalaOrganization.value % "scala-reflect" % scalaVersion.value % Provided,
      // Speed up compilation a bit. Our .java files do not need to see the .scala files.
      compileOrder := CompileOrder.JavaThenScala,
      libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"
  )
  .jsConfigure(_.enablePlugins(ScalaJSJUnitPlugin))
  .nativeSettings(
      libraryDependencies +=
        "org.scala-native" %%% "junit-runtime" % "0.4.0" % "test",
      addCompilerPlugin(("org.scala-native" % "junit-plugin" % "0.4.0").cross(CrossVersion.full))
  )

addCommandAlias(
    "fix",
    "; all compile:scalafix test:scalafix; all scalafmtSbt scalafmtAll"
)
addCommandAlias(
    "lint",
    "; scalafmtSbtCheck; scalafmtCheckAll; compile:scalafix --check; test:scalafix --check"
)
