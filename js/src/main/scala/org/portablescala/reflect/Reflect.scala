package org.portablescala.reflect

import com.github.ghik.silencer.silent

import scala.scalajs.reflect.{Reflect => SJSReflect}

object Reflect {

  /** Reflectively looks up a loadable module class.
   *
   *  A module class is the technical term referring to the class of a Scala
   *  `object`. The object or one of its super types (classes or traits) must
   *  be annotated with
   *  [[org.portablescala.reflect.annotation.EnableReflectiveInstantiation @EnableReflectiveInstantiation]].
   *  Moreover, the object must be "static", i.e., declared at the top-level of
   *  a package or inside a static object.
   *
   *  If the module class cannot be found, either because it does not exist,
   *  was not `@EnableReflectiveInstantiation` or was not static, this method
   *  returns `None`.
   *
   *  @param fqcn
   *    Fully-qualified name of the module class, including its trailing `$`
   */
  def lookupLoadableModuleClass(fqcn: String): Option[LoadableModuleClass] =
    SJSReflect.lookupLoadableModuleClass(fqcn)

  /** Reflectively looks up a loadable module class.
   *
   *  In Scala.js, this method ignores the parameter `loader`. Calling this
   *  method is equivalent to
   *  {{{
   *  Reflect.lookupLoadableModuleClass(fqcn)
   *  }}}
   *
   *  @param fqcn
   *    Fully-qualified name of the module class, including its trailing `$`
   *
   *  @param loader
   *    Ignored, used in the JVM variant and we need to keep the API
   */
  def lookupLoadableModuleClass(fqcn: String,
      @silent("never used") loader: ClassLoader): Option[LoadableModuleClass] = {
    lookupLoadableModuleClass(fqcn)
  }

  /** Reflectively looks up an instantiable class.
   *
   *  The class or one of its super types (classes or traits) must be annotated
   *  with
   *  [[org.portablescala.reflect.annotation.EnableReflectiveInstantiation @EnableReflectiveInstantiation]].
   *  Moreover, the class must not be abstract, nor be a local class (i.e., a
   *  class defined inside a `def`). Inner classes (defined inside another
   *  class) are supported.
   *
   *  If the class cannot be found, either because it does not exist,
   *  was not `@EnableReflectiveInstantiation` or was abstract or local, this
   *  method returns `None`.
   *
   *  @param fqcn
   *    Fully-qualified name of the class
   */
  def lookupInstantiatableClass(fqcn: String): Option[InstantiatableClass] =
    SJSReflect.lookupInstantiatableClass(fqcn)

  /** Reflectively looks up an instantiable class.
   *
   *  In Scala.js, this method ignores the parameter `loader`. Calling this
   *  method is equivalent to
   *  {{{
   *  Reflect.lookupInstantiatableClass(fqcn)
   *  }}}
   *
   *  @param fqcn
   *    Fully-qualified name of the class
   *
   *  @param loader
   *    Ignored, used in the JVM variant and we need to keep the API
   */
  def lookupInstantiatableClass(fqcn: String,
      @silent("never used") loader: ClassLoader): Option[InstantiatableClass] = {
    lookupInstantiatableClass(fqcn)
  }
}
