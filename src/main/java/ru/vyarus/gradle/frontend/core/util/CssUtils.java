package ru.vyarus.gradle.frontend.core.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Vyacheslav Rusakov
 * @since 07.02.2023
 */
public final class CssUtils {

    public static final Pattern URL_PATTERN = Pattern.compile(
            "url\\s*\\(\\s*['\"]?(?<url>[^'\")]*)\\s*['\"]?", Pattern.DOTALL);

    private CssUtils() {
    }

    public static List<String> findLinks(final File file) {
        String content;
        try {
            content = Files.readString(file.toPath());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read file: " + file.getAbsolutePath(), e);
        }
        return findLinks(content);
    }

    public static List<String> findLinks(final String content) {
        final List<String> res = new ArrayList<>();
        final Matcher matcher = URL_PATTERN.matcher(content);
        while (matcher.find()) {
            String url = matcher.group("url");
            if (!url.startsWith("data:")) {
                res.add(url.trim());
            }
        }

        return res;
    }
}
