package test;

import io.codegen.jsobuilder.annotations.JsBuilder;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class PrimitivesJSO {

    boolean booleanProperty;

    byte byteProperty;

    double doubleProperty;

    float floatProperty;

    int intProperty;

    long longProperty;

    short shortProperty;

    @JsBuilder
    public static class Builder extends PrimitivesJSO {}

}
