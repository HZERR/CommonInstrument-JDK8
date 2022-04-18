package ru.hzerr.bytecode;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.NotFoundException;
import ru.hzerr.collections.list.ArrayHList;
import ru.hzerr.collections.list.HList;

import java.util.function.Predicate;

public class ConstructorByteCodeBuilder extends ByteCodeBuilder {

    private ConstructorByteCodeBuilder() {
    }

    private HList<CtConstructor> constructors;
    private final StringBuilder ACTION = new StringBuilder();

    public static ConstructorByteCodeBuilder init(CtConstructor... constructors) {
        ConstructorByteCodeBuilder builder = new ConstructorByteCodeBuilder();
        builder.constructors = HList.of(constructors);
        return builder;
    }

    public static ConstructorByteCodeBuilder init(CtClass ctClass) {
        ConstructorByteCodeBuilder builder = new ConstructorByteCodeBuilder();
        builder.constructors = HList.of(ctClass.getDeclaredConstructors());
        builder.reference = ctClass;
        return builder;
    }

    public ConstructorByteCodeBuilder filter(Predicate<? super CtConstructor> predicate) {
        constructors.removeIf(predicate);
        return this;
    }

    public ConstructorByteCodeBuilder filterByParameters(String... classes) throws NotFoundException {
        HList<CtClass> ctClasses = HList.of(classes).map(clazz -> ByteCodeBuilderFactory.getDefaultClassPoolSettings().getCtClass(clazz), NotFoundException.class);
        constructors.removeIf(constructor ->
                ctClasses.allMatch(ctClass ->
                        ArrayHList.create(constructor.getParameterTypes())
                                .contains(innerParameter -> innerParameter.getName().equals(ctClass.getName())), NotFoundException.class), NotFoundException.class);
        return this;
    }

    public ConstructorByteCodeBuilder setEmptyBody() throws CannotCompileException {
        constructors.forEach(constructor -> constructor.setBody("return;"), CannotCompileException.class);
        return this;
    }

    public ConstructorByteCodeBuilder insertBefore() throws CannotCompileException {
        constructors.forEach(constructor -> constructor.insertBefore(ACTION.toString()), CannotCompileException.class);
        return this;
    }

    public ConstructorByteCodeBuilder insertBody() throws CannotCompileException {
        constructors.forEach(constructor -> constructor.setBody(ACTION.toString()), CannotCompileException.class);
        return this;
    }

    public ConstructorByteCodeBuilder insertAfter() throws CannotCompileException {
        constructors.forEach(constructor -> constructor.insertAfter(ACTION.toString()), CannotCompileException.class);
        return this;
    }

    public ConstructorByteCodeBuilder removeModifiers(Integer... modifiers) {
        HList<Integer> modifiersList = HList.of(modifiers);
        constructors.forEach(constructor -> modifiersList.forEach(modifier -> constructor.setModifiers(constructor.getModifiers() & ~modifier)));
        return this;
    }

    public ConstructorByteCodeBuilder addModifiers(Integer... modifiers) {
        HList<Integer> modifiersList = HList.of(modifiers);
        constructors.forEach(constructor -> modifiersList.forEach(modifier -> constructor.setModifiers(constructor.getModifiers() | modifier)));
        return this;
    }

    public ConstructorByteCodeBuilder addBlockStartPoint() {
        ACTION.append("{");
        return this;
    }

    public ConstructorByteCodeBuilder addBlockEndPoint() {
        ACTION.append("}");
        return this;
    }

    public ConstructorByteCodeBuilder addBlockPoints() {
        this.ACTION.insert(0, '{').append('}');
        return this;
    }

    /*
     * Methods that change the body
     */
    public ConstructorByteCodeBuilder addCode(String code) {
        this.ACTION.append(code);
        return this;
    }

    /*
     * ATTENTION!
     * Example of an argument:
     * addPrintln("\"Hello, World!\"");
     */
    public ConstructorByteCodeBuilder addPrintln(String message) {
        this.ACTION.append("System.out.println(").append(message).append(");");
        return this;
    }

    /*
     * ATTENTION!
     * All content is framed in quotes!
     * Example of an argument:
     * addSafePrintln("Hello, World!");
     */
    public ConstructorByteCodeBuilder addSafePrintln(String message) {
        this.ACTION.append("System.out.println(\"").append(message).append("\");");
        return this;
    }

    public ConstructorByteCodeBuilder addPrintlnStackTraceWithTab() {
        this.ACTION
                .append("StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();")
                .append("for (int i = 0; i < stackTraceElements.length; i++) {")
                .append("System.out.println(\"\\t\" + stackTraceElements[i]);")
                .append("}");
        return this;
    }
}
