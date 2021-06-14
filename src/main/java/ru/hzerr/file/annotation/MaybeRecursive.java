package ru.hzerr.file.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
// Означает, что метод может включать и не включать в себя рекурсивное решение
public @interface MaybeRecursive {
}
