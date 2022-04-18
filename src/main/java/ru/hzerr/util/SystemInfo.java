package ru.hzerr.util;

import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Locale;

public class SystemInfo {

    private static final String USER_HOME = System.getProperty("user.home");
    private static final String JAVA_VERSION = System.getProperty("java.version");
    private static final String OS_NAME = System.getProperty("os.name");
    private static final String OS_NAME_LCASE = OS_NAME.toLowerCase(Locale.ENGLISH);
    private static final String VERSION = System.getProperty("os.version");
    private static final String JAVAFX_PLATFORM = AccessController.doPrivileged((PrivilegedAction<String>) () -> System.getProperty("javafx.platform"));
    private static final boolean ANDROID = "android".equals(JAVAFX_PLATFORM) || "Dalvik".equals(System.getProperty("java.vm.name"));
    private static final boolean WINDOWS = OS_NAME.startsWith("Windows");
    private static final boolean WINDOWS_VISTA_OR_LATER = WINDOWS && versionNumberGreaterThanOrEqualTo(6.0f);
    private static final boolean WINDOWS_7_OR_LATER = WINDOWS && versionNumberGreaterThanOrEqualTo(6.1f);
    private static final boolean MAC = OS_NAME.startsWith("Mac");
    private static final boolean LINUX = OS_NAME.startsWith("Linux") && !ANDROID;
    private static final boolean SOLARIS = OS_NAME.startsWith("SunOS");
    private static final boolean IOS = OS_NAME.startsWith("iOS");

    private SystemInfo() {
    }

    public static String getBit() {
        boolean bl = WINDOWS ? System.getenv("ProgramFiles(x86)") != null : System.getProperty("os.arch").contains("64");
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
    public static boolean isJava17() { return JAVA_VERSION.startsWith("17"); }
    public static boolean isJava8OrLower() { return JAVA_VERSION.charAt(0) == '1'; }
    public static boolean isJava9OrHigher() { return Character.getNumericValue(JAVA_VERSION.charAt(0)) >= 9; }
    public static String getOsName() { return OS_NAME; }
    // return os name with lower case
    public static String getOsNameLCase() { return OS_NAME_LCASE; }
    public static boolean isWindows() { return WINDOWS; }
    public static boolean isWinVistaOrLater(){
        return WINDOWS_VISTA_OR_LATER;
    }
    public static boolean isWin7OrLater(){
        return WINDOWS_7_OR_LATER;
    }
    public static boolean isLinux() { return LINUX; }
    public static boolean isMacOS() { return MAC; }
    public static boolean isSolaris(){
        return SOLARIS;
    }
    public static boolean isUnix(){
        return LINUX || SOLARIS;
    }
    public static boolean isIOS(){
        return IOS;
    }
    public static boolean isAndroid() {
        return ANDROID;
    }

    public static String getCurrentJvm() {
        return System.getProperty("java.home") +
                File.separator +
                "bin" +
                File.separator +
                "java" +
                (isWindows() ? ".exe" : "");
    }

    public static String getCurrentJvmVersion() {
        return "JDK "
                + System.getProperty("java.version")
                + ", VM "
                + System.getProperty("java.vm.version");
    }

    public static String getCurrentOSVersion() {
        return System.getProperty("os.name")
                + ", "
                + System.getProperty("os.arch")
                + ", "
                + System.getProperty("os.version");
    }

    private static boolean versionNumberGreaterThanOrEqualTo(float value) {
        try {
            return Float.parseFloat(VERSION) >= value;
        } catch (Exception e) {
            return false;
        }
    }
}
