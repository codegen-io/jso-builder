package test;

import io.codegen.jsobuilder.annotations.JsBuilder;
import jsinterop.annotations.JsType;

@JsBuilder
@JsType(isNative = true, namespace = "codegen", name = "Object")
public abstract class NonGlobalJsType {}
