package ru.vyarus.gradle.frontend.core.util;

/**
 * Size format utils.
 *
 * @author Vyacheslav Rusakov
 * @since 02.05.2023
 */
public final class SizeFormatter {

    private SizeFormatter() {
    }

    /**
     * @param originalSize original size
     * @param size         current size
     * @return percent of size change (increased, decreased, not changed)
     */
    public static String formatChangePercent(final long originalSize, final long size) {
        final long percent = (originalSize - size) * 100 / originalSize;
        if (percent == 0) {
            return "not changed";
        } else {
            final boolean decrease = percent > 0;
            return Math.abs(percent) + "% size " + (decrease ? "decrease" : "increase(!)");
        }
    }
}
