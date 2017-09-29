package test;

import com.google.gwt.core.shared.GWT;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Stream;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

public abstract class WithConflictingNamesJSOBuilder {
    private final WithConflictingNames object;

    protected WithConflictingNamesJSOBuilder(Supplier<? extends WithConflictingNames> supplier) {
        object = supplier.get();
    }

    @SuppressWarnings("unchecked")
    public WithConflictingNamesJSOBuilder withValue(String[] value) {
        if (GWT.isClient()) {
            JsArray<String> array;
            if (this.object.value != null) {
                Object value_ = this.object.value;
                array = (JsArray<String>) value_;
            } else {
                array = new JsArray<>();
                Object value_ = array;
                this.object.value = (String[]) value_;
            }
            for (int i = 0; i < value.length; i++) {
                array.push(value[i]);
            }
        } else {
            if (this.object.value == null) {
                this.object.value = new String[0];
            }
            this.object.value = Stream.concat(
                    Arrays.stream(this.object.value), Arrays.stream(value))
                    .toArray(size -> new String[size]);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public WithConflictingNamesJSOBuilder withArray(String[] array) {
        if (GWT.isClient()) {
            JsArray<String> array_;
            if (this.object.array != null) {
                Object value = this.object.array;
                array_ = (JsArray<String>) value;
            } else {
                array_ = new JsArray<>();
                Object value = array_;
                this.object.array = (String[]) value;
            }
            for (int i = 0; i < array.length; i++) {
                array_.push(array[i]);
            }
        } else {
            if (this.object.array == null) {
                this.object.array = new String[0];
            }
            this.object.array = Stream.concat(
                    Arrays.stream(this.object.array), Arrays.stream(array))
                    .toArray(size -> new String[size]);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public WithConflictingNamesJSOBuilder withObject(String[] object) {
        if (GWT.isClient()) {
            JsArray<String> array;
            if (this.object.object != null) {
                Object value = this.object.object;
                array = (JsArray<String>) value;
            } else {
                array = new JsArray<>();
                Object value = array;
                this.object.object = (String[]) value;
            }
            for (int i = 0; i < object.length; i++) {
                array.push(object[i]);
            }
        } else {
            if (this.object.object == null) {
                this.object.object = new String[0];
            }
            this.object.object = Stream.concat(
                    Arrays.stream(this.object.object), Arrays.stream(object))
                    .toArray(size -> new String[size]);
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    public WithConflictingNamesJSOBuilder withSize(String[] size) {
        if (GWT.isClient()) {
            JsArray<String> array;
            if (this.object.size != null) {
                Object value = this.object.size;
                array = (JsArray<String>) value;
            } else {
                array = new JsArray<>();
                Object value = array;
                this.object.size = (String[]) value;
            }
            for (int i = 0; i < size.length; i++) {
                array.push(size[i]);
            }
        } else {
            if (this.object.size == null) {
                this.object.size = new String[0];
            }
            this.object.size = Stream.concat(
                    Arrays.stream(this.object.size), Arrays.stream(size))
                    .toArray(size_ -> new String[size_]);
        }
        return this;
    }

    public WithConflictingNames build() {
        return object;
    }

    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Array")
    static final class JsArray<T> {
        public native void push(T item);
    }

}
