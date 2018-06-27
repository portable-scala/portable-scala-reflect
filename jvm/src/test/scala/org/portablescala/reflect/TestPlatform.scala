package org.portablescala.reflect

object TestPlatform {
  val isScala210OnJVM =
    scala.util.Properties.versionNumberString.startsWith("2.10.")
}
