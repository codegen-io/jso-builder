package test;

import com.google.gwt.core.shared.GWT;
import java.util.Arrays;
import java.util.stream.Stream;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

public abstract class BasicJSOJSOBuilder {
    private final BasicJSO object = new BasicJSO();

    public BasicJSOJSOBuilder withStringProperty(String stringProperty) {
        this.object.stringProperty = stringProperty;
        return this;
    }

    public BasicJSOJSOBuilder withIntProperty(int intProperty) {
        this.object.intProperty = intProperty;
        return this;
    }

    public BasicJSOJSOBuilder withPropertyWithoutAnnotation(String propertyWithoutAnnotation) {
        this.object.propertyWithoutAnnotation = propertyWithoutAnnotation;
        return this;
    }

    @SuppressWarnings("unchecked")
    public BasicJSOJSOBuilder withStringArrayProperty(String... stringArrayProperty) {
        if (GWT.isClient()) {
            JsArray<String> array;
            if (this.object.stringArrayProperty != null) {
                Object value = this.object.stringArrayProperty;
                array = (JsArray<String>) value;
            } else {
                array = new JsArray<>();
                Object value = array;
                this.object.stringArrayProperty = (String[]) value;
            }
            for (int i = 0; i < stringArrayProperty.length; i++) {
                array.push(stringArrayProperty[i]);
            }
        } else {
            if (this.object.stringArrayProperty == null) {
                this.object.stringArrayProperty = new String[0];
            }
            this.object.stringArrayProperty = Stream.concat(
                    Arrays.stream(this.object.stringArrayProperty), Arrays.stream(stringArrayProperty))
                    .toArray(size -> new String[size]);
        }
        return this;
    }

    public BasicJSO build() {
        BasicJSO result = new BasicJSO();
        result.intProperty = this.object.intProperty == Global.UNDEFINED_INT ? 0 : this.object.intProperty;
        result.propertyWithoutAnnotation = this.object.propertyWithoutAnnotation == Global.UNDEFINED_OBJECT ? null : this.object.propertyWithoutAnnotation;
        result.stringArrayProperty = this.object.stringArrayProperty == Global.UNDEFINED_OBJECT ? null : this.object.stringArrayProperty;
        result.stringProperty = this.object.stringProperty == Global.UNDEFINED_OBJECT ? null : this.object.stringProperty;
        return result;
    }

    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Array")
    static final class JsArray<T> {
        public native void push(T item);
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

}
