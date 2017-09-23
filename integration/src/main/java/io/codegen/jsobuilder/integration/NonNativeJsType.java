package io.codegen.jsobuilder.integration;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = false, namespace = JsPackage.GLOBAL, name = "Object")
public abstract class NonNativeJsType {}
