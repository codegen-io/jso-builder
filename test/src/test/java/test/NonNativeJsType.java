package test;

import io.codegen.jsobuilder.annotations.JsBuilder;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsBuilder
@JsType(isNative = false, namespace = JsPackage.GLOBAL, name = "Object")
public class NonNativeJsType {}
