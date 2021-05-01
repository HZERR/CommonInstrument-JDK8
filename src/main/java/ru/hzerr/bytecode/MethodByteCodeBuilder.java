package ru.hzerr.bytecode;

import javassist.CtClass;
import javassist.CtMethod;
import ru.hzerr.stream.HStream;
import ru.hzerr.stream.function.Predicate;

import java.util.Arrays;

public class MethodByteCodeBuilder extends ByteCodeBuilder {

    private MethodByteCodeBuilder() {
    }

    private HStream<CtMethod> methods;
    private final StringBuilder ACTION = new StringBuilder();

    public static MethodByteCodeBuilder init(CtMethod... methods) {
        MethodByteCodeBuilder builder = new MethodByteCodeBuilder();
        builder.methods = HStream.of(methods);
        return builder;
    }

    public static MethodByteCodeBuilder init(CtClass ctClass) {
        MethodByteCodeBuilder builder = new MethodByteCodeBuilder();
        builder.methods = HStream.of(ctClass.getDeclaredMethods());
        builder.reference = ctClass;
        return builder;
    }

    public MethodByteCodeBuilder filter(Predicate<? super CtMethod> predicate) {
        methods.filter(predicate);
        return this;
    }

    public MethodByteCodeBuilder filterByNames(String... names) {
        HStream<String> namesHStream = HStream.of(names);
        methods.filter(method -> namesHStream.anyMatch(name -> method.getName().equals(name)));
        return this;
    }

    public MethodByteCodeBuilder filterByParameters(String... classes) {
        methods.filter(method -> HStream.of(classes).allMatch(clazz -> method.getLongName().contains(clazz)));
//        HStream<CtClass> ctClassHStream = HStream.of(classes).map(
//                clazz -> Runtime.call(() ->
//                        ByteCodeBuilderFactory.getDefaultClassPoolSettings().getCtClass(clazz)));
//        methods.filter(method ->
//                ctClassHStream.allMatch(ctClass ->
//                        Arrays.asList(Runtime.call(method::getParameterTypes)).contains(ctClass)));
        return this;
    }

    public MethodByteCodeBuilder setBodyReturnTrue() {
        methods.forEach(method -> Runtime.run(() -> method.setBody("return true;")));
        return this;
    }

    public MethodByteCodeBuilder setBodyReturnFalse() {
        methods.forEach(method -> Runtime.run(() -> method.setBody("return false;")));
        return this;
    }

    public MethodByteCodeBuilder setEmptyBody() {
        methods.forEach(method -> Runtime.run(() -> method.setBody("return;")));
        return this;
    }

    public MethodByteCodeBuilder insertBefore() {
        methods.forEach(method -> Runtime.run(() -> method.insertBefore(ACTION.toString())));
        return this;
    }

    public MethodByteCodeBuilder insertBody() {
        methods.forEach(method -> Runtime.run(() -> method.setBody(ACTION.toString())));
        return this;
    }

    public MethodByteCodeBuilder insertAfter() {
        methods.forEach(method -> Runtime.run(() -> method.insertAfter(ACTION.toString())));
        return this;
    }

    public MethodByteCodeBuilder removeModifiers(Integer... modifiers) {
        HStream<Integer> modifiersStream = HStream.of(modifiers);
        methods.forEach(method ->
                modifiersStream.forEach(modifier ->
                        method.setModifiers(method.getModifiers() & ~modifier)));
        return this;
    }

    public MethodByteCodeBuilder addModifiers(Integer... modifiers) {
        HStream<Integer> modifiersStream = HStream.of(modifiers);
        methods.forEach(method -> modifiersStream.forEach(modifier -> method.setModifiers(method.getModifiers() | modifier)));
        return this;
    }

    public MethodByteCodeBuilder addBlockStartPoint() {
        this.ACTION.append("{");
        return this;
    }

    public MethodByteCodeBuilder addBlockEndPoint() {
        this.ACTION.append("}");
        return this;
    }

    public MethodByteCodeBuilder addBlockPoints() {
        this.ACTION.insert(0, '{').append('}');
        return this;
    }

    public MethodByteCodeBuilder addCode(String code) {
        this.ACTION.append(code);
        return this;
    }

    public MethodByteCodeBuilder addParameter(String parameterClassName) {
        CtClass param = ByteCodeBuilderFactory.getDefaultClassPoolSettings().getCtClass(parameterClassName);
        methods.forEach(method -> Runtime.run(() -> method.addParameter(param)));
        return this;
    }

    public MethodByteCodeBuilder addLocalVariable(String name, String typeClassName) {
        CtClass type = ByteCodeBuilderFactory.getDefaultClassPoolSettings().getCtClass(typeClassName);
        methods.forEach(method -> Runtime.run(() -> method.addLocalVariable(name, type)));
        return this;
    }

    public void delete() {
        methods.forEach(method -> reference.removeMethod(method));
    }

    /*
     * ATTENTION!
     * Example of an argument:
     * addPrintln("\"Hello, World!\"");
     */
    public MethodByteCodeBuilder addPrintln(String message) {
        this.ACTION.append("System.out.println(").append(message).append(");");
        return this;
    }

    public MethodByteCodeBuilder addCatchBlock(String catchCode, String exceptionClassName) {
        CtClass e = ByteCodeBuilderFactory.getDefaultClassPoolSettings().getCtClass(exceptionClassName);
        methods.forEach(method -> Runtime.run(() -> method.addCatch(catchCode, e)));
        return this;
    }

    /*
     * ATTENTION!
     * All content is framed in quotes!
     * Example of an argument:
     * addSafePrintln("Hello, World!");
     */
    public MethodByteCodeBuilder addSafePrintln(String message) {
        this.ACTION.append("System.out.println(\"").append(message).append("\");");
        return this;
    }

    public MethodByteCodeBuilder addPrintlnStackTraceWithTab() {
        this.ACTION
                .append("StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();")
                .append("for (int i = 0; i < stackTraceElements.length; i++) {")
                .append("System.out.println(\"\\t\" + stackTraceElements[i]);")
                .append("}");
        return this;
    }
}
