package ru.hzerr.bytecode;

import javassist.CtClass;
import javassist.NotFoundException;

import java.io.IOException;

public class ByteCodeBuilderFactory {

    private static final CPSettings SETTINGS = CPSettings.create();

    public static CPSettings getDefaultClassPoolSettings() { return SETTINGS; }

    public static MethodByteCodeBuilder createMethodByteCodeBuilder(String className) throws NotFoundException {
        CtClass clazz = SETTINGS.getCtClass(className);
        return MethodByteCodeBuilder.init(clazz);
    }

    public static MethodByteCodeBuilder createMethodByteCodeBuilder(byte[] classFileBuffer) throws IOException {
        CtClass clazz = SETTINGS.getCtClass(classFileBuffer);
        return MethodByteCodeBuilder.init(clazz);
    }

    public static ConstructorByteCodeBuilder createConstructorByteCodeBuilder(String className) throws NotFoundException {
        CtClass clazz = SETTINGS.getCtClass(className);
        return ConstructorByteCodeBuilder.init(clazz);
    }

    public static ConstructorByteCodeBuilder createConstructorByteCodeBuilder(byte[] classFileBuffer) throws IOException {
        CtClass clazz = SETTINGS.getCtClass(classFileBuffer);
        return ConstructorByteCodeBuilder.init(clazz);
    }

    public static FieldByteCodeBuilder createFieldByteCodeBuilder(String className) throws NotFoundException {
        CtClass clazz = SETTINGS.getCtClass(className);
        return FieldByteCodeBuilder.init(clazz);
    }

    public static FieldByteCodeBuilder createFieldByteCodeBuilder(byte[] classFileBuffer) throws IOException {
        CtClass clazz = SETTINGS.getCtClass(classFileBuffer);
        return FieldByteCodeBuilder.init(clazz);
    }

    public static ClassByteCodeBuilder createClassByteCodeBuilder(byte[]... classFileBuffers) throws IOException {
        CtClass[] classes = new CtClass[classFileBuffers.length];
        for (int i = 0; i < classes.length; i++) {
            classes[i] = SETTINGS.getCtClass(classFileBuffers[i]);
        }

        return ClassByteCodeBuilder.init(classes);
    }

    public static ClassByteCodeBuilder createClassByteCodeBuilder(String... classNames) throws NotFoundException {
        CtClass[] classes = new CtClass[classNames.length];
        for (int i = 0; i < classes.length; i++) {
            classes[i] = SETTINGS.getCtClass(classNames[i]);
        }

        return ClassByteCodeBuilder.init(classes);
    }
}
