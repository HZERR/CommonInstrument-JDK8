package ru.hzerr.bytecode;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import ru.hzerr.collections.list.ArrayHList;
import ru.hzerr.collections.list.HList;

import java.util.function.Predicate;

@SuppressWarnings("CodeBlock2Expr")
public class MethodByteCodeBuilder extends ByteCodeBuilder {

    private MethodByteCodeBuilder() {
    }

    private HList<CtMethod> methods;
    private final StringBuilder ACTION = new StringBuilder();

    public static MethodByteCodeBuilder init(CtMethod... methods) {
        MethodByteCodeBuilder builder = new MethodByteCodeBuilder();
        builder.methods = HList.of(methods);
        return builder;
    }

    public static MethodByteCodeBuilder init(CtClass ctClass) {
        MethodByteCodeBuilder builder = new MethodByteCodeBuilder();
        builder.methods = HList.of(ctClass.getDeclaredMethods());
        builder.reference = ctClass;
        return builder;
    }

    public MethodByteCodeBuilder filter(Predicate<? super CtMethod> predicate) {
        methods.removeIf(predicate);
        return this;
    }

    public MethodByteCodeBuilder filterByNames(String... names) {
        HList<String> namesList = HList.of(names);
        methods.removeIf(method -> namesList.anyMatch(name -> method.getName().equals(name)));
        return this;
    }

    public MethodByteCodeBuilder filterByParameters(String... classes) throws NotFoundException {
        HList<CtClass> parameters = HList.of(classes).map(clazz -> {
            return ByteCodeBuilderFactory.getDefaultClassPoolSettings().getCtClass(clazz);
        }, NotFoundException.class);
        methods.removeIf(method -> {
            return parameters.allMatch(ctClass -> {
                return ArrayHList.create(method.getParameterTypes()).contains(innerParameter -> innerParameter.getName().equals(ctClass.getName()));
            }, NotFoundException.class);
        }, NotFoundException.class);

        return this;
    }

    public MethodByteCodeBuilder setBodyReturnTrue() throws CannotCompileException {
        methods.forEach(method -> method.setBody("return true;"), CannotCompileException.class);
        return this;
    }

    public MethodByteCodeBuilder setBodyReturnFalse() throws CannotCompileException {
        methods.forEach(method -> method.setBody("return false;"), CannotCompileException.class);
        return this;
    }

    public MethodByteCodeBuilder setEmptyBody() throws CannotCompileException {
        methods.forEach(method -> method.setBody("return;"), CannotCompileException.class);
        return this;
    }

    public MethodByteCodeBuilder insertBefore() throws CannotCompileException {
        methods.forEach(method -> method.insertBefore(ACTION.toString()), CannotCompileException.class);
        return this;
    }

    public MethodByteCodeBuilder insertBody() throws CannotCompileException {
        methods.forEach(method -> method.setBody(ACTION.toString()), CannotCompileException.class);
        return this;
    }

    public MethodByteCodeBuilder insertAfter() throws CannotCompileException {
        methods.forEach(method -> method.insertAfter(ACTION.toString()), CannotCompileException.class);
        return this;
    }

    public MethodByteCodeBuilder removeModifiers(Integer... modifiers) {
        HList<Integer> modifiersList = HList.of(modifiers);
        methods.forEach(method ->
                modifiersList.forEach(modifier ->
                        method.setModifiers(method.getModifiers() & ~modifier)));
        return this;
    }

    public MethodByteCodeBuilder addModifiers(Integer... modifiers) {
        HList<Integer> modifiersList = HList.of(modifiers);
        methods.forEach(method -> modifiersList.forEach(modifier -> method.setModifiers(method.getModifiers() | modifier)));
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

    public void delete() throws NotFoundException {
        methods.forEach(method -> reference.removeMethod(method), NotFoundException.class);
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
