package ru.vyarus.gradle.frontend.util.minify;

import java.io.File;

/**
 * @author Vyacheslav Rusakov
 * @since 01.02.2023
 */
public class MinifyResult {

    private final File minified;
    private final File sourceMap;

    public MinifyResult(final File minified, final File sourceMap) {
        this.minified = minified;
        this.sourceMap = sourceMap;
    }

    public File getMinified() {
        return minified;
    }

    public File getSourceMap() {
        return sourceMap;
    }

    public boolean isChanged(final File original) {
        return !original.equals(minified);
    }
}
