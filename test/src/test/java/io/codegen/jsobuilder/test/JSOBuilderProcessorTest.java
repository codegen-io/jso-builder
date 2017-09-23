package io.codegen.jsobuilder.test;

import static com.google.testing.compile.CompilationSubject.*;
import static com.google.testing.compile.Compiler.*;

import java.io.IOException;

import org.junit.Test;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;

import io.codegen.jsobuilder.processor.JSOBuilderProcessor;

public class JSOBuilderProcessorTest {

    @Test
    public void testProcessor() throws IOException {
        Compilation compilation = javac()
                .withProcessors(new JSOBuilderProcessor())
                .compile(JavaFileObjects.forResource("test/AbstractJSO.java"));

        assertThat(compilation).succeeded();
        assertThat(compilation).generatedSourceFile("test/AbstractJSOJSOBuilder")
            .hasSourceEquivalentTo(JavaFileObjects.forResource("test/AbstractJSOJSOBuilder.java"));
    }

}
