package ru.vyarus.gradle.frontend.core.util.minify;

import java.io.File;

/**
 * Resource minification result.
 *
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

    /**
     * @return minified file
     */
    public File getMinified() {
        return minified;
    }

    /**
     * @return source map file or null
     */
    public File getSourceMap() {
        return sourceMap;
    }

    /**
     * @param original original file
     * @return true if minified file differ, false otherwise
     */
    public boolean isChanged(final File original) {
        return !original.equals(minified);
    }

    /**
     * @return additional log messages (some minifiers provide additional logs)
     */
    public String getExtraLog() {
        return extraLog;
    }
}
