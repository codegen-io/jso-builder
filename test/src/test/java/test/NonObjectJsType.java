package test;

import io.codegen.jsobuilder.annotations.JsBuilder;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsBuilder
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Window")
public abstract class NonObjectJsType {}
