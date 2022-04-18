package ru.hzerr.bytecode;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtNewMethod;
import ru.hzerr.collections.list.HList;

@SuppressWarnings("CodeBlock2Expr")
public class ClassByteCodeBuilder extends ByteCodeBuilder {

    private ClassByteCodeBuilder() {
    }

    private HList<CtClass> classes;

    public static ClassByteCodeBuilder init(CtClass... classes) {
        ClassByteCodeBuilder builder = new ClassByteCodeBuilder();
        builder.classes = HList.of(classes);
        return builder;
    }

    public ClassByteCodeBuilder
    addMethod(int modifiers, CtClass returnType, String mname, CtClass[] parameters, CtClass[] exceptions, String body) throws CannotCompileException {
        classes.forEach(clazz -> {
            CtNewMethod.make(modifiers, returnType, mname, parameters, exceptions, body, clazz);
        }, CannotCompileException.class);

        return this;
    }
}
