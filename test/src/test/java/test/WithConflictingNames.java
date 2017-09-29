package test;

import io.codegen.jsobuilder.annotations.JsBuilder;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

@JsBuilder
@JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
public class WithConflictingNames {

    /**
     * The name 'value' is used in the builder.
     */
    String[] value;

    /**
     * The name 'array' is used in the builder.
     */
    String[] array;

    /**
     * The name 'object' is used in the builder.
     */
    String[] object;

    /**
     * The name 'size' is used in the builder.
     */
    String[] size;

}
