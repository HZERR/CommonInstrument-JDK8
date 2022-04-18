package ru.hzerr.bytecode;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;

public class CPSettings {

    private CPSettings() {}

    private static final ClassPool CLASS_POOL = ClassPool.getDefault();

    public ClassPool getClassPool() { return CLASS_POOL; }

    public CtClass getCtClass(String className) throws NotFoundException { return CLASS_POOL.get(className); }
    public CtClass getCtClass(byte[] classFileBuffer) throws IOException {
        return CLASS_POOL.makeClass(new ByteArrayInputStream(classFileBuffer));
    }

    public void appendClassPath(String path) throws NotFoundException { CLASS_POOL.appendPathList(path); }
    public void insertClassPath(String path) throws NotFoundException { CLASS_POOL.insertClassPath(path); }
    public URL getResource(String path) { return CLASS_POOL.find(path); }

    public static CPSettings create() { return new CPSettings(); }
}
