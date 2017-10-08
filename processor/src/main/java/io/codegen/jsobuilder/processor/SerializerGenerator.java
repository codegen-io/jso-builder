package io.codegen.jsobuilder.processor;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

public class SerializerGenerator {

    public TypeSpec createSerializer(ClassName className) {
        return TypeSpec.classBuilder(className)
                .addModifiers(Modifier.STATIC)
                .superclass(className.peerClass("Serializer"))
                .addMethod(createToJSONMethod())
                .addMethod(createIsJsObject())
                .addMethod(createWriteJSONMethod())
                .addMethod(createWriteValueMethod())
                .build();
    }

    private MethodSpec createToJSONMethod() {
        return MethodSpec.methodBuilder("toJSON")
                .addModifiers(Modifier.FINAL)
                .addAnnotation(ClassNames.GWT_INCOMPATIBLE)
                .addParameter(Object.class, "object")
                .returns(String.class)
                .beginControlFlow("if (!isJsObject(object.getClass()))")
                    .addStatement("throw new $T($S + object.getClass() + $S)", IllegalStateException.class,
                            "Class ", " isn't a JavaScript object")
                .endControlFlow()
                .addStatement("$T writer = new $T()", StringWriter.class, StringWriter.class)
                .beginControlFlow("try")
                    .addStatement("writeJSON(writer, object)")
                .nextControlFlow("catch ($T e)", IOException.class)
                    .addStatement("throw new $T(e)", RuntimeException.class)
                .endControlFlow()
                .addStatement("return writer.toString()")
                .build();
    }

