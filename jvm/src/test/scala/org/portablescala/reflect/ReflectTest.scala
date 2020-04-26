package org.portablescala.reflect

import scala.reflect.ClassTag

import org.junit.Assert._
import org.junit.Assume._
import org.junit.Test

import org.portablescala.reflect.annotation.EnableReflectiveInstantiation

package subpackage {
  @EnableReflectiveInstantiation
  private class PrivateClassEnableDirect {
    override def toString(): String = "instance of PrivateClassEnableDirect"
  }
}

class ReflectTest {
  import ReflectTest.{Accessors, VC, ConstructorThrowsMessage, intercept}

  private final val Prefix = "org.portablescala.reflect.ReflectTest$"

  private final val NameClassEnableDirect =
    Prefix + "ClassEnableDirect"
  private final val NameClassEnableDirectNoZeroArgCtor =
    Prefix + "ClassEnableDirectNoZeroArgCtor"
  private final val NameObjectEnableDirect =
    Prefix + "ObjectEnableDirect$"
  private final val NameTraitEnableDirect =
    Prefix + "TraitEnableDirect"
  private final val NameAbstractClassEnableDirect =
    Prefix + "AbstractClassEnableDirect"
  private final val NameClassNoPublicConstructorEnableDirect =
    Prefix + "ClassNoPublicConstructorEnableDirect"

  private final val NameInnerClass = {
    Prefix + "ClassWithInnerClassWithEnableReflectiveInstantiation$" +
    "InnerClassWithEnableReflectiveInstantiation"
  }

  private final val NameClassEnableIndirect =
    Prefix + "ClassEnableIndirect"
  private final val NameClassEnableIndirectNoZeroArgCtor =
    Prefix + "ClassEnableIndirectNoZeroArgCtor"
  private final val NameObjectEnableIndirect =
    Prefix + "ObjectEnableIndirect$"
  private final val NameTraitEnableIndirect =
    Prefix + "TraitEnableIndirect"
  private final val NameAbstractClassEnableIndirect =
    Prefix + "AbstractClassEnableIndirect"
  private final val NameClassNoPublicConstructorEnableIndirect =
    Prefix + "ClassNoPublicConstructorEnableIndirect"

  private final val NameClassDisable =
    Prefix + "ClassDisable"
  private final val NameObjectDisable =
    Prefix + "ObjectDisable$"
  private final val NameTraitDisable =
    Prefix + "TraitDisable"

  private final val NameInnerObject = {
    Prefix + "ClassWithInnerObjectWithEnableReflectiveInstantiation$" +
    "InnerObjectWithEnableReflectiveInstantiation"
  }

  private final val NameObjectWithInitialization =
    Prefix + "ObjectWithInitialization$"
  private final val NameObjectWithThrowingCtor =
    Prefix + "ObjectWithThrowingCtor$"
  private final val NameClassWithThrowingCtor =
    Prefix + "ClassWithThrowingCtor"

  private final val NamePrivateClassEnableDirect =
    "org.portablescala.reflect.subpackage.PrivateClassEnableDirect"

  @Test def testClassRuntimeClass(): Unit = {
    def test(name: String): Unit = {
      val optClassData = Reflect.lookupInstantiatableClass(name)
      assertTrue(name, optClassData.isDefined)
      val classData = optClassData.get

      val runtimeClass = optClassData.get.runtimeClass
      assertEquals(name, name, runtimeClass.getName)
    }

    test(NameClassEnableDirect)
    test(NameClassEnableDirectNoZeroArgCtor)
    test(NameClassEnableIndirect)
    test(NameClassEnableIndirectNoZeroArgCtor)
  }

  @Test def testObjectRuntimeClass(): Unit = {
    def test(name: String): Unit = {
      val optClassData = Reflect.lookupLoadableModuleClass(name)
      assertTrue(name, optClassData.isDefined)
      val classData = optClassData.get

      val runtimeClass = optClassData.get.runtimeClass
      assertEquals(name, name, runtimeClass.getName)
    }

    test(NameObjectEnableDirect)
    test(NameObjectEnableIndirect)
  }

  @Test def testClassCannotBeFound(): Unit = {
    def test(name: String): Unit =
      assertTrue(name, Reflect.lookupInstantiatableClass(name).isEmpty)

    test(NameObjectEnableDirect)
    test(NameTraitEnableDirect)
    test(NameAbstractClassEnableDirect)
    test(NameClassNoPublicConstructorEnableDirect)
    test(NameObjectEnableIndirect)
    test(NameTraitEnableIndirect)
    test(NameAbstractClassEnableIndirect)
    test(NameClassNoPublicConstructorEnableIndirect)
    test(NameClassDisable)
    test(NameObjectDisable)
    test(NameTraitDisable)
  }

