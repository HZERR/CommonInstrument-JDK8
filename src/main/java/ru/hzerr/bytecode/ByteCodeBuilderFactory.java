package ru.hzerr.bytecode;

import javassist.CtClass;

public class ByteCodeBuilderFactory {

    private static final CPSettings SETTINGS = CPSettings.create();

    public static CPSettings getDefaultClassPoolSettings() { return SETTINGS; }

    public static MethodByteCodeBuilder createMethodByteCodeBuilder(String className) {
        CtClass clazz = SETTINGS.getCtClass(className);
        return MethodByteCodeBuilder.init(clazz);
    }

    public static MethodByteCodeBuilder createMethodByteCodeBuilder(byte[] classFileBuffer) {
        CtClass clazz = SETTINGS.getCtClass(classFileBuffer);
        return MethodByteCodeBuilder.init(clazz);
    }

    public static ConstructorByteCodeBuilder createConstructorByteCodeBuilder(String className) {
        CtClass clazz = SETTINGS.getCtClass(className);
        return ConstructorByteCodeBuilder.init(clazz);
    }

    public static ConstructorByteCodeBuilder createConstructorByteCodeBuilder(byte[] classFileBuffer) {
        CtClass clazz = SETTINGS.getCtClass(classFileBuffer);
        return ConstructorByteCodeBuilder.init(clazz);
    }

    public static FieldByteCodeBuilder createFieldByteCodeBuilder(String className) {
        CtClass clazz = SETTINGS.getCtClass(className);
        return FieldByteCodeBuilder.init(clazz);
    }

    public static FieldByteCodeBuilder createFieldByteCodeBuilder(byte[] classFileBuffer) {
        CtClass clazz = SETTINGS.getCtClass(classFileBuffer);
        return FieldByteCodeBuilder.init(clazz);
    }

    public static ClassByteCodeBuilder createClassByteCodeBuilder(byte[]... classFileBuffers) {
        CtClass[] classes = new CtClass[classFileBuffers.length];
        for (int i = 0; i < classes.length; i++) {
            classes[i] = SETTINGS.getCtClass(classFileBuffers[i]);
        }

        return ClassByteCodeBuilder.init(classes);
    }

    public static ClassByteCodeBuilder createClassByteCodeBuilder(String... classNames) {
        CtClass[] classes = new CtClass[classNames.length];
        for (int i = 0; i < classes.length; i++) {
            classes[i] = SETTINGS.getCtClass(classNames[i]);
        }

        return ClassByteCodeBuilder.init(classes);
    }
}
