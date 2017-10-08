package io.codegen.jsobuilder.processor;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic.Kind;

import org.immutables.metainf.Metainf;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.NameAllocator;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

@Metainf.Service(Processor.class)
public class JSOBuilderProcessor extends AbstractProcessor {

    private final Set<String> processedClasses = new HashSet<>();

    private Elements elementUtils = null;
    private Messager messager = null;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        this.elementUtils = processingEnv.getElementUtils();
        this.messager = processingEnv.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Arrays.asList(
                ClassNames.JS_BUILDER_ANNOTATION).stream()
                .map(ClassName::toString)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        Set<String> classesToProcess = collectClassNames(annotations, roundEnv);

        classesToProcess.stream()
            .filter(name -> !processedClasses.contains(name))
            .map(name -> elementUtils.getTypeElement(name))
            .forEach(this::generateBuilder);

        if (roundEnv.processingOver()) {
            processedClasses.clear();
        }

        return false;
    }

    private Set<String> collectClassNames(Set<? extends TypeElement> annotations, RoundEnvironment environment) {
        return annotations.stream()
                .flatMap(annotation -> environment.getElementsAnnotatedWith(annotation).stream())
                .map(this::getClassName)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    private Optional<String> getClassName(Element element) {
        Element type;

        // Determine if the class or the inner builder is annotated
        switch (element.getKind()) {
            case CLASS:
                if (ElementKind.CLASS.equals(element.getEnclosingElement().getKind())) {
                    type = element.getEnclosingElement();
                } else {
                    type = element;
                }
                break;
            default:
                emitError("Unkown element kind " + element.getKind(), element);
                return Optional.empty();
        }

        if (type.getModifiers().contains(Modifier.ABSTRACT)) {
            emitError("Type isn't a concrete JsType JavaScript object", type);
            return Optional.empty();
        }

        return Optional.of(type.asType().toString());
    }

    private void emitError(String message, Element element) {
        messager.printMessage(Kind.ERROR, message, element);
    }

    private void generateBuilder(TypeElement element) {
        if (!isJsObject(element)) {
            emitError("Type isn't a pure JsType JavaScript object", element);
            return;
        }

        ClassName className = ClassName.get(element);
        ClassName builderName = className.peerClass(className.simpleName() + "JSOBuilder");

        TypeSpec.Builder typeSpec = TypeSpec.classBuilder(builderName)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

        // Create object field
        typeSpec.addField(createObjectField(className));

        typeSpec.addMethods(getPropertyFields(element)
                .map(this::createPropertyMethod)
                .collect(Collectors.toList()));

        // Create build method
        typeSpec.addMethod(createBuildMethod(className, element));

        // Create toJSON method
        typeSpec.addMethod(createToJSONMethod(className));

        // Create JsArray type
        typeSpec.addType(createJsArrayInterface());

        // Create JSON type
        typeSpec.addType(createJsonInterface());

        // Create Global type
        typeSpec.addType(createGlobalInterface());

        // Create Serializer type
        typeSpec.addType(createSerializer(builderName));

        // Create JSON serializer
        typeSpec.addType(new SerializerGenerator().createSerializer(builderName.nestedClass("JreSerializer")));

        JavaFile javaFile = JavaFile.builder(builderName.packageName(), typeSpec.build())
                .skipJavaLangImports(true)
                .build();

        try {
            javaFile.writeTo(this.processingEnv.getFiler());
        } catch (IOException e) {
            emitError("Unable to write file: " + e.getMessage(), element);
        }
    }

    private Stream<? extends Element> getPropertyFields(TypeElement element) {
        return element.getEnclosedElements().stream()
                .filter(type -> ElementKind.FIELD.equals(type.getKind()))
                .filter(type -> type.getAnnotationMirrors().stream()
                        .map(mirror -> AnnotationSpec.get(mirror).type)
                        .noneMatch(annotation -> annotation.equals(ClassNames.JSINTEROP_JSIGNORE)
                                || annotation.equals(ClassNames.JSINTEROP_JSOVERLAY)));
    }

    private FieldSpec createObjectField(ClassName className) {
        return FieldSpec.builder(className, "object", Modifier.PRIVATE, Modifier.FINAL)
                .initializer("new $T()", className)
                .build();
    }

    private MethodSpec createPropertyMethod(Element element) {

        if (TypeKind.ARRAY.equals(element.asType().getKind())) {
            NameAllocator names = new NameAllocator();

            String name = names.newName(element.getSimpleName().toString());
            String array = names.newName("array");
            String value = names.newName("value");
            String size = names.newName("size");

            TypeMirror componentType = TypeMapper.asArrayType(element.asType()).getComponentType();

            CodeBlock code = CodeBlock.builder()
                    .beginControlFlow("if ($T.isClient())", ClassNames.GWT_SHARED_HELPER)
                        .addStatement("JsArray<$T> $L", componentType, array)
                        .beginControlFlow("if (this.object.$L != null)", name)
                            .addStatement("Object $L = this.object.$L", value, name)
                            .addStatement("$L = (JsArray<$T>) $L", array, componentType, value)
                        .nextControlFlow("else")
                            .addStatement("$L = new JsArray<>()", array)
                            .addStatement("Object $L = $L", value, array)
                            .addStatement("this.object.$L = ($T[]) $L", name, componentType, value)
                        .endControlFlow()
                        .beginControlFlow("for (int i = 0; i < $L.length; i++)", name)
                            .addStatement("$L.push($L[i])", array, name)
                        .endControlFlow()
                    .nextControlFlow("else")
                        .beginControlFlow("if (this.object.$L == null)", name)
                            .addStatement("this.object.$L = new $T[0]", name, componentType)
                        .endControlFlow()
                        .addStatement("this.object.$L = $T.concat("
                                + "\n$T.stream(this.object.$L), $T.stream($L))"
                                + "\n.toArray($L -> new $T[$L])",
                                name, Stream.class, Arrays.class, name, Arrays.class, name, size, componentType, size)
                    .endControlFlow()
                    .addStatement("return this")
                    .build();

            return MethodSpec.methodBuilder(getWithMethodName(element))
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class)
                            .addMember("value", "$S", "unchecked")
                            .build())
                    .returns(getBuilderName(element.getEnclosingElement()))
                    .addParameter(ParameterSpec.builder(TypeName.get(element.asType()), element.getSimpleName().toString())
                            .build())
                    .varargs()
                    .addCode(code)
                    .build();
        } else {
            return MethodSpec.methodBuilder(getWithMethodName(element))
                    .addModifiers(Modifier.PUBLIC)
                    .returns(getBuilderName(element.getEnclosingElement()))
                    .addParameter(ParameterSpec.builder(TypeName.get(element.asType()), element.getSimpleName().toString())
                            .build())
                    .addCode(CodeBlock.builder()
                            .addStatement("this.object.$L = $L", element.getSimpleName(), element.getSimpleName())
                            .addStatement("return this")
                            .build())
                    .build();

        }

    }

    private MethodSpec createBuildMethod(ClassName className, TypeElement element) {
        CodeBlock.Builder builder = CodeBlock.builder();

        builder.addStatement("$T result = new $T()", className, className);

        List<Element> fields = getPropertyFields(element)
            .sorted(Comparator.comparing(field -> field.getSimpleName().toString()))
            .collect(Collectors.toList());

        for (Element field : fields) {
            String name = field.getSimpleName().toString();
            TypeKind kind = field.asType().getKind();
            if (TypeKind.BOOLEAN.equals(kind)) {
                builder.addStatement("result.$1L = this.object.$1L == Global.UNDEFINED_BOOLEAN ? false : this.object.$1L", name);
            } else if (kind.isPrimitive()) {
                builder.addStatement("result.$1L = this.object.$1L == Global.UNDEFINED_$2L ? 0 : this.object.$1L", name, kind.name());
            } else {
                builder.addStatement("result.$1L = this.object.$1L  == Global.UNDEFINED_OBJECT ? null : this.object.$1L", name);
            }
        }

        builder.addStatement("return result");

        return MethodSpec.methodBuilder("build")
                .addModifiers(Modifier.PUBLIC)
                .returns(className)
                .addCode(builder.build())
                .build();
    }

    private MethodSpec createToJSONMethod(ClassName className) {
        return MethodSpec.methodBuilder("toJSON")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(className, "object")
                .returns(String.class)
                .addCode(CodeBlock.builder()
                        .beginControlFlow("if ($T.isClient())", ClassNames.GWT_SHARED_HELPER)
                            .addStatement("return JSON.stringify(object)")
                        .nextControlFlow("else")
                            .addStatement("Serializer serializer = new JreSerializer()")
                            .addStatement("return serializer.toJSON(object)")
                        .endControlFlow()
                        .build())
                .build();
    }

    private TypeSpec createJsArrayInterface() {
        TypeVariableName variableName = TypeVariableName.get("T");
        return TypeSpec.classBuilder("JsArray")
                .addModifiers(Modifier.STATIC, Modifier.FINAL)
                .addTypeVariable(variableName)
                .addAnnotation(AnnotationSpec.builder(ClassNames.JSINTEROP_JSTYPE)
                        .addMember("isNative", "true")
                        .addMember("namespace", "$T.GLOBAL", ClassNames.JSINTEROP_JSPACKAGE)
                        .addMember("name", "$S", "Array")
                        .build())
                .addMethod(MethodSpec.methodBuilder("push")
                        .addModifiers(Modifier.PUBLIC, Modifier.NATIVE)
                        .addParameter(variableName, "item")
                        .build())
                .build();
    }

    private TypeSpec createJsonInterface() {
        return TypeSpec.classBuilder("JSON")
                .addModifiers(Modifier.STATIC, Modifier.FINAL)
                .addAnnotation(AnnotationSpec.builder(ClassNames.JSINTEROP_JSTYPE)
                        .addMember("isNative", "true")
                        .addMember("namespace", "$T.GLOBAL", ClassNames.JSINTEROP_JSPACKAGE)
                        .addMember("name", "$S", "JSON")
                        .build())
                .addMethod(MethodSpec.methodBuilder("stringify")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.NATIVE)
                        .addParameter(Object.class, "object")
                        .returns(String.class)
                        .build())
                .build();
    }

    private TypeSpec createGlobalInterface() {
        return TypeSpec.classBuilder("Global")
                .addModifiers(Modifier.STATIC, Modifier.FINAL)
                .addAnnotation(AnnotationSpec.builder(ClassNames.JSINTEROP_JSTYPE)
                        .addMember("isNative", "true")
                        .addMember("namespace", "$T.GLOBAL", ClassNames.JSINTEROP_JSPACKAGE)
                        .build())
                .addFields(Stream.of(Object.class,
                        Boolean.TYPE, Byte.TYPE, Character.TYPE, Double.TYPE,
                        Float.TYPE, Integer.TYPE, Long.TYPE, Short.TYPE)
                        .map(type -> FieldSpec.builder(type, "UNDEFINED_" + type.getSimpleName().toUpperCase())
                                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                .addAnnotation(AnnotationSpec.builder(ClassNames.JSINTEROP_JSPROPERTY)
                                        .addMember("namespace", "$T.GLOBAL", ClassNames.JSINTEROP_JSPACKAGE)
                                        .addMember("name", "$S", "undefined")
                                        .build())
                                .build())
                    .collect(Collectors.toList()))
                .build();
    }

    private TypeSpec createSerializer(ClassName builderName) {
        return TypeSpec.classBuilder(builderName.nestedClass("Serializer"))
                .addModifiers(Modifier.STATIC)
                .addMethod(MethodSpec.methodBuilder("toJSON")
                        .addParameter(Object.class, "object")
                        .returns(String.class)
                        .addStatement("return null")
                        .build())
                .build();
    }

    private ClassName getBuilderName(Element element) {
        ClassName className = ClassName.get(TypeMapper.asType(element));
        return className.peerClass(className.simpleName() + "JSOBuilder");
    }

    private String getWithMethodName(Element element) {
        String name = element.getSimpleName().toString();
        return "with" + Character.toUpperCase(name.charAt(0)) + name.substring(1, name.length());
    }

    private boolean isJsObject(TypeElement element) {
        return element.getAnnotationMirrors().stream()
                .filter(mirror -> ClassNames.JSINTEROP_JSTYPE.equals(AnnotationSpec.get(mirror).type))
                .anyMatch(mirror -> isNative(mirror) && isInGlobalNamespace(mirror) && isJsObject(mirror));
    }

    private boolean isNative(AnnotationMirror mirror) {
        return mirror.getElementValues().entrySet().stream()
                .filter(entry -> "isNative".equals(entry.getKey().getSimpleName().toString()))
                .anyMatch(entry -> Boolean.TRUE.equals(entry.getValue().getValue()));
    }

    private boolean isInGlobalNamespace(AnnotationMirror mirror) {
        return mirror.getElementValues().entrySet().stream()
                .filter(entry -> "namespace".equals(entry.getKey().getSimpleName().toString()))
                .anyMatch(entry -> "<global>".equals(entry.getValue().getValue().toString()));
    }

    private boolean isJsObject(AnnotationMirror mirror) {
        return mirror.getElementValues().entrySet().stream()
                .filter(entry -> "name".equals(entry.getKey().getSimpleName().toString()))
                .anyMatch(entry -> "Object".equals(entry.getValue().getValue().toString()));
    }

}