  @Test def testObjectCannotBeFound(): Unit = {
    def test(name: String): Unit =
      assertTrue(name, Reflect.lookupLoadableModuleClass(name).isEmpty)

    test(NameClassEnableDirect)
    test(NameClassEnableDirectNoZeroArgCtor)
    test(NameTraitEnableDirect)
    test(NameAbstractClassEnableDirect)
    test(NameClassNoPublicConstructorEnableDirect)
    test(NameClassEnableIndirect)
    test(NameTraitEnableIndirect)
    test(NameAbstractClassEnableIndirect)
    test(NameClassNoPublicConstructorEnableIndirect)
    test(NameClassDisable)
    test(NameObjectDisable)
    test(NameTraitDisable)
  }

  @Test def testClassNoArgCtor(): Unit = {
    for (name <- Seq(NameClassEnableDirect, NameClassEnableIndirect)) {
      val optClassData = Reflect.lookupInstantiatableClass(name)
      assertTrue(name, optClassData.isDefined)
      val classData = optClassData.get

      val instance = classData.newInstance().asInstanceOf[Accessors]
      assertEquals(name, -1, instance.x)
      assertEquals(name, name.stripPrefix(Prefix), instance.y)
    }
  }

  @Test def testClassNoArgCtorErrorCase(): Unit = {
    for (name <- Seq(NameClassEnableDirectNoZeroArgCtor,
        NameClassEnableIndirectNoZeroArgCtor)) {
      val optClassData = Reflect.lookupInstantiatableClass(name)
      assertTrue(name, optClassData.isDefined)
      val classData = optClassData.get

      intercept[InstantiationException](classData.newInstance())
    }
  }

  @Test def testClassCtorWithArgs(): Unit = {
    for (name <- Seq(NameClassEnableDirect, NameClassEnableDirectNoZeroArgCtor,
        NameClassEnableIndirect, NameClassEnableIndirectNoZeroArgCtor)) {
      val optClassData = Reflect.lookupInstantiatableClass(name)
      assertTrue(optClassData.isDefined)
      val classData = optClassData.get

      val optCtorIntString =
        classData.getConstructor(classOf[Int], classOf[String])
      assertTrue(optCtorIntString.isDefined)
      val instanceIntString =
        optCtorIntString.get.newInstance(543, "foobar").asInstanceOf[Accessors]
      assertEquals(543, instanceIntString.x)
      assertEquals("foobar", instanceIntString.y)

      val optCtorInt = classData.getConstructor(classOf[Int])
      assertTrue(optCtorInt.isDefined)
      val instanceInt =
        optCtorInt.get.newInstance(123).asInstanceOf[Accessors]
      assertEquals(123, instanceInt.x)
      assertEquals(name.stripPrefix(Prefix), instanceInt.y)

      // Value class is seen as its underlying
      val optCtorShort = classData.getConstructor(classOf[Short])
      assertTrue(optCtorShort.isDefined)
      val instanceShort =
        optCtorShort.get.newInstance(21.toShort).asInstanceOf[Accessors]
      assertEquals(42, instanceShort.x)
      assertEquals(name.stripPrefix(Prefix), instanceShort.y)

      // Non-existent
      assertFalse(classData.getConstructor(classOf[Boolean]).isDefined)
      assertFalse(classData.getConstructor(classOf[VC]).isDefined)

      // Non-public
      assertFalse(classData.getConstructor(classOf[String]).isDefined)
      assertFalse(classData.getConstructor(classOf[Double]).isDefined)
    }
  }

  @Test def testInnerClass(): Unit = {
    val outer = new ReflectTest.ClassWithInnerClassWithEnableReflectiveInstantiation(15)

    val optClassData = Reflect.lookupInstantiatableClass(NameInnerClass)
    assertTrue(optClassData.isDefined)
    val classData = optClassData.get

    val optCtorOuterString =
      classData.getConstructor(outer.getClass, classOf[String])
    assertTrue(optCtorOuterString.isDefined)
    val instanceOuterString =
      optCtorOuterString.get.newInstance(outer, "babar").asInstanceOf[Accessors]
    assertEquals(15, instanceOuterString.x)
    assertEquals("babar", instanceOuterString.y)
  }

  private val classInsideLambdaInsideCtor: () => Class[_] = { () =>
    @EnableReflectiveInstantiation
    class LocalClassWithEnableReflectiveInstantiationInsideLambdaInsideCtor

    classOf[LocalClassWithEnableReflectiveInstantiationInsideLambdaInsideCtor]
  }

