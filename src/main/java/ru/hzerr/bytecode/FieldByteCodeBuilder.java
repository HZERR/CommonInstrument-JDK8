package ru.hzerr.bytecode;

import javassist.CtClass;
import javassist.CtField;
import ru.hzerr.stream.HStream;
import ru.hzerr.stream.function.Predicate;

public class FieldByteCodeBuilder extends ByteCodeBuilder {

    private FieldByteCodeBuilder() {
    }

    private HStream<CtField> fields;
    private final StringBuilder ACTION = new StringBuilder();

    public static FieldByteCodeBuilder init(CtField... fields) {
        FieldByteCodeBuilder builder = new FieldByteCodeBuilder();
        builder.fields = HStream.of(fields);
        return builder;
    }

    public static FieldByteCodeBuilder init(CtClass ctClass) {
        FieldByteCodeBuilder builder = new FieldByteCodeBuilder();
        builder.fields = HStream.of(ctClass.getDeclaredFields());
        builder.reference = ctClass;
        return builder;
    }

    public FieldByteCodeBuilder filter(Predicate<? super CtField> predicate) {
        fields.filter(predicate);
        return this;
    }

    public FieldByteCodeBuilder filterByNames(String... names) {
        HStream<String> namesHStream = HStream.of(names);
        fields.filter(field -> namesHStream.anyMatch(name -> field.getName().equals(name)));
        return this;
    }

    public FieldByteCodeBuilder setType(String className) {
        fields.forEach(field ->
                field.setType(ByteCodeBuilderFactory.getDefaultClassPoolSettings().getCtClass(className)));
        return this;
    }

    public FieldByteCodeBuilder removeModifiers(Integer... modifiers) {
        HStream<Integer> modifiersStream = HStream.of(modifiers);
        fields.forEach(field ->
                modifiersStream.forEach(modifier ->
                        field.setModifiers(field.getModifiers() & ~modifier)));
        return this;
    }

    public FieldByteCodeBuilder addModifiers(Integer... modifiers) {
        HStream<Integer> modifiersStream = HStream.of(modifiers);
        fields.forEach(field -> modifiersStream.forEach(modifier -> field.setModifiers(field.getModifiers() | modifier)));
        return this;
    }
}
