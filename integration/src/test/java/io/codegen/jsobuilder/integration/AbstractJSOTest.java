package io.codegen.jsobuilder.integration;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class AbstractJSOTest {

    AbstractJSO.Builder builder;

    @Before
    public void setUp() {
        builder = new AbstractJSO.Builder(() -> new AbstractJSO() {});
    }

    @Test
    public void testStringProperty() {
        AbstractJSO jso = builder
                .withStringProperty("value")
                .build();

        assertEquals("value", jso.stringProperty);
    }

    @Test
    public void testIntProperty() {
        AbstractJSO jso = builder
                .withIntProperty(1)
                .build();

        assertEquals(1, jso.intProperty);
    }

    @Test
    public void testBooleanProperty() {
        AbstractJSO jso = builder
                .withBooleanProperty(true)
                .build();

        assertEquals(true, jso.booleanProperty);
    }

    @Test
    public void testOverlayedProperty() {
        AbstractJSO jso = builder
                .build();

        assertEquals("overlayed", jso.overlayedProperty);
    }

    @Test
    public void testPropertyWithoutAnnotation() {
        AbstractJSO jso = builder
                .withPropertyWithoutAnnotation("value")
                .build();

        assertEquals("value", jso.propertyWithoutAnnotation);
    }

    @Test
    public void testArrayProperty() {
        AbstractJSO jso = builder
                .withStringArrayProperty(new String[]{"value"})
                .withStringArrayProperty(new String[]{"another"})
                .build();

        assertEquals("value", jso.stringArrayProperty[0]);
        assertEquals("another", jso.stringArrayProperty[1]);
    }

}