  @Test def testLocalClass(): Unit = {
    def assertCannotFind(c: Class[_]): Unit = {
      val fqcn = c.getName
      assertFalse(fqcn, Reflect.lookupInstantiatableClass(fqcn).isDefined)
    }

    // Inside a method
    @EnableReflectiveInstantiation
    class LocalClassWithEnableReflectiveInstantiationInsideMethod

    assertCannotFind(
        classOf[LocalClassWithEnableReflectiveInstantiationInsideMethod])

    assumeFalse(
        "Scala/JVM 2.10.x does not correctly configure classes in lambdas as local",
        TestPlatform.isScala210OnJVM)

    // In a lambda whose owner is ultimately the constructor of the class
    assertCannotFind(classInsideLambdaInsideCtor())

    // Inside lambda whose owner is a method
    val f = { () =>
      @EnableReflectiveInstantiation
      class LocalClassWithEnableReflectiveInstantiationInsideLambdaInsideMethod

      assertCannotFind(
          classOf[LocalClassWithEnableReflectiveInstantiationInsideLambdaInsideMethod])
    }
    f()
  }

  @Test def testObjectLoad(): Unit = {
    for (name <- Seq(NameObjectEnableDirect, NameObjectEnableIndirect)) {
      val optClassData = Reflect.lookupLoadableModuleClass(name)
      assertTrue(name, optClassData.isDefined)
      val classData = optClassData.get

      val instance = classData.loadModule().asInstanceOf[Accessors]
      assertEquals(name, 101, instance.x)
      assertEquals(name, name.stripPrefix(Prefix), instance.y)
    }
  }

  @Test def testObjectLoadInitialization(): Unit = {
    assertFalse(ReflectTest.ObjectWithInitializationHasBeenInitialized)
    val optClassData =
      Reflect.lookupLoadableModuleClass(NameObjectWithInitialization)
    assertTrue(optClassData.isDefined)
    val classData = optClassData.get
    assertFalse(ReflectTest.ObjectWithInitializationHasBeenInitialized)

    classData.loadModule()
    assertTrue(ReflectTest.ObjectWithInitializationHasBeenInitialized)
  }

  @Test def testExceptionsInConstructor(): Unit = {
    val objClassData =
      Reflect.lookupLoadableModuleClass(NameObjectWithThrowingCtor).get
    val e1 = intercept[ArithmeticException] {
      objClassData.loadModule()
    }
    assertEquals(ConstructorThrowsMessage, e1.getMessage)

    val clsClassData =
      Reflect.lookupInstantiatableClass(NameClassWithThrowingCtor).get
    val e2 = intercept[ArithmeticException] {
      clsClassData.newInstance()
    }
    assertEquals(ConstructorThrowsMessage, e2.getMessage)

    val ctor = clsClassData.getConstructor().get
    val e3 = intercept[ArithmeticException] {
      ctor.newInstance()
    }
    assertEquals(ConstructorThrowsMessage, e3.getMessage)
  }

  @Test def testPrivateClass(): Unit = {
    // Private classes are discoverable

    val optClassData = Reflect.lookupInstantiatableClass(NamePrivateClassEnableDirect)
    assertTrue("1", optClassData.isDefined)
    val classData = optClassData.get

    val obj = classData.newInstance()
    assertEquals("2", "instance of PrivateClassEnableDirect", obj.toString())
  }

  @Test def testInnerObjectWithEnableReflectiveInstantiation_issue_3228(): Unit = {
    assertFalse(Reflect.lookupLoadableModuleClass(NameInnerObject).isDefined)
    assertFalse(Reflect.lookupInstantiatableClass(NameInnerObject).isDefined)
  }

  @Test def testLocalClassWithReflectiveInstantiationInLambda_issue_3227(): Unit = {
    // Test that the presence of the following code does not prevent linking
    val f = { () =>
      @EnableReflectiveInstantiation
      class Foo
    }
    identity(f) // discard f without compiler warning
  }
}

object ReflectTest {
  private final val ConstructorThrowsMessage = "constructor throws"

  def intercept[T <: Throwable : ClassTag](body: => Unit): T = {
    try {
      body
      throw new AssertionError("no exception was thrown")
    } catch {
      case t: T => t
    }
  }

  trait Accessors {
    val x: Int
    val y: String
  }

  final class VC(val self: Short) extends AnyVal

  /* FIXME In the classes below, protected constructors are commented out,
   * because Scala.js and Scala/JVM do not agree on whether they should be
   * visible. Scala.js says no, but Scala/JVM compiles them as public, and
   * therefore says yes.
   */

  // Entities with directly enabled reflection