    private MethodSpec createIsJsObject() {
        CodeBlock.Builder builder = CodeBlock.builder();
        builder.addStatement("$1T jsType = type.getAnnotation($1T.class)", ClassNames.JSINTEROP_JSTYPE);
        Stream.of(
                "jsType == null",
                "!jsType.isNative()",
                "!JsPackage.GLOBAL.equals(jsType.namespace())",
                "!\"Object\".equals(jsType.name())")
            .forEachOrdered(statement -> builder
                    .beginControlFlow("if (" + statement + ")")
                        .addStatement("return false")
                    .endControlFlow());
        builder.addStatement("return true");

        return MethodSpec.methodBuilder("isJsObject")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .addAnnotation(ClassNames.GWT_INCOMPATIBLE)
                .addParameter(ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(Object.class)), "type")
                .returns(Boolean.TYPE)
                .addCode(builder.build())
                .build();
    }

    private MethodSpec createWriteJSONMethod() {
        return MethodSpec.methodBuilder("writeJSON")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .addAnnotation(ClassNames.GWT_INCOMPATIBLE)
                .addParameter(Writer.class, "writer")
                .addParameter(Object.class, "value")
                .addException(IOException.class)

                .beginControlFlow("if (value == null)")
                    .addStatement("writer.append($S)", "null")
                    .addStatement("return")
                .endControlFlow()

                .addStatement("$T type = value.getClass()", ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(Object.class)))

                .beginControlFlow("if (type.isPrimitive())")
                    .addStatement("writer.append(String.valueOf(value))")

                .nextControlFlow("else if (type.isArray())")
                    .addStatement("writer.append('[')")
                    .addStatement("int arraySize = $T.getLength(value)", Array.class)
                    .beginControlFlow("for (int arrayIndex = 0; arrayIndex < arraySize; arrayIndex++)")
                        .addStatement("writeJSON(writer, $T.get(value, arrayIndex))", Array.class)
                        .beginControlFlow("if (arrayIndex < (arraySize - 1))")
                            .addStatement("writer.append(',')")
                        .endControlFlow()
                    .endControlFlow()
                    .addStatement("writer.append(']')")

                .nextControlFlow("else if ($T.class.isAssignableFrom(type))", Number.class)
                    .addStatement("writer.append($T.valueOf(value))", String.class)

                .nextControlFlow("else if ($T.class.isAssignableFrom(type))", Boolean.class)
                    .addStatement("writer.append($T.valueOf(value))", String.class)

                .nextControlFlow("else if ($T.class.isAssignableFrom(type))", CharSequence.class)
                    .addStatement("writer.append('\"')")
                    .addStatement("writeValue(writer, $T.valueOf(value))", String.class)
                    .addStatement("writer.append('\"')")

                .nextControlFlow("else if (isJsObject(type))")
                    .addStatement("writer.append('{')")
                    .addStatement("$T[] fields = type.getDeclaredFields()", Field.class)
                    .addStatement("$T<String, Field> fieldByName = new $T<>()", SortedMap.class, TreeMap.class)

                    .beginControlFlow("for (int fieldIndex = 0; fieldIndex < fields.length; fieldIndex++)")
                        .addStatement("$T field = fields[fieldIndex]", Field.class)
                        .beginControlFlow("if (field.getAnnotation($T.class) != null)", ClassNames.JSINTEROP_JSIGNORE)
                            .addStatement("continue")
                        .endControlFlow()
                        .beginControlFlow("if (field.getAnnotation($T.class) != null)", ClassNames.JSINTEROP_JSOVERLAY)
                            .addStatement("continue")
                        .endControlFlow()
                        .addStatement("$T name", String.class)
                        .addStatement("$1T property = field.getAnnotation($1T.class)", ClassNames.JSINTEROP_JSPROPERTY)
                        .beginControlFlow("if (property != null && !$S.equals(property.name()))", "<auto>")
                            .addStatement("name = property.name()")
                         .nextControlFlow("else")
                             .addStatement("name = field.getName()")
                        .endControlFlow()
                        .addStatement("fieldByName.put(name, field)")
                    .endControlFlow()

                    .addStatement("boolean firstProperty = true")
                    .beginControlFlow("for ($T<$T, $T> entry : fieldByName.entrySet())", Map.Entry.class, String.class, Field.class)
                        .beginControlFlow("if (!firstProperty)")
                            .addStatement("writer.append(',')")
                        .endControlFlow()

                        .addStatement("writer.append('\"')")
                        .addStatement("writeValue(writer, entry.getKey())")
                        .addStatement("writer.append('\"')")
                        .addStatement("writer.append(':')")

                        .beginControlFlow("try")
                            .addStatement("writeJSON(writer, entry.getValue().get(value))")
                        .nextControlFlow("catch ($T | $T e)", IllegalArgumentException.class, IllegalAccessException.class)
                            .addStatement("throw new $T(e)", RuntimeException.class)
                        .endControlFlow()
                        .addStatement("firstProperty = false")
                    .endControlFlow()

                    .addStatement("writer.append('}')")
                .nextControlFlow("else")
                    .addStatement("throw new $T($S + type)", IllegalArgumentException.class, "Unknown type ")
                .endControlFlow()
                .build();
    }

    private MethodSpec createWriteValueMethod() {
        CodeBlock.Builder builder = CodeBlock.builder();
        builder
            .beginControlFlow("for (int index = 0; index < value.length(); index++)")
                .addStatement("char currentCharacter = value.charAt(index)")
                .beginControlFlow("switch (currentCharacter)");

        IntStream.of('\\', '"', 'b', 't', 'n', 'f', 'r')
            .forEach(character -> builder.unindent()
                    .add("case '\\$L':\n", Character.toString((char) character))
                    .indent()
                    .addStatement("writer.write($S)", "\\" + Character.toString((char) character))
                    .addStatement("break")
                    );

        builder.unindent().add("default:\n").indent();

        builder.add("if (");
        builder.add(Stream.of(
                CharacterRange.of('\u0000', '\u001f'),
                CharacterRange.of('\u007f', '\u009f'),
                CharacterRange.of('\u2028', '\u2029'))
            .map(CharacterRange::asCondition)
            .collect(Collectors.joining("\n || ")));

        builder.add(") {\n").indent();

        builder
            .addStatement("writer.write($S)", "\\u")
            .addStatement("$T hexValue = $T.toHexString(currentCharacter)", String.class, Integer.class)
            .addStatement("writer.write($S, 0, 4 - hexValue.length())", "0000")
            .addStatement("writer.write(hexValue)");

        builder.unindent().add("} else {\n").indent();
        builder.addStatement("writer.write(currentCharacter)");
        builder.unindent().add("}\n");

        builder.endControlFlow();

        builder.endControlFlow();

        return MethodSpec.methodBuilder("writeValue")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .addAnnotation(ClassNames.GWT_INCOMPATIBLE)
                .addParameter(Writer.class, "writer")
                .addParameter(CharSequence.class, "value")
                .addException(IOException.class)
                .addCode(builder.build())
                .build();
    }

    private static final class CharacterRange {
        private final char lowerBound;
        private final char upperBound;

        private CharacterRange(char lowerBound, char upperBound) {
            this.lowerBound = lowerBound;
            this.upperBound = upperBound;
        }

        private String asCondition() {
            return "(currentCharacter >= '\\u" + toHexString(lowerBound)
                    + "' && currentCharacter <= '\\u" + toHexString(upperBound) + "')";
        }

        private String toHexString(char character) {
            String hexValue = Integer.toHexString(character);
            return "0000".concat(hexValue).substring(hexValue.length());
        }

        private static CharacterRange of (char lowerBound, char upperBound) {
            return new CharacterRange(lowerBound, upperBound);
        }

    }

}
