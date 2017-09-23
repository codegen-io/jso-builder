package io.codegen.jsobuilder.processor;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic.Kind;

import org.immutables.metainf.Metainf;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

@Metainf.Service(Processor.class)
public class JSOBuilderProcessor extends AbstractProcessor {

    private final Set<String> processedClasses = new HashSet<>();

    private Elements elementUtils = null;
    // private Types typeUtils = null;
    private Messager messager = null;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        this.elementUtils = processingEnv.getElementUtils();
        // this.typeUtils = processingEnv.getTypeUtils();
        this.messager = processingEnv.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Arrays.asList(
                ClassNames.JSINTEROP_JSTYPE).stream()
                .map(ClassName::toString)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        Set<String> classesToProcess = collectClassNames(annotations, roundEnv);

        classesToProcess.stream()
            .filter(name -> !processedClasses.contains(name))
            .map(name -> elementUtils.getTypeElement(name))
            .filter(this::isJsObject)
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
                .collect(Collectors.toSet());
    }

    private String getClassName(Element element) {
        switch (element.getKind()) {
            case CLASS:
                return element.asType().toString();
            default:
                emitError("Unkown element kind " + element.getKind(), element);
                return null;
        }
    }

    private void emitError(String message, Element element) {
        messager.printMessage(Kind.ERROR, message, element);
    }

    private void generateBuilder(TypeElement element) {
        ClassName className = ClassName.get(element);
        ClassName builderName = className.peerClass(className.simpleName() + "JSOBuilder");

        TypeSpec.Builder typeSpec = TypeSpec.classBuilder(builderName)
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

        // Create object field
        typeSpec.addField(createObjectField(className));

        // Create builder constructor
        typeSpec.addMethod(createBuilderConstructor(className));

        typeSpec.addMethods(element.getEnclosedElements().stream()
                .filter(type -> ElementKind.FIELD.equals(type.getKind()))
                .filter(type -> type.getAnnotationMirrors().stream()
                        .map(mirror -> AnnotationSpec.get(mirror).type)
                        .noneMatch(annotation -> annotation.equals(ClassNames.JSINTEROP_JSIGNORE)
                                || annotation.equals(ClassNames.JSINTEROP_JSOVERLAY)))
                .map(this::createPropertyMethod)
                .collect(Collectors.toList()));

        // Create build method
        typeSpec.addMethod(createBuildMethod(className));

        JavaFile javaFile = JavaFile.builder(builderName.packageName(), typeSpec.build())
                .skipJavaLangImports(true)
                .build();

        try {
            javaFile.writeTo(this.processingEnv.getFiler());
        } catch (IOException e) {
            emitError("Unable to write file: " + e.getMessage(), element);
        }
    }

    private FieldSpec createObjectField(ClassName className) {
        return FieldSpec.builder(className, "object", Modifier.PRIVATE, Modifier.FINAL)
                .build();
    }

    private MethodSpec createBuilderConstructor(ClassName className) {
        ParameterizedTypeName supplier =
                ParameterizedTypeName.get(ClassName.get(Supplier.class), WildcardTypeName.subtypeOf(className));

        return MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PROTECTED)
                .addParameter(supplier, "supplier")
                .addCode("object = supplier.get();\n", className)
                .build();
    }

    private MethodSpec createPropertyMethod(Element element) {
        return MethodSpec.methodBuilder(getWithMethodName(element))
                .addModifiers(Modifier.PUBLIC)
                .returns(getBuilderName(element.getEnclosingElement()))
                .addParameter(ParameterSpec.builder(TypeName.get(element.asType()), element.getSimpleName().toString())
                        .build())
                .addCode(CodeBlock.builder()
                        .addStatement("object.$L = $L", element.getSimpleName(), element.getSimpleName())
                        .addStatement("return this")
                        .build())
                .build();
    }

    private MethodSpec createBuildMethod(ClassName className) {
        return MethodSpec.methodBuilder("build")
                .addModifiers(Modifier.PUBLIC)
                .returns(className)
                .addCode(CodeBlock.builder()
                        .addStatement("return object")
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
