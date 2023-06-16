package ru.vyarus.gradle.frontend.core.info;

/**
 * File size statistics. Used for storing intermediate file sizes.
 *
 * @author Vyacheslav Rusakov
 * @since 30.01.2023
 */
public enum SizeType {

    /**
     * Original file size. For downloaded file might be already minified size, if minified version loaded from cdn.
     */
    ORIGINAL,
    /**
     * Minified file size.
     */
    MODIFIED,
    /**
     * Gzipped file size.
     */
    GZIPPED
}
