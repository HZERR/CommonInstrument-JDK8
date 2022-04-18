package ru.hzerr.bytecode;

import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;
import ru.hzerr.collections.list.HList;

import java.util.function.Predicate;

@SuppressWarnings("CodeBlock2Expr")
public class FieldByteCodeBuilder extends ByteCodeBuilder {

    private FieldByteCodeBuilder() {
    }

    private HList<CtField> fields;
    private final StringBuilder ACTION = new StringBuilder();

    public static FieldByteCodeBuilder init(CtField... fields) {
        FieldByteCodeBuilder builder = new FieldByteCodeBuilder();
        builder.fields = HList.of(fields);
        return builder;
    }

    public static FieldByteCodeBuilder init(CtClass ctClass) {
        FieldByteCodeBuilder builder = new FieldByteCodeBuilder();
        builder.fields = HList.of(ctClass.getDeclaredFields());
        builder.reference = ctClass;
        return builder;
    }

    public FieldByteCodeBuilder filter(Predicate<? super CtField> predicate) {
        fields.removeIf(predicate);
        return this;
    }

    public FieldByteCodeBuilder filterByNames(String... names) {
        HList<String> namesList = HList.of(names);
        fields.removeIf(field -> namesList.anyMatch(name -> field.getName().equals(name)));
        return this;
    }

    public FieldByteCodeBuilder setType(String className) throws NotFoundException {
        fields.forEach(field -> {
            field.setType(ByteCodeBuilderFactory.getDefaultClassPoolSettings().getCtClass(className));
        }, NotFoundException.class);

        return this;
    }

    public FieldByteCodeBuilder removeModifiers(Integer... modifiers) {
        HList<Integer> modifiersList = HList.of(modifiers);
        fields.forEach(field ->
                modifiersList.forEach(modifier ->
                        field.setModifiers(field.getModifiers() & ~modifier)));
        return this;
    }

    public FieldByteCodeBuilder addModifiers(Integer... modifiers) {
        HList<Integer> modifiersList = HList.of(modifiers);
        fields.forEach(field -> modifiersList.forEach(modifier -> field.setModifiers(field.getModifiers() | modifier)));
        return this;
    }
}
