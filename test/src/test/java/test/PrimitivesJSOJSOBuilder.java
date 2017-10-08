package test;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.core.shared.GwtIncompatible;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

public abstract class PrimitivesJSOJSOBuilder {
    private final PrimitivesJSO object = new PrimitivesJSO();

    public PrimitivesJSOJSOBuilder withBooleanProperty(boolean booleanProperty) {
        this.object.booleanProperty = booleanProperty;
        return this;
    }

    public PrimitivesJSOJSOBuilder withByteProperty(byte byteProperty) {
        this.object.byteProperty = byteProperty;
        return this;
    }

    public PrimitivesJSOJSOBuilder withDoubleProperty(double doubleProperty) {
        this.object.doubleProperty = doubleProperty;
        return this;
    }

    public PrimitivesJSOJSOBuilder withFloatProperty(float floatProperty) {
        this.object.floatProperty = floatProperty;
        return this;
    }

    public PrimitivesJSOJSOBuilder withIntProperty(int intProperty) {
        this.object.intProperty = intProperty;
        return this;
    }

    public PrimitivesJSOJSOBuilder withLongProperty(long longProperty) {
        this.object.longProperty = longProperty;
        return this;
    }

    public PrimitivesJSOJSOBuilder withShortProperty(short shortProperty) {
        this.object.shortProperty = shortProperty;
        return this;
    }

    public PrimitivesJSO build() {
        PrimitivesJSO result = new PrimitivesJSO();
        result.booleanProperty = this.object.booleanProperty == Global.UNDEFINED_BOOLEAN ? false : this.object.booleanProperty;
        result.byteProperty = this.object.byteProperty == Global.UNDEFINED_BYTE ? 0 : this.object.byteProperty;
        result.doubleProperty = this.object.doubleProperty == Global.UNDEFINED_DOUBLE ? 0 : this.object.doubleProperty;
        result.floatProperty = this.object.floatProperty == Global.UNDEFINED_FLOAT ? 0 : this.object.floatProperty;
        result.intProperty = this.object.intProperty == Global.UNDEFINED_INT ? 0 : this.object.intProperty;
        result.longProperty = this.object.longProperty == Global.UNDEFINED_LONG ? 0 : this.object.longProperty;
        result.shortProperty = this.object.shortProperty == Global.UNDEFINED_SHORT ? 0 : this.object.shortProperty;
        return result;
    }

