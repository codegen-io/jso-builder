package io.codegen.jsobuilder.integration;

import java.util.function.Supplier;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public abstract class AbstractJSO {

    @JsProperty
    String stringProperty;

    @JsProperty
    int intProperty;

    @JsProperty
    boolean booleanProperty;

    String propertyWithoutAnnotation;

    @JsIgnore
    String ignoredProperty;

    @JsOverlay
    String overlayedProperty = new String("overlayed");

    public static class Builder extends AbstractJSOJSOBuilder {
        public Builder(Supplier<? extends AbstractJSO> supplier) {
            super(supplier);
        }
    }

}
