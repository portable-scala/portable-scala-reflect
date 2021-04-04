package org.portablescala.reflect

import java.lang.reflect.InvocationTargetException

/** A wrapper for a class that can be instantiated.
 *
 *  @param runtimeClass
 *    The `java.lang.Class[_]` representing the class.
 */
final class InstantiatableClass private[reflect] (val runtimeClass: Class[_]) {

  /** A list of the public constructors declared in this class. */
  val declaredConstructors: List[InvokableConstructor] =
    runtimeClass.getConstructors.map(new InvokableConstructor(_)).toList

  /** Instantiates this class using its zero-argument constructor.
   *
   *  @throws java.lang.InstantiationException
   *    (caused by a `NoSuchMethodException`)
   *    If this class does not have a public zero-argument constructor.
   */
  def newInstance(): Any = {
    try {
      runtimeClass.getDeclaredConstructor().newInstance()
    } catch {
      case e: InvocationTargetException if e.getCause != null =>
        throw e.getCause
      case e: NoSuchMethodException =>
        throw new InstantiationException(runtimeClass.getName).initCause(e)
    }
  }

  /** Looks up a public constructor identified by the types of its formal
   *  parameters.
   *
   *  If no such public constructor exists, returns `None`.
   */
  def getConstructor(parameterTypes: Class[_]*): Option[InvokableConstructor] = {
    try {
      Some(new InvokableConstructor(runtimeClass.getConstructor(parameterTypes: _*)))
    } catch {
      case _: NoSuchMethodException => None
    }
  }
}
