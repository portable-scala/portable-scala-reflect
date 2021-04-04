package org.portablescala.reflect

/** A wrapper for a module class that can be loaded.
 *
 *  @param runtimeClass
 *    The `java.lang.Class[_]` representing the module class.
 */
final class LoadableModuleClass private[reflect] (val runtimeClass: Class[_]) {

  /** Loads the module instance and returns it.
   *
   *  If the underlying constructor throws an exception `e`, then `loadModule`
   *  throws `e`, unlike `java.lang.reflect.Field.get` which would wrap it in a
   *  `java.lang.reflect.ExceptionInInitializerError`.
   */
  def loadModule(): Any = {
    try {
      runtimeClass.getField("MODULE$").get(null)
    } catch {
      case e: ExceptionInInitializerError if e.getCause != null =>
        throw e.getCause
    }
  }
}
