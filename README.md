# portable-scala-reflect: platform-agnostic reflection for Scala

[![Build Status](https://travis-ci.org/portable-scala/portable-scala-reflect.svg?branch=master)](https://travis-ci.org/portable-scala/portable-scala-reflect)
[![Scala.js](https://www.scala-js.org/assets/badges/scalajs-0.6.29.svg)](https://www.scala-js.org/)
[![Scala.js](https://www.scala-js.org/assets/badges/scalajs-1.0.0.svg)](https://www.scala-js.org)
[![Scaladoc](https://javadoc-badge.appspot.com/org.portable-scala/portable-scala-reflect_2.12.svg?label=scaladoc)](https://javadoc.io/doc/org.portable-scala/portable-scala-reflect_2.12/latest/org/portablescala/reflect/index.html)

The various platforms supported by Scala (JVM, JavaScript and Native) have varying support for run-time reflection.
Even the subset of functionality that is supported across the platforms is exposed through different APIs.

This library exposes a unified, portable API for run-time reflection in Scala.
It currently supports Scala/JVM and Scala.js, but will eventually support Scala Native as well (currently blocked on [scala-native#1279](https://github.com/scala-native/scala-native/issues/1279)).
To be portable, only the subset of reflection capabilities that is implementable across all platforms is exposed.

## Setup

Add the following line to your (cross-)project's settings in `build.sbt`:

```scala
libraryDependencies += "org.portable-scala" %%% "portable-scala-reflect" % "1.0.0"
```

`portable-scala-reflect` 1.0.0 supports:

* Scala 2.11.x, 2.12.x and 2.13.x
* Scala/JVM
* Scala.js 0.6.x and 1.x

## Usage

### Instantiate a class given its name

In order to reflectively instantiate a class, `portable-scala-reflect` demands that you "enable reflective instantiation" for it.
This is the case if:

* The class is annotated with `@org.portablescala.reflect.annotation.EnableReflectiveInstantiation`, or
* The class directly or indirectly extend a class or trait annotated with that annotation.

For example:

```scala
import org.portablescala.reflect.annotation.EnableReflectiveInstantiation

@EnableReflectiveInstantiation
class A // discoverable

@EnableReflectiveInstantiation
trait SuperTrait

class B extends SuperTrait // discoverable

class C extends B // discoverable

class D // NOT discoverable
```

In addition, a class must satisfy the following properties to be discoverable:

* It must be concrete
* It must have at least one public constructor
* It must not be a local class, i.e., defined inside a method

If a class is discoverable, you can use the method `lookupInstantiatableClass` of `org.portablescala.reflect.Reflect` to get an `InstantiatableClass` representing it using:

```scala
import org.portablescala.reflect._

val clsOpt = Reflect.lookupInstantiatableClass("fully.qualified.ClassName", someClassLoader)
```

The `someClassLoader` argument is optional, and defaults to the current class loader at call site.
It is only meaningful on the JVM.

Once you have an `InstantiatableClass`, you can use its methods to instantiate the class.
A typical use case is to instantiate the class using its no-argument constructor:

```scala
val cls = clsOpt.get // or any safer way to extract the Option
val instance = cls.newInstance()
```

For other constructors, you need to use `declaredConstructors` or `getConstructor()` to find the appropriate `InvokableConstructor`, given its parameter types:

```scala
val ctor = cls.getConstructor(classOf[Int], classOf[String])
val instance = ctor.newInstance(42, "hello")
```

Consult the Scaladoc of each method for more details (conditions, exceptional behavior, etc.).

### Load the singleton instance of an `object` given its name

Similarly to classes, you must enable reflective instantiation on an `object` to be able to reflectively load it.
In addition, the object must satisfy the following property to be discoverable:

* It must be "static", i.e., top-level or defined inside a static object

Use the method `Reflect.lookupLoadableModuleClass` to discover an object ("module" is the technical name of an `object` in Scala).

```scala
import org.portablescala.reflect._

val clsOpt = Reflect.lookupLoadableModuleClass("fully.qualified.ObjectName$", someClassLoader)
```

The `$` at the end of the object name is required.

Once you have a `LoadableModuleClass`, you can use its `loadModule()` method to load the singleton instance of the object:

```scala
val cls = clsOpt.get // or any safer way to extract the Option
val instance = cls.loadModule()
```

Consult the Scaladoc of each method for more details (conditions, exceptional behavior, etc.).

### Reflectively call methods

`portable-scala-reflect` does not provide any API to reflectively call methods.
If the name and signature of a method are statically known, it is possible to use a structural type in Scala instead, as follows:

```scala
val obj: Any = ??? // an object on which we want to call a method.

type ReflectiveAccess = {
  def theMethod(x: Int): Int
}

val result = obj.asInstanceOf[ReflectiveAccess].theMethod(42)
```

If the name or signature of the method is not statically known, you are out of luck: there is no way to perform such a reflective call in Scala.js nor Scala Native, so `portable-scala-reflect` does not provide any API for it.
