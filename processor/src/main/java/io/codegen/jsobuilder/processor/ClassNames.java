package io.codegen.jsobuilder.processor;

import com.squareup.javapoet.ClassName;

public class ClassNames {

    private static final String JSINTEROP_PACKAGE = "jsinterop.annotations";

    public static final ClassName JSINTEROP_JSTYPE = ClassName.get(JSINTEROP_PACKAGE, "JsType");

    public static final ClassName JSINTEROP_JSIGNORE = ClassName.get(JSINTEROP_PACKAGE, "JsIgnore");

    public static final ClassName JSINTEROP_JSMETHOD = ClassName.get(JSINTEROP_PACKAGE, "JsMethod");

    public static final ClassName JSINTEROP_JSPROPERTY = ClassName.get(JSINTEROP_PACKAGE, "JsProperty");

    public static final ClassName JSINTEROP_JSOVERLAY = ClassName.get(JSINTEROP_PACKAGE, "JsOverlay");

}
