module common.instrument {
    requires javassist;
    requires org.jetbrains.annotations;
    requires org.apache.commons.lang3;
    requires org.apache.commons.io;
    requires jdk.unsupported;
    requires java.logging;
    requires java.management;
    exports ru.hzerr.bytecode;
    exports ru.hzerr.file;
    exports ru.hzerr.collections;
    exports ru.hzerr.util;
}