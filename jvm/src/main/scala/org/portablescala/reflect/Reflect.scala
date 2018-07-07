package org.portablescala.reflect

import scala.language.experimental.macros

import scala.collection.mutable

import java.lang.reflect._

import org.portablescala.reflect.annotation._

object Reflect {
  /** Magic to get cross-compiling access to `blackbox.Context` with a fallback
   *  on `macros.Context`, without deprecation warning in any Scala version.
   */
  private object MacroCompat {
    object Scope1 {
      object blackbox
    }
    import Scope1._

    object Scope2 {
      import scala.reflect.macros._
      object Inner {
        import blackbox._
        type BlackboxContext = Context
      }
    }
  }

  import MacroCompat.Scope2.Inner.BlackboxContext

  /** Reflectively looks up a loadable module class using the current class
   *  loader.
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
   *  This method is equivalent to calling
   *  {{{
   *  Reflect.lookupLoadableModuleClass(fqcn, this.getClass.getClassLoader)
   *  }}}
   *
   *  @param fqcn
   *    Fully-qualified name of the module class, including its trailing `$`
   */
  def lookupLoadableModuleClass(fqcn: String): Option[LoadableModuleClass] =
    macro lookupLoadableModuleClass_impl

  def lookupLoadableModuleClass_impl(
      c: BlackboxContext { type PrefixType = Reflect.type })(
      fqcn: c.Expr[String]): c.Expr[Option[LoadableModuleClass]] = {
    import c.universe._
    val loaderExpr = currentClassLoaderExpr(c)
    reify {
      c.prefix.splice.lookupLoadableModuleClass(fqcn.splice, loaderExpr.splice)
    }
  }

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
   *
   *  @param loader
   *    Class loader to use to load the module class
   */
  def lookupLoadableModuleClass(fqcn: String,
      loader: ClassLoader): Option[LoadableModuleClass] = {
    load(fqcn, loader).filter(isModuleClass).map(new LoadableModuleClass(_))
  }

  /** Reflectively looks up an instantiatable class using the current class
   *  loader.
   *
   *  The class or one of its super types (classes or traits) must be annotated
   *  with
   *  [[org.portablescala.reflect.annotation.EnableReflectiveInstantiation @EnableReflectiveInstantiation]].
   *  Moreover, the class must not be abstract, nor be a local class (i.e., a
   *  class defined inside a `def` or inside an anonymous function). Inner
   *  classes (defined inside another class) are supported.
   *
   *  If the class cannot be found, either because it does not exist,
   *  was not `@EnableReflectiveInstantiation` or was abstract or local, this
   *  method returns `None`.
   *
   *  This method is equivalent to calling
   *  {{{
   *  Reflect.lookupInstantiatableClass(fqcn, this.getClass.getClassLoader)
   *  }}}
   *
   *  @param fqcn
   *    Fully-qualified name of the class
   */
  def lookupInstantiatableClass(fqcn: String): Option[InstantiatableClass] =
    macro lookupInstantiatableClass_impl

  def lookupInstantiatableClass_impl(
      c: BlackboxContext { type PrefixType = Reflect.type })(
      fqcn: c.Expr[String]): c.Expr[Option[InstantiatableClass]] = {
    import c.universe._
    val loaderExpr = currentClassLoaderExpr(c)
    reify {
      c.prefix.splice.lookupInstantiatableClass(fqcn.splice, loaderExpr.splice)
    }
  }

  /** Reflectively looks up an instantiatable class.
   *
   *  The class or one of its super types (classes or traits) must be annotated
   *  with
   *  [[org.portablescala.reflect.annotation.EnableReflectiveInstantiation @EnableReflectiveInstantiation]].
   *  Moreover, the class must not be abstract, nor be a local class (i.e., a
   *  class defined inside a `def` or inside an anonymous function). Inner
   *  classes (defined inside another class) are supported.
   *
   *  If the class cannot be found, either because it does not exist,
   *  was not `@EnableReflectiveInstantiation` or was abstract or local, this
   *  method returns `None`.
   *
   *  @param fqcn
   *    Fully-qualified name of the class
   *
   *  @param loader
   *    Class loader to use to load the class
   */
  def lookupInstantiatableClass(fqcn: String,
      loader: ClassLoader): Option[InstantiatableClass] = {
    load(fqcn, loader).filter(isInstantiatableClass).map(new InstantiatableClass(_))
  }

  private def currentClassLoaderExpr(
      c: BlackboxContext { type PrefixType = Reflect.type }): c.Expr[ClassLoader] = {
    import c.universe._
    val enclosingClassTree = c.reifyEnclosingRuntimeClass
    if (enclosingClassTree.isEmpty)
      c.abort(c.enclosingPosition, "call site does not have an enclosing class")
    val enclosingClassExpr = c.Expr[java.lang.Class[_]](enclosingClassTree)
    reify {
      enclosingClassExpr.splice.getClassLoader()
    }
  }

  private def isModuleClass(clazz: Class[_]): Boolean = {
    try {
      val fld = clazz.getField("MODULE$")
      clazz.getName.endsWith("$") && (fld.getModifiers & Modifier.STATIC) != 0
    } catch {
      case _: NoSuchFieldException => false
    }
  }

  private def isInstantiatableClass(clazz: Class[_]): Boolean = {
    /* A local class will have a non-null *enclosing* class, but a null
     * *declaring* class. For a top-level class, both are null, and for an
     * inner class (non-local), both are the same non-null class.
     */
    def isLocalClass: Boolean =
      clazz.getEnclosingClass() != clazz.getDeclaringClass()

    (clazz.getModifiers() & Modifier.ABSTRACT) == 0 &&
    clazz.getConstructors().length > 0 &&
    !isModuleClass(clazz) &&
    !isLocalClass
  }

  private def load(fqcn: String, loader: ClassLoader): Option[Class[_]] = {
    try {
      /* initialize = false, so that the constructor of a module class is not
       * executed right away. It will only be executed when we call
       * `loadModule`.
       */
      val clazz = Class.forName(fqcn, false, loader)
      if (inheritsAnnotation(clazz)) Some(clazz)
      else None
    } catch {
      case _: ClassNotFoundException => None
    }
  }

  private def inheritsAnnotation(clazz: Class[_]): Boolean = {
    val cache = mutable.Map.empty[Class[_], Boolean]

    def c(clazz: Class[_]): Boolean =
      cache.getOrElseUpdate(clazz, l(clazz))

    def l(clazz: Class[_]): Boolean = {
      if (clazz.getAnnotation(classOf[EnableReflectiveInstantiation]) != null) {
        true
      } else {
        (Iterator(clazz.getSuperclass) ++ Iterator(clazz.getInterfaces: _*))
          .filter(_ != null)
          .exists(c)
      }
    }

    c(clazz)
  }
}
