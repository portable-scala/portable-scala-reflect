package org.portablescala.reflect

import java.lang.reflect._

/** A description of a constructor that can reflectively invoked. */
final class InvokableConstructor private[reflect] (ctor: Constructor[_]) {

  /** The `Class[_]` objects representing the formal parameters of this
   *  constructor.
   */
  val parameterTypes: List[Class[_]] = ctor.getParameterTypes.toList

  /** Invokes this constructor to instantiate a new object.
   *
   *  If the underlying constructor throws an exception `e`, then `newInstance`
   *  throws `e`, unlike `java.lang.reflect.Constructor.newInstance` which
   *  would wrap it in a `java.lang.reflect.InvocationTargetException`.
   *
   *  @param args
   *    The formal arguments to be given to the constructor.
   */
  def newInstance(args: Any*): Any = {
    try {
      ctor.newInstance(args.asInstanceOf[Seq[AnyRef]]: _*)
    } catch {
      case e: InvocationTargetException if e.getCause != null =>
        throw e.getCause
    }
  }
}
