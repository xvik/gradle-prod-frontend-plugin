package ru.vyarus.gradle.frontend.core.info.root;

import org.jsoup.nodes.Element;
import ru.vyarus.gradle.frontend.core.info.ResourceInfo;
import ru.vyarus.gradle.frontend.core.info.root.sub.SubResourceInfo;

import java.io.File;
import java.util.List;

/**
 * @author Vyacheslav Rusakov
 * @since 24.02.2023
 */
public interface RootResourceInfo extends ResourceInfo {

    Element getElement();

    String getSourceDeclaration();

    String getTarget();
    String getIntegrity();

    boolean isRemote();

    File getFile();
    File getSourceMap();
//    List<File> getMapSources();
    File getGzip();

    List<? extends SubResourceInfo> getSubResources();
}
