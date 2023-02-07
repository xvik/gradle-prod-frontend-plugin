package ru.vyarus.gradle.frontend.util;

import com.helger.css.ECSSVersion;
import com.helger.css.decl.CascadingStyleSheet;
import com.helger.css.decl.visit.AbstractModifyingCSSUrlVisitor;
import com.helger.css.decl.visit.CSSVisitor;
import com.helger.css.reader.CSSReader;
import com.helger.css.utils.CSSDataURLHelper;

import javax.annotation.Nonnull;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Vyacheslav Rusakov
 * @since 07.02.2023
 */
public final class CssUtils {

    private CssUtils() {
    }

    public static List<String> findLinks(final File file) {
        final CascadingStyleSheet css = CSSReader.readFromFile(file, StandardCharsets.UTF_8, ECSSVersion.CSS30);
        if (css == null) {
            throw new IllegalStateException("Failed to read css file: " + file.getAbsolutePath());
        }
        final List<String> urls = new ArrayList<>();
        CSSVisitor.visitCSSUrl(css, new AbstractModifyingCSSUrlVisitor() {
            @Nonnull
            @Override
            protected String getModifiedURI(@Nonnull String url) {
                // ignore data urls and svg references
                if (!CSSDataURLHelper.isDataURL(url) && !url.startsWith("#")) {
                    urls.add(url);
                }
                return url;
            }
        });
        return urls;
    }
}
