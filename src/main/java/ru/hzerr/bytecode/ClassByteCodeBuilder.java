package ru.hzerr.bytecode;

import javassist.CtClass;
import javassist.CtNewMethod;
import ru.hzerr.stream.HStream;

public class ClassByteCodeBuilder extends ByteCodeBuilder {

    private ClassByteCodeBuilder() {
    }

    private HStream<CtClass> classes;
    private final StringBuilder ACTION = new StringBuilder();

    public static ClassByteCodeBuilder init(CtClass... classes) {
        ClassByteCodeBuilder builder = new ClassByteCodeBuilder();
        builder.classes = HStream.of(classes);
        return builder;
    }

    public ClassByteCodeBuilder
    addMethod(int modifiers, CtClass returnType, String mname, CtClass[] parameters, CtClass[] exceptions, String body) {
        classes.forEach(clazz -> CtNewMethod.make(modifiers, returnType, mname, parameters, exceptions, body, clazz));
        return this;
    }
}
