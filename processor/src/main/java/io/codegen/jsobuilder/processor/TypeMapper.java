package io.codegen.jsobuilder.processor;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.util.SimpleElementVisitor8;
import javax.lang.model.util.SimpleTypeVisitor8;

public class TypeMapper {

    private static final ElementVisitor<ExecutableElement, Void> EXECUTABLE_VISITOR =
            new SimpleElementVisitor8<ExecutableElement, Void>() {
                @Override
                public ExecutableElement visitExecutable(ExecutableElement e, Void label) {
                    return e;
                }
            };

    private static final ElementVisitor<TypeElement, Void> TYPE_VISITOR =
            new SimpleElementVisitor8<TypeElement, Void>() {
                @Override
                public TypeElement visitType(TypeElement e, Void label) {
                    return e;
                }
            };

    private static final TypeVisitor<DeclaredType, Void> DECLARED_TYPE_VISITOR =
            new SimpleTypeVisitor8<DeclaredType, Void>() {
                @Override
                public DeclaredType visitDeclared(DeclaredType e, Void label) {
                    return e;
                }
            };

    /**
     * Convert the given {@link Element} to an {@link ExecutableElement}.
     *
     * @param element
     * @return
     */
    public static ExecutableElement asExecutable(Element element) {
        return element.accept(EXECUTABLE_VISITOR, null);
    }

    /**
     * Convert the given {@link Element} to a {@link TypeElement}.
     *
     * @param element
     * @return
     */
    public static TypeElement asType(Element element) {
        return element.accept(TYPE_VISITOR, null);
    }

    /**
     * Convert the given {@link TypeMirror} to a {@link DeclaredType}.
     *
     * @param element
     * @return
     */
    public static DeclaredType asDeclaredType(TypeMirror element) {
        return element.accept(DECLARED_TYPE_VISITOR, null);
    }

}