    public static String toJSON(PrimitivesJSO object) {
        if (GWT.isClient()) {
            return JSON.stringify(object);
        } else {
            Serializer serializer = new JreSerializer();
            return serializer.toJSON(object);
        }
    }

    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Array")
    static final class JsArray<T> {
        public native void push(T item);
    }

    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "JSON")
    static final class JSON {
        public static native String stringify(Object object);
    }

    @JsType(isNative = true, namespace = JsPackage.GLOBAL)
    static final class Global {
        @JsProperty(namespace = JsPackage.GLOBAL, name = "undefined")
        public static Object UNDEFINED_OBJECT;

        @JsProperty(namespace = JsPackage.GLOBAL, name = "undefined")
        public static boolean UNDEFINED_BOOLEAN;

        @JsProperty(namespace = JsPackage.GLOBAL, name = "undefined")
        public static byte UNDEFINED_BYTE;

        @JsProperty(namespace = JsPackage.GLOBAL, name = "undefined")
        public static char UNDEFINED_CHAR;

        @JsProperty(namespace = JsPackage.GLOBAL, name = "undefined")
        public static double UNDEFINED_DOUBLE;

        @JsProperty(namespace = JsPackage.GLOBAL, name = "undefined")
        public static float UNDEFINED_FLOAT;

        @JsProperty(namespace = JsPackage.GLOBAL, name = "undefined")
        public static int UNDEFINED_INT;

        @JsProperty(namespace = JsPackage.GLOBAL, name = "undefined")
        public static long UNDEFINED_LONG;

        @JsProperty(namespace = JsPackage.GLOBAL, name = "undefined")
        public static short UNDEFINED_SHORT;
    }

    static class Serializer {
        String toJSON(Object object) { return null; }
    }

    static class JreSerializer extends Serializer {
        @GwtIncompatible
        final String toJSON(Object object) {
            if (!isJsObject(object.getClass())) {
                throw new IllegalStateException("Class " + object.getClass() + " isn't a JavaScript object");
            }
            StringWriter writer = new StringWriter();
            try {
                writeJSON(writer, object);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return writer.toString();
        }

        @GwtIncompatible
        private static final boolean isJsObject(Class<?> type) {
            JsType jsType = type.getAnnotation(JsType.class);
            if (jsType == null) {
                return false;
            }
            if (!jsType.isNative()) {
                return false;
            }
            if (!JsPackage.GLOBAL.equals(jsType.namespace())) {
                return false;
            }
            if (!"Object".equals(jsType.name())) {
                return false;
            }
            return true;
        }

        @GwtIncompatible
        private static final void writeJSON(Writer writer, Object value) throws IOException {
            if (value == null) {
                writer.append("null");
                return;
            }

            Class<?> type = value.getClass();
            if (type.isPrimitive()) {
                writer.append(String.valueOf(value));
            } else if (type.isArray()) {
                writer.append('[');
                int arraySize = Array.getLength(value);
                for (int arrayIndex = 0; arrayIndex < arraySize; arrayIndex++) {
                    writeJSON(writer, Array.get(value, arrayIndex));

                    if (arrayIndex < (arraySize - 1)) {
                        writer.append(',');
                    }
                }
                writer.append(']');
            } else if (Number.class.isAssignableFrom(type)) {
                writer.append(String.valueOf(value));
            } else if (Boolean.class.isAssignableFrom(type)) {
                writer.append(String.valueOf(value));
            } else if (CharSequence.class.isAssignableFrom(type)) {
                writer.append('"');
                writeValue(writer, String.valueOf(value));
                writer.append('"');
            } else if (isJsObject(type)) {
                writer.append('{');

                Field[] fields = type.getDeclaredFields();

                SortedMap<String, Field> fieldByName = new TreeMap<>();

                for (int fieldIndex = 0; fieldIndex < fields.length; fieldIndex++) {
                    Field field = fields[fieldIndex];

                    if (field.getAnnotation(JsIgnore.class) != null) {
                        continue;
                    }
                    if (field.getAnnotation(JsOverlay.class) != null) {
                        continue;
                    }

                    String name;
                    JsProperty property = field.getAnnotation(JsProperty.class);
                    if (property != null && !"<auto>".equals(property.name())) {
                        name = property.name();
                    } else {
                        name = field.getName();
                    }

                    fieldByName.put(name, field);
                }

                boolean firstProperty = true;
                for (Map.Entry<String, Field> entry : fieldByName.entrySet()) {
                    if (!firstProperty) {
                        writer.append(',');
                    }

                    writer.append('"');
                    writeValue(writer, entry.getKey());
                    writer.append('"');
                    writer.append(':');

                    try {
                        writeJSON(writer, entry.getValue().get(value));
                    } catch (IllegalArgumentException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }

                    firstProperty = false;
                }

                writer.append('}');
            } else {
                throw new IllegalArgumentException("Unknown type " + type);
            }
        }

        @GwtIncompatible
        private static final void writeValue(Writer writer, CharSequence value) throws IOException {
            for (int index = 0; index < value.length(); index++) {
                char currentCharacter = value.charAt(index);
                switch (currentCharacter) {
                    case '\\':
                        writer.write("\\\\");
                        break;
                    case '\"':
                        writer.write("\\\"");
                        break;
                    case '\b':
                        writer.write("\\b");
                        break;
                    case '\t':
                        writer.write("\\t");
                        break;
                    case '\n':
                        writer.write("\\n");
                        break;
                    case '\f':
                        writer.write("\\f");
                        break;
                    case '\r':
                        writer.write("\\r");
                        break;
                    default:
                        if ((currentCharacter >= '\u0000' && currentCharacter <= '\u001f')
                                || (currentCharacter >= '\u007f' && currentCharacter <= '\u009f')
                                || (currentCharacter >= '\u2028' && currentCharacter <= '\u2029')) {
                            writer.write("\\u");
                            String hexValue = Integer.toHexString(currentCharacter);
                            writer.write("0000", 0, 4 - hexValue.length());
                            writer.write(hexValue);
                        } else {
                            writer.write(currentCharacter);
                        }
                }
            }
        }
    }

}
