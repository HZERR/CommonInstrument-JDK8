package ru.hzerr.bytecode;

import javassist.CtClass;

// TODO: 06.11.2021 REWRITE ALL 
public abstract class ByteCodeBuilder {

    CtClass reference;

    public ClassByteCodeBuilder concatClassByteCodeBuilder() { return ClassByteCodeBuilder.init(reference); }
    public FieldByteCodeBuilder concatFieldByteCodeBuilder() { return FieldByteCodeBuilder.init(reference); }
    public MethodByteCodeBuilder concatMethodByteCodeBuilder() {
        return MethodByteCodeBuilder.init(reference);
    }
    public ConstructorByteCodeBuilder concatConstructorByteCodeBuilder() { return ConstructorByteCodeBuilder.init(reference); }

    public byte[] toBytecode() {
        return Runtime.callIsNotNull(reference, () -> {
            byte[] transformed = reference.toBytecode();
            reference.detach();
            return transformed;
        });
    }

    public void writeFile() {
        Runtime.runIsNotNull(reference, () -> reference.writeFile());
    }

    public void writeFile(String directory) {
        Runtime.runIsNotNull(reference, () -> reference.writeFile(directory));
    }
}
