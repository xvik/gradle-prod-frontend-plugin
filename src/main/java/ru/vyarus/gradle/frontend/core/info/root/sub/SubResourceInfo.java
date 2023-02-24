package ru.vyarus.gradle.frontend.core.info.root.sub;

import ru.vyarus.gradle.frontend.core.info.ResourceInfo;

import java.io.File;

/**
 * @author Vyacheslav Rusakov
 * @since 24.02.2023
 */
public interface SubResourceInfo extends ResourceInfo {

    boolean isRemote();
    File getFile();
    String getTarget();
    File getGzip();
}
