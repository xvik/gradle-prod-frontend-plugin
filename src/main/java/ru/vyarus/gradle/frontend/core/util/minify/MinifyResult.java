package ru.vyarus.gradle.frontend.core.util.minify;

import java.io.File;

/**
 * @author Vyacheslav Rusakov
 * @since 01.02.2023
 */
public class MinifyResult {

    private final File minified;
    private final File sourceMap;
    private final String extraLog;

    public MinifyResult(final File minified, final File sourceMap, final String extraLog) {
        this.minified = minified;
        this.sourceMap = sourceMap;
        this.extraLog = extraLog;
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

    public String getExtraLog() {
        return extraLog;
    }
}
