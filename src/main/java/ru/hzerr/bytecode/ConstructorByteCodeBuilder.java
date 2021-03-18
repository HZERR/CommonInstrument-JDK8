package ru.hzerr.bytecode;

import javassist.CtClass;
import javassist.CtConstructor;
import ru.hzerr.stream.HStream;
import ru.hzerr.stream.function.Predicate;

import java.util.Arrays;

public class ConstructorByteCodeBuilder extends ByteCodeBuilder {

    private ConstructorByteCodeBuilder() {
    }

    private HStream<CtConstructor> constructors;
    private final StringBuilder ACTION = new StringBuilder();

    public static ConstructorByteCodeBuilder init(CtConstructor... constructors) {
        ConstructorByteCodeBuilder builder = new ConstructorByteCodeBuilder();
        builder.constructors = HStream.of(constructors);
        return builder;
    }

    public static ConstructorByteCodeBuilder init(CtClass ctClass) {
        ConstructorByteCodeBuilder builder = new ConstructorByteCodeBuilder();
        builder.constructors = HStream.of(ctClass.getDeclaredConstructors());
        builder.reference = ctClass;
        return builder;
    }

    public ConstructorByteCodeBuilder filter(Predicate<? super CtConstructor> predicate) {
        constructors.filter(predicate);
        return this;
    }

    public ConstructorByteCodeBuilder filterByParameters(String... classes) {
        HStream<CtClass> ctClassHStream = HStream.of(classes).map(
                clazz -> Runtime.call(() ->
                        ByteCodeBuilderFactory.getDefaultClassPoolSettings().getCtClass(clazz)));
        constructors.filter(constructor ->
                ctClassHStream.allMatch(ctClass ->
                        Arrays.asList(Runtime.call(constructor::getParameterTypes)).contains(ctClass)));
        return this;
    }

    public ConstructorByteCodeBuilder setEmptyBody() {
        constructors.forEach(constructor -> Runtime.run(() -> constructor.setBody("return;")));
        return this;
    }

    public ConstructorByteCodeBuilder insertBefore() {
        constructors.forEach(constructor -> Runtime.run(() -> constructor.insertBefore(ACTION.toString())));
        return this;
    }

    public ConstructorByteCodeBuilder insertBody() {
        constructors.forEach(constructor -> Runtime.run(() -> constructor.setBody(ACTION.toString())));
        return this;
    }

    public ConstructorByteCodeBuilder insertAfter() {
        constructors.forEach(constructor -> Runtime.run(() -> constructor.insertAfter(ACTION.toString())));
        return this;
    }

    public ConstructorByteCodeBuilder removeModifiers(Integer... modifiers) {
        HStream<Integer> modifiersStream = HStream.of(modifiers);
        constructors.forEach(constructor -> modifiersStream.forEach(modifier -> constructor.setModifiers(constructor.getModifiers() & ~modifier)));
        return this;
    }

    public ConstructorByteCodeBuilder addModifiers(Integer... modifiers) {
        HStream<Integer> modifiersStream = HStream.of(modifiers);
        constructors.forEach(constructor -> modifiersStream.forEach(modifier -> constructor.setModifiers(constructor.getModifiers() | modifier)));
        return this;
    }

    public ConstructorByteCodeBuilder addLocalVariable(String name, String typeClassName) {
        CtClass type = ByteCodeBuilderFactory.getDefaultClassPoolSettings().getCtClass(typeClassName);
        constructors.forEach(constructor -> Runtime.run(() -> constructor.addLocalVariable(name, type)));
        return this;
    }

    public ConstructorByteCodeBuilder addCatchBlock(String catchCode, String exceptionClassName) {
        CtClass e = ByteCodeBuilderFactory.getDefaultClassPoolSettings().getCtClass(exceptionClassName);
        constructors.forEach(constructor -> Runtime.run(() -> constructor.addCatch(catchCode, e)));
        return this;
    }

    public ConstructorByteCodeBuilder addParameter(String parameterClassName) {
        CtClass param = ByteCodeBuilderFactory.getDefaultClassPoolSettings().getCtClass(parameterClassName);
        constructors.forEach(constructor -> Runtime.run(() -> constructor.addParameter(param)));
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
