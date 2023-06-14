package ru.vyarus.gradle.frontend.core.util.minify;

import in.wilsonl.minifyhtml.Configuration;
import in.wilsonl.minifyhtml.MinifyHtml;

/**
 * Html minification with <a href="https://github.com/wilsonzlin/minify-html">minify-html</a>.
 *
 * @author Vyacheslav Rusakov
 * @since 01.02.2023
 */
public final class HtmlMinifier {

    private HtmlMinifier() {
    }

    /**
     * Minifies provided html.
     *
     * @param html      html to minify
     * @param minifyCss true to minify inner css
     * @param minifyJs  true to minify inner js
     * @return minified html
     */
    public static String minify(final String html, final boolean minifyCss, final boolean minifyJs) {
        final Configuration cfg = new Configuration.Builder()
                .setKeepHtmlAndHeadOpeningTags(true)
                .setDoNotMinifyDoctype(true)
                .setEnsureSpecCompliantUnquotedAttributeValues(true)
                .setKeepSpacesBetweenAttributes(true)
                .setMinifyCss(minifyCss)
                .setMinifyJs(minifyJs)
                .build();
        return MinifyHtml.minify(html, cfg);
    }
}