  @EnableReflectiveInstantiation
  class ClassEnableDirect(val x: Int, val y: String) extends Accessors {
    def this(x: Int) = this(x, "ClassEnableDirect")
    def this() = this(-1)
    def this(vc: VC) = this(vc.self.toInt * 2)

    //protected def this(y: String) = this(-5, y)
    private def this(d: Double) = this(d.toInt)
  }

  @EnableReflectiveInstantiation
  class ClassEnableDirectNoZeroArgCtor(val x: Int, val y: String)
      extends Accessors {
    def this(x: Int) = this(x, "ClassEnableDirectNoZeroArgCtor")
    def this(vc: VC) = this(vc.self.toInt * 2)

    //protected def this(y: String) = this(-5, y)
    private def this(d: Double) = this(d.toInt)
  }

  @EnableReflectiveInstantiation
  object ObjectEnableDirect extends Accessors {
    val x = 101
    val y = "ObjectEnableDirect$"
  }

  @EnableReflectiveInstantiation
  trait TraitEnableDirect extends Accessors

  @EnableReflectiveInstantiation
  abstract class AbstractClassEnableDirect(val x: Int, val y: String)
      extends Accessors {

    def this(x: Int) = this(x, "AbstractClassEnableDirect")
    def this() = this(-1)
    def this(vc: VC) = this(vc.self.toInt * 2)

    //protected def this(y: String) = this(-5, y)
    private def this(d: Double) = this(d.toInt)
  }

  @EnableReflectiveInstantiation
  class ClassNoPublicConstructorEnableDirect private (val x: Int, val y: String)
      extends Accessors {

    //protected def this(y: String) = this(-5, y)
  }

  class ClassWithInnerClassWithEnableReflectiveInstantiation(_x: Int) {
    @EnableReflectiveInstantiation
    class InnerClassWithEnableReflectiveInstantiation(_y: String)
        extends Accessors {
      val x = _x
      val y = _y
    }
  }

  // Entities with reflection enabled by inheritance

  @EnableReflectiveInstantiation
  trait EnablingTrait

  class ClassEnableIndirect(val x: Int, val y: String)
      extends EnablingTrait with Accessors {

    def this(x: Int) = this(x, "ClassEnableIndirect")
    def this() = this(-1)
    def this(vc: VC) = this(vc.self.toInt * 2)

    //protected def this(y: String) = this(-5, y)
    private def this(d: Double) = this(d.toInt)
  }

  class ClassEnableIndirectNoZeroArgCtor(val x: Int, val y: String)
      extends EnablingTrait with Accessors {
    def this(x: Int) = this(x, "ClassEnableIndirectNoZeroArgCtor")
    def this(vc: VC) = this(vc.self.toInt * 2)

    //protected def this(y: String) = this(-5, y)
    private def this(d: Double) = this(d.toInt)
  }

  object ObjectEnableIndirect extends EnablingTrait with Accessors {
    val x = 101
    val y = "ObjectEnableIndirect$"
  }

  trait TraitEnableIndirect extends EnablingTrait with Accessors

  abstract class AbstractClassEnableIndirect(val x: Int, val y: String)
      extends EnablingTrait with Accessors {

    def this(x: Int) = this(x, "AbstractClassEnableIndirect")
    def this() = this(-1)
    def this(vc: VC) = this(vc.self.toInt * 2)

    //protected def this(y: String) = this(-5, y)
    private def this(d: Double) = this(d.toInt)
  }

  class ClassNoPublicConstructorEnableIndirect private (
      val x: Int, val y: String)
      extends EnablingTrait with Accessors {

    //protected def this(y: String) = this(-5, y)
  }

  // Entities with reflection disabled

  class ClassDisable(val x: Int, val y: String) extends Accessors

  object ObjectDisable extends Accessors {
    val x = 101
    val y = "ObjectDisable$"
  }

  trait TraitDisable extends Accessors

  // Corner cases

  var ObjectWithInitializationHasBeenInitialized: Boolean = false

  @EnableReflectiveInstantiation
  object ObjectWithInitialization {
    ObjectWithInitializationHasBeenInitialized = true
  }

  @EnableReflectiveInstantiation
  object ObjectWithThrowingCtor {
    throw new ArithmeticException(ConstructorThrowsMessage)
  }

  @EnableReflectiveInstantiation
  class ClassWithThrowingCtor {
    throw new ArithmeticException(ConstructorThrowsMessage)
  }

  // Regression cases

  class ClassWithInnerObjectWithEnableReflectiveInstantiation {
    @EnableReflectiveInstantiation
    object InnerObjectWithEnableReflectiveInstantiation
  }
}
