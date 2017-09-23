package test;

import java.util.function.Supplier;

public abstract class AbstractJSOJSOBuilder {

    private final AbstractJSO object;

    protected AbstractJSOJSOBuilder(Supplier<? extends AbstractJSO> supplier) {
        object = supplier.get();
    }

    public AbstractJSOJSOBuilder withStringProperty(String stringProperty) {
        object.stringProperty = stringProperty;
        return this;
    }

    public AbstractJSOJSOBuilder withIntProperty(int intProperty) {
        object.intProperty = intProperty;
        return this;
    }

    public AbstractJSOJSOBuilder withPropertyWithoutAnnotation(String propertyWithoutAnnotation) {
        object.propertyWithoutAnnotation = propertyWithoutAnnotation;
        return this;
    }

    public AbstractJSO build() {
        return object;
    }

}
