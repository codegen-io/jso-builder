package io.codegen.jsobuilder.integration;

import static org.junit.Assert.*;

import org.junit.Test;

public class BasicJSOTest {

    @Test
    public void testStringProperty() {
        BasicJSO jso = BasicJSO.builder()
                .withStringProperty("value")
                .build();

        assertEquals("value", jso.stringProperty);
    }

    @Test
    public void testIntProperty() {
        BasicJSO jso = BasicJSO.builder()
                .withIntProperty(1)
                .build();

        assertEquals(1, jso.intProperty);
    }

    @Test
    public void testBooleanProperty() {
        BasicJSO jso = BasicJSO.builder()
                .withBooleanProperty(true)
                .build();

        assertEquals(true, jso.booleanProperty);
    }

    @Test
    public void testOverlayedProperty() {
        BasicJSO jso = BasicJSO.builder()
                .build();

        assertEquals("overlayed", jso.overlayedProperty);
    }

    @Test
    public void testPropertyWithoutAnnotation() {
        BasicJSO jso = BasicJSO.builder()
                .withPropertyWithoutAnnotation("value")
                .build();

        assertEquals("value", jso.propertyWithoutAnnotation);
    }

    @Test
    public void testArrayProperty() {
        BasicJSO jso = BasicJSO.builder()
                .withStringArrayProperty("value")
                .withStringArrayProperty("and", "another")
                .build();

        assertEquals("value", jso.stringArrayProperty[0]);
        assertEquals("another", jso.stringArrayProperty[2]);
    }

    @Test
    public void testSerializeJSO() {
        BasicJSO jso = BasicJSO.builder()
                .withBooleanProperty(true)
                .withIntProperty(2)
                .withStringProperty("value")
                .withStringArrayProperty("one", "two", "three")
                .build();

        assertEquals("{"
                + "\"booleanProperty\":true,"
                + "\"intProperty\":2,"
                + "\"propertyWithoutAnnotation\":null,"
                + "\"stringArrayProperty\":[\"one\",\"two\",\"three\"],"
                + "\"stringProperty\":\"value\""
                + "}", jso.toJSON());
    }

}
