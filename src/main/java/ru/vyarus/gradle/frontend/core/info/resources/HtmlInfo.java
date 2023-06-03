package ru.vyarus.gradle.frontend.core.info.resources;

import org.jsoup.nodes.Document;
import ru.vyarus.gradle.frontend.core.info.resources.root.ResourceInfo;

import java.io.File;
import java.util.List;

/**
 * Html page optimization info.
 *
 * @author Vyacheslav Rusakov
 * @since 24.02.2023
 */
public interface HtmlInfo extends OptimizedEntityInfo {

    /**
     * @return true if file has htm or html extension
     */
    boolean isPureHtml();

    /**
     * @return document tree parsed by jsoup (used for resources detection)
     */
    Document getParsedDocument();

    /**
     * @return html file
     */
    File getFile();

    /**
     * @return gzip file or null if wasn't generated
     */
    File getGzip();


    /**
     * @return detected js resources
     */
    List<? extends ResourceInfo> getJs();

    /**
     * @return detected js resources
     */
    List<? extends ResourceInfo> getCss();
}
