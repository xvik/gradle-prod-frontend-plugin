package ru.vyarus.gradle.frontend.core.util.minify;

import java.io.File;

/**
 * @author Vyacheslav Rusakov
 * @since 23.03.2023
 */
public interface ResourceMinifier {

    MinifyResult minify(final File file, final boolean sourceMap);
}
