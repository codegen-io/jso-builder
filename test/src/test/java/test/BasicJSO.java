package test;

import io.codegen.jsobuilder.annotations.JsBuilder;
import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class BasicJSO {

    @JsProperty
    String stringProperty;

    @JsProperty
    int intProperty;

    String propertyWithoutAnnotation;

    @JsIgnore
    String ignoredProperty;

    @JsOverlay
    String overlayedProperty = new String("overlayed");

    String[] stringArrayProperty;

    @JsBuilder
    public static class Builder extends BasicJSOJSOBuilder {}

}
