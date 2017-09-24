package io.codegen.jsobuilder.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * JsBuilder annotation which triggers the generation of the builder class.
 */
@Retention(SOURCE)
@Target(TYPE)
public @interface JsBuilder {

}
