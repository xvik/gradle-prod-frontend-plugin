package ru.vyarus.gradle.frontend.core.util;

/**
 * @author Vyacheslav Rusakov
 * @since 02.05.2023
 */
public final class SizeFormatter {

    private SizeFormatter() {
    }

    public static String formatChangePercent(final long originalSize, final long size) {
        long percent = (originalSize - size) * 100 / originalSize;
        if (percent == 0) {
            return "not changed";
        } else {
            boolean decrease = percent > 0;
            return Math.abs(percent) + "% size " + (decrease ? "decrease" : "increase(!)");
        }
    }
}
