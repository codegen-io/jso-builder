package io.codegen.jsobuilder.test;

import static com.google.testing.compile.CompilationSubject.*;
import static com.google.testing.compile.Compiler.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;

import io.codegen.jsobuilder.processor.JSOBuilderProcessor;

public class JSOBuilderProcessorTest {

    Compiler compiler;

    @Before
    public void setUp() {
        compiler = javac().withProcessors(new JSOBuilderProcessor());
    }

    @Test
    public void testProcessor() throws IOException {
        Compilation compilation = compiler.compile(JavaFileObjects.forResource("test/BasicJSO.java"));

        assertThat(compilation).succeeded();
        assertThat(compilation).generatedSourceFile("test/BasicJSOJSOBuilder")
            .hasSourceEquivalentTo(JavaFileObjects.forResource("test/BasicJSOJSOBuilder.java"));
    }

    @Test
    public void conflictingNamesAreRenamed() throws IOException {
        Compilation compilation = compiler.compile(JavaFileObjects.forResource("test/WithConflictingNames.java"));

        assertThat(compilation).succeeded();
        assertThat(compilation).generatedSourceFile("test/WithConflictingNamesJSOBuilder")
            .hasSourceEquivalentTo(JavaFileObjects.forResource("test/WithConflictingNamesJSOBuilder.java"));
    }

    @Test
    public void nonJsTypeThrowsException() throws IOException {
        Compilation compilation = compiler.compile(JavaFileObjects.forResource("test/NonJsType.java"));

        assertThat(compilation).hadErrorContaining("Type isn't a pure JsType JavaScript object");
    }

    @Test
    public void nonGlobalJsTypeThrowsException() throws IOException {
        Compilation compilation = compiler.compile(JavaFileObjects.forResource("test/NonGlobalJsType.java"));

        assertThat(compilation).hadErrorContaining("Type isn't a pure JsType JavaScript object");
    }

    @Test
    public void nonNativeJsTypeThrowsException() throws IOException {
        Compilation compilation = compiler.compile(JavaFileObjects.forResource("test/NonNativeJsType.java"));

        assertThat(compilation).hadErrorContaining("Type isn't a pure JsType JavaScript object");
    }

    @Test
    public void nonObjectJsTypeThrowsException() throws IOException {
        Compilation compilation = compiler.compile(JavaFileObjects.forResource("test/NonObjectJsType.java"));

        assertThat(compilation).hadErrorContaining("Type isn't a pure JsType JavaScript object");
    }

    @Test
    public void nonConcreteJsTypeThrowsException() throws IOException {
        Compilation compilation = compiler.compile(JavaFileObjects.forResource("test/NonConcreteJsType.java"));

        assertThat(compilation).hadErrorContaining("Type isn't a concrete JsType JavaScript object");
    }

    @Test
    public void testPrimitives() throws IOException {
        Compilation compilation = compiler.compile(JavaFileObjects.forResource("test/PrimitivesJSO.java"));

        assertThat(compilation).succeeded();
        assertThat(compilation).generatedSourceFile("test/PrimitivesJSOJSOBuilder")
            .hasSourceEquivalentTo(JavaFileObjects.forResource("test/PrimitivesJSOJSOBuilder.java"));
    }

}
