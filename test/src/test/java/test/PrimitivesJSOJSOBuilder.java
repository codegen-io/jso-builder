package test;

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
