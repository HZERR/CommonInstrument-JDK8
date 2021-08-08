package ru.hzerr.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public final class Instruments {

    private Instruments() {
    }

    public static Process runAsync(String... cmd) {
        try {
            return new ProcessBuilder(cmd).start();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public static Collection<String> run(String... cmd) {
        return run(Arrays.asList(cmd));
    }

    public static Collection<String> run(List<String> cmd) {
        Collection<String> messages = new ArrayList<>();
        try {
            Process p = new ProcessBuilder(cmd).start();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            // drain streams, else we might lock up
            InputStreamDrainer errDrainer = new InputStreamDrainer(p.getErrorStream(), baos);
            InputStreamDrainer outDrainer = new InputStreamDrainer(p.getInputStream(), baos);

            errDrainer.start();
            outDrainer.start();

            int err = p.waitFor();

            errDrainer.join();
            outDrainer.join();

            messages.add(baos.toString());
        } catch (IOException ex) {
            return Collections.singleton(ex.getMessage());
        } catch (InterruptedException ex) {
            throw new IllegalStateException(ex);
        }
        return messages;
    }

    public static long getPid(Process process) {
        // Step 1. Try Process.pid, available since Java 9.
        try {
            Method m = Process.class.getMethod("pid");
            Object pid = m.invoke(process);
            if (pid instanceof Long) {
                return (long) pid;
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            // Fallthrough
        }

        // Step 2. Try to hack into the JDK 8- UNIXProcess.
        try {
            Class<?> c = Class.forName("java.lang.UNIXProcess");
            Field f = c.getDeclaredField("pid");
            setAccessible(f);
            Object o = f.get(process);
            if (o instanceof Integer) {
                return (int) o;
            }
        } catch (NoSuchFieldException | ClassNotFoundException | IllegalAccessException e) {
            // Fallthrough
        }

        // Step 3. Try to hack into JDK 9+ ProcessImpl.
        // Renamed from UNIXProcess with JDK-8071481.
        try {
            Class<?> c = Class.forName("java.lang.ProcessImpl");
            Field f = c.getDeclaredField("pid");
            setAccessible(f);
            Object o = f.get(process);
            if (o instanceof Integer) {
                return (int) o;
            }
        } catch (NoSuchFieldException | ClassNotFoundException | IllegalAccessException e) {
            // Fallthrough
        }

        // No dice, return zero
        return 0;
    }

    public static long getPid() {
        final String DELIM = "@";

        String name = ManagementFactory.getRuntimeMXBean().getName();

        if (name != null) {
            int idx = name.indexOf(DELIM);

            if (idx != -1) {
                String str = name.substring(0, name.indexOf(DELIM));
                try {
                    return Long.parseLong(str);
                } catch (NumberFormatException nfe) {
                    throw new IllegalStateException("Process PID is not a number: " + str);
                }
            }
        }
        throw new IllegalStateException("Unsupported PID format: " + name);
    }

    public static double[] HSBtoRGB(double hue, double saturation, double brightness) {
        // normalize the hue
        double normalizedHue = ((hue % 360) + 360) % 360;
        hue = normalizedHue / 360;

        double r = 0, g = 0, b = 0;
        if (saturation == 0) {
            r = g = b = brightness;
        } else {
            double h = (hue - Math.floor(hue)) * 6.0;
            double f = h - java.lang.Math.floor(h);
            double p = brightness * (1.0 - saturation);
            double q = brightness * (1.0 - saturation * f);
            double t = brightness * (1.0 - (saturation * (1.0 - f)));
            switch ((int) h) {
                case 0:
                    r = brightness;
                    g = t;
                    b = p;
                    break;
                case 1:
                    r = q;
                    g = brightness;
                    b = p;
                    break;
                case 2:
                    r = p;
                    g = brightness;
                    b = t;
                    break;
                case 3:
                    r = p;
                    g = q;
                    b = brightness;
                    break;
                case 4:
                    r = t;
                    g = p;
                    b = brightness;
                    break;
                case 5:
                    r = brightness;
                    g = p;
                    b = q;
                    break;
            }
        }
        double[] f = new double[3];
        f[0] = r;
        f[1] = g;
        f[2] = b;
        return f;
    }

    public static double[] RGBtoHSB(double r, double g, double b) {
        double hue, saturation, brightness;
        double[] hsbvals = new double[3];
        double cmax = Math.max(r, g);
        if (b > cmax) cmax = b;
        double cmin = Math.min(r, g);
        if (b < cmin) cmin = b;

        brightness = cmax;
        if (cmax != 0)
            saturation = (double) (cmax - cmin) / cmax;
        else
            saturation = 0;

        if (saturation == 0) {
            hue = 0;
        } else {
            double redc = (cmax - r) / (cmax - cmin);
            double greenc = (cmax - g) / (cmax - cmin);
            double bluec = (cmax - b) / (cmax - cmin);
            if (r == cmax)
                hue = bluec - greenc;
            else if (g == cmax)
                hue = 2.0 + redc - bluec;
            else
                hue = 4.0 + greenc - redc;
            hue = hue / 6.0;
            if (hue < 0)
                hue = hue + 1.0;
        }
        hsbvals[0] = hue * 360;
        hsbvals[1] = saturation;
        hsbvals[2] = brightness;
        return hsbvals;
    }

    public static String convertUnicode(String src) {
        char[] buf;
        int bp;
        int buflen;

        char ch;

        int unicodeConversionBp = -1;

        buf = src.toCharArray();
        buflen = buf.length;
        bp = -1;

        char[] dst = new char[buflen];
        int dstIndex = 0;

        while (bp < buflen - 1) {
            ch = buf[++bp];
            if (ch == '\\') {
                if (unicodeConversionBp != bp) {
                    bp++;
                    ch = buf[bp];
                    if (ch == 'u') {
                        do {
                            bp++;
                            ch = buf[bp];
                        } while (ch == 'u');
                        int limit = bp + 3;
                        if (limit < buflen) {
                            char c = ch;
                            int result = Character.digit(c, 16);
                            if (result >= 0 && c > 0x7f) {
                                ch = "0123456789abcdef".charAt(result);
                            }
                            int d = result;
                            int code = d;
                            while (bp < limit && d >= 0) {
                                bp++;
                                ch = buf[bp];
                                char c1 = ch;
                                int result1 = Character.digit(c1, 16);
                                if (result1 >= 0 && c1 > 0x7f) {
                                    ch = "0123456789abcdef".charAt(result1);
                                }
                                d = result1;
                                code = (code << 4) + d;
                            }
                            if (d >= 0) {
                                ch = (char) code;
                                unicodeConversionBp = bp;
                            }
                        }
                    } else {
                        bp--;
                        ch = '\\';
                    }
                }
            }
            dst[dstIndex++] = ch;
        }

        return new String(dst, 0, dstIndex);
    }

    private static void setAccessible(AccessibleObject o) throws IllegalAccessException {
        try {
            o.setAccessible(true);
        } catch (SecurityException se) { throw new IllegalAccessException(o + " is not accessible"); }
    }
}
