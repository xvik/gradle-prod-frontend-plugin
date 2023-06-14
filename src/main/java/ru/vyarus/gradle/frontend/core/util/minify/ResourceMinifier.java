package ru.vyarus.gradle.frontend.core.util.minify;

import java.io.File;

/**
 * Resource minifier (css or js).
 *
 * @author Vyacheslav Rusakov
 * @since 23.03.2023
 */
public interface ResourceMinifier {

    /**
     * @param file      file to minify
     * @param sourceMap true to generate source map
     * @return minification result
     */
    MinifyResult minify(final File file, final boolean sourceMap);
}
