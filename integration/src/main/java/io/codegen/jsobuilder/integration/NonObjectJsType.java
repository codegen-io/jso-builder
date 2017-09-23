package io.codegen.jsobuilder.integration;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Window")
public abstract class NonObjectJsType {}
