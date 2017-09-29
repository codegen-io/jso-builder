package test;

import com.google.gwt.core.shared.GWT;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Stream;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

public abstract class AbstractJSOJSOBuilder {
    private final AbstractJSO object;

    protected AbstractJSOJSOBuilder(Supplier<? extends AbstractJSO> supplier) {
        object = supplier.get();
    }

    public AbstractJSOJSOBuilder withStringProperty(String stringProperty) {
        this.object.stringProperty = stringProperty;
        return this;
    }

    public AbstractJSOJSOBuilder withIntProperty(int intProperty) {
        this.object.intProperty = intProperty;
        return this;
    }

    public AbstractJSOJSOBuilder withPropertyWithoutAnnotation(String propertyWithoutAnnotation) {
        this.object.propertyWithoutAnnotation = propertyWithoutAnnotation;
        return this;
    }

    @SuppressWarnings("unchecked")
    public AbstractJSOJSOBuilder withStringArrayProperty(String[] stringArrayProperty) {
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

    public AbstractJSO build() {
        return object;
    }

    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Array")
    static final class JsArray<T> {
        public native void push(T item);
    }

}
