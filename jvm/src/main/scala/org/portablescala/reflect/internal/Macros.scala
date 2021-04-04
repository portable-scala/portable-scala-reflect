package org.portablescala.reflect.internal

import org.portablescala.reflect._

import scala.reflect.macros.blackbox

/* Macro definitions are enclosed in a dedicated `Macros` object,
 * so that their metadata (the types involved etc.) don't pollute `Reflect`'s metadata.
 * This enables using `Reflect`'s methods without `scala-reflect` JAR
 * https://github.com/scala/bug/issues/8090
 * https://github.com/xeno-by/sbt-example-paradise210/issues/1#issuecomment-20996354
 */
object Macros {

  private def currentClassLoaderExpr(
      c: blackbox.Context { type PrefixType = Reflect.type }): c.Expr[ClassLoader] = {
    import c.universe._
    val enclosingClassTree = c.reifyEnclosingRuntimeClass
    if (enclosingClassTree.isEmpty)
      c.abort(c.enclosingPosition, "call site does not have an enclosing class")
    val enclosingClassExpr = c.Expr[java.lang.Class[_]](enclosingClassTree)
    reify {
      enclosingClassExpr.splice.getClassLoader
    }
  }

  def lookupLoadableModuleClass(c: blackbox.Context { type PrefixType = Reflect.type })(
      fqcn: c.Expr[String]): c.Expr[Option[LoadableModuleClass]] = {
    import c.universe._
    val loaderExpr = currentClassLoaderExpr(c)
    reify {
      c.prefix.splice.lookupLoadableModuleClass(fqcn.splice, loaderExpr.splice)
    }
  }

  def lookupInstantiatableClass(c: blackbox.Context { type PrefixType = Reflect.type })(
      fqcn: c.Expr[String]): c.Expr[Option[InstantiatableClass]] = {
    import c.universe._
    val loaderExpr = currentClassLoaderExpr(c)
    reify {
      c.prefix.splice.lookupInstantiatableClass(fqcn.splice, loaderExpr.splice)
    }
  }
}
