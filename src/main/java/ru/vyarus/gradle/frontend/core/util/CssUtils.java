package ru.vyarus.gradle.frontend.core.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CSS utilities.
 *
 * @author Vyacheslav Rusakov
 * @since 07.02.2023
 */
public final class CssUtils {

    /**
     * Url extractor pattern (inside CSS)ю
     */
    public static final Pattern URL_PATTERN = Pattern.compile(
            "url\\s*\\(\\s*['\"]?(?<url>[^'\")]*)\\s*['\"]?", Pattern.DOTALL);

    private CssUtils() {
    }

    /**
     * Searches for remote urls in css file. Data urls ignored.
     *
     * @param file css file
     * @return found urls or empty list
     */
    public static List<String> findLinks(final File file) {
        String content;
        try {
            content = Files.readString(file.toPath());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read file: " + file.getAbsolutePath(), e);
        }
        return findLinks(content);
    }

    /**
     * Searches for remote urls in css file. Data urls ignored.
     *
     * @param content css file content
     * @return found urls or empty list
     */
    public static List<String> findLinks(final String content) {
        final List<String> res = new ArrayList<>();
        final Matcher matcher = URL_PATTERN.matcher(content);
        while (matcher.find()) {
            final String url = matcher.group("url");
            if (!url.startsWith("data:")) {
                res.add(url.trim());
            }
        }

        return res;
    }
}
