package ru.vyarus.gradle.frontend.util;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Vyacheslav Rusakov
 * @since 06.02.2023
 */
public class WebUtils {
    private static final Pattern SOURCE_URL = Pattern.compile("sourceMappingURL=([^ *]+)");

    public static String getSourceMapReference(final File file) {
        final String line = FileUtils.readLastLine(file);
        return line == null ? null : getSourceMapReference(line);
    }

    public static String getSourceMapReference(String line) {
        final Matcher matcher = SOURCE_URL.matcher(line);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
