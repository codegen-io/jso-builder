# JSO Builder

[![Maven Central](https://img.shields.io/maven-central/v/io.codegen.jso-builder/jso-builder-processor.svg)](https://mvnrepository.com/artifact/io.codegen.jso-builder/jso-builder-processor)
[![Travis](https://img.shields.io/travis/codegen-io/jso-builder.svg)](https://travis-ci.org/codegen-io/jso-builder)

The JSO Builder project enables one to automatically generate builder classes from GWT JsInterop
JavaScript objects. The builder classes are compatible with GWT and allow the creation of unit
tests in a JVM environment.

## Usage

```java
// Define a concrete class annotated as a native JsType object
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class Value {

    // Each member can be set using a corresponding with* method in the builder
    String property;
    int number;

    // The JsBuilder annotation will trigger the processor and generate ValueJSOBuilder
    // which our builder will inherit in order to expose it's methods
    @JsBuilder
    public static class Builder extends ValueJSOBuilder {}

    // The generated builder allows serializing the JavaScript object to JSON
    // from both a GWT application and a JRE environment like a unit test
    @JsOverlay
    public String toJSON() {
        return Builder.toJSON(this);
    }

}

// Use the generated builder to create our Value
Value value =
    new Value.Builder()
        .withProperty("one")
        .withNumber(2)
        .build();

// Serialize the Value to JSON
value.toJSON();
```

## Installation

You will need to include `jso-builder-annotations-x.y.z.jar` in your build classpath at compile
time and add `jso-builder-processor-x.y.z.jar` to the annotation path in order to activate the
annotation processor and generate the builder implementations.

### Maven

In a Maven project, include the `jso-builder-annotations` artifact in the dependencies section
of your `pom.xml` and the `jso-builder-processor` artifact as an `annotationProcessorPaths`
value of the `maven-compiler-plugin`:

```xml
<dependencies>
  <dependency>
    <groupId>io.codegen.jso-builder</groupId>
    <artifactId>jso-builder-annotations</artifactId>
    <version>x.y.z</version>
  </dependency>
</dependencies>
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <version>3.6.1</version>
      <configuration>
        <annotationProcessorPaths>
          <path>
            <groupId>io.codegen.jso-builder</groupId>
            <artifactId>jso-builder-processor</artifactId>
            <version>x.y.z</version>
          </path>
        </annotationProcessorPaths>
      </configuration>
    </plugin>
  </plugins>
</build>
```
