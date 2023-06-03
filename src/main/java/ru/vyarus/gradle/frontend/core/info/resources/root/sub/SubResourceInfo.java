package ru.vyarus.gradle.frontend.core.info.resources.root.sub;

import ru.vyarus.gradle.frontend.core.info.resources.OptimizedEntityInfo;

import java.io.File;

/**
 * Sub resource for root resource: currently might be only urls, declared in root css (extra css files like fonts).
 *
 * @author Vyacheslav Rusakov
 * @since 24.02.2023
 */
public interface SubResourceInfo extends OptimizedEntityInfo {

    /**
     * @return true if resource was downloaded, false for local file
     */
    boolean isRemote();

    /**
     * @return sub-resource file
     */
    File getFile();

    /**
     * For downloaded resource would be path to local file.
     *
     * @return resource url
     */
    String getTarget();

    /**
     * @return gzip file or null if not generated
     */
    File getGzip();
}
