package ru.vyarus.gradle.frontend.util.minify;

import in.wilsonl.minifyhtml.Configuration;
import in.wilsonl.minifyhtml.MinifyHtml;

/**
 * https://github.com/wilsonzlin/minify-html
 *
 * @author Vyacheslav Rusakov
 * @since 01.02.2023
 */
public final class HtmlMinifier {

    private HtmlMinifier() {
    }

    public static String minify(final String html) {
        final Configuration cfg = new Configuration.Builder()
                .setKeepHtmlAndHeadOpeningTags(true)
                .setDoNotMinifyDoctype(true)
                .setEnsureSpecCompliantUnquotedAttributeValues(true)
                .setKeepSpacesBetweenAttributes(true)
                .setMinifyCss(true)
                .setMinifyJs(true)
                .build();
        return MinifyHtml.minify(html, cfg);
    }
}
