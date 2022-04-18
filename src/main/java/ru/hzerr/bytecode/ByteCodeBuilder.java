package ru.hzerr.bytecode;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;

import java.io.IOException;

// TODO: 06.11.2021 REWRITE ALL 
public abstract class ByteCodeBuilder {

    protected CtClass reference;

    public ClassByteCodeBuilder concatClassByteCodeBuilder() { return ClassByteCodeBuilder.init(reference); }
    public FieldByteCodeBuilder concatFieldByteCodeBuilder() { return FieldByteCodeBuilder.init(reference); }
    public MethodByteCodeBuilder concatMethodByteCodeBuilder() {
        return MethodByteCodeBuilder.init(reference);
    }
    public ConstructorByteCodeBuilder concatConstructorByteCodeBuilder() { return ConstructorByteCodeBuilder.init(reference); }

    public byte[] toBytecode() throws IOException, CannotCompileException {
        if (reference != null) {
            byte[] transformed = reference.toBytecode();
            reference.detach();
            return transformed;
        } else
            throw new NullPointerException("There is no reference to the class");
    }

    public void writeFile() throws NotFoundException, IOException, CannotCompileException {
        if (reference != null) {
            reference.writeFile();
        } else
            throw new NullPointerException("There is no reference to the class");
    }

    public void writeFile(String directory) throws CannotCompileException, IOException {
        if (reference != null) {
            reference.writeFile(directory);
        } else
            throw new NullPointerException("There is no reference to the class");
    }
}
