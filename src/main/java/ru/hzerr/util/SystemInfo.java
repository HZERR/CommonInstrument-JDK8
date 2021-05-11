package ru.hzerr.util;

import java.util.Locale;

public class SystemInfo {

    private static final String USER_HOME = System.getProperty("user.home");
    private static final String JAVA_VERSION = System.getProperty("java.version");
    private static final String OS_NAME = System.getProperty("os.name");
    private static final String OS_NAME_LCASE = OS_NAME.toLowerCase(Locale.ENGLISH);
    private static final boolean IS_WINDOWS = OS_NAME_LCASE.contains("win");
    private static final boolean IS_LINUX = OS_NAME_LCASE.contains("nux");
    private static final boolean IS_MAC = OS_NAME_LCASE.contains("mac") || OS_NAME_LCASE.contains("darwin");

    private SystemInfo() {
    }

    public static String getBit() {
        boolean bl = IS_WINDOWS ? System.getenv("ProgramFiles(x86)") != null : System.getProperty("os.arch").contains("64");
        return bl ? "64" : "32";
    }

    public static String getUserHome() { return USER_HOME; }
    public static String getJavaVersion() { return JAVA_VERSION; }
    public static boolean isJava8() { return JAVA_VERSION.startsWith("1.8"); }
    public static boolean isJava9() { return JAVA_VERSION.startsWith("9"); }
    public static boolean isJava10() { return JAVA_VERSION.startsWith("10"); }
    public static boolean isJava11() { return JAVA_VERSION.startsWith("11"); }
    public static boolean isJava12() { return JAVA_VERSION.startsWith("12"); }
    public static boolean isJava13() { return JAVA_VERSION.startsWith("13"); }
    public static boolean isJava14() { return JAVA_VERSION.startsWith("14"); }
    public static boolean isJava15() { return JAVA_VERSION.startsWith("15"); }
    public static boolean isJava16() { return JAVA_VERSION.startsWith("16"); }
    public static boolean isJava8OrLower() { return JAVA_VERSION.charAt(0) == '1'; }
    public static boolean isJava9OrHigher() { return Character.getNumericValue(JAVA_VERSION.charAt(0)) >= 9; }
    public static String getOsName() { return OS_NAME; }
    // return os name with lower case
    public static String getOsNameLCase() { return OS_NAME_LCASE; }

    public static boolean isWindows() { return IS_WINDOWS; }
    public static boolean isLinux() { return IS_LINUX; }
    public static boolean isMacOS() { return IS_MAC; }
}
