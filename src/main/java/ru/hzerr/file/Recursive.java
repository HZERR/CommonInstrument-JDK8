package ru.hzerr.file;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
// Означает, что метод включает в себя рекурсивное решение
public @interface Recursive {

    boolean value() default true;
}
