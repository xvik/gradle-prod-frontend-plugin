package ru.vyarus.gradle.frontend.core.info;

import org.jsoup.nodes.Document;
import ru.vyarus.gradle.frontend.core.info.root.RootResourceInfo;

import java.io.File;
import java.util.List;

/**
 * @author Vyacheslav Rusakov
 * @since 24.02.2023
 */
public interface HtmlInfo extends ResourceInfo {

    Document getParsedDocument();

    File getFile();

    List<? extends RootResourceInfo> getJs();
    List<? extends RootResourceInfo> getCss();
}
