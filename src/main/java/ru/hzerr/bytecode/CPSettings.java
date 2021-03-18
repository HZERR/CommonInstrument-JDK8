package ru.hzerr.bytecode;

import javassist.ClassPool;
import javassist.CtClass;

import java.io.ByteArrayInputStream;
import java.net.URL;

public class CPSettings {

    private CPSettings() {}

    private static final ClassPool CLASS_POOL = ClassPool.getDefault();

    public ClassPool getClassPool() { return CLASS_POOL; }

    public CtClass getCtClass(String className) { return Runtime.call(() -> CLASS_POOL.get(className)); }
    public CtClass getCtClass(byte[] classFileBuffer) {
        return Runtime.call(() -> CLASS_POOL.makeClass(new ByteArrayInputStream(classFileBuffer)));
    }

    public void appendClassPath(String path) { Runtime.run(() -> CLASS_POOL.appendPathList(path)); }
    public void insertClassPath(String path) { Runtime.run(() -> CLASS_POOL.insertClassPath(path)); }
    public URL getResource(String path) { return Runtime.call(() -> CLASS_POOL.find(path)); }

    public static CPSettings create() { return new CPSettings(); }
}
