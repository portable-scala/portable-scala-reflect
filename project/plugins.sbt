val scalaJSVersion =
  Option(System.getenv("SCALAJS_VERSION")).filter(_ != "").getOrElse("1.0.1")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % scalaJSVersion)
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject" % "1.0.0")

addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "0.8.1")
