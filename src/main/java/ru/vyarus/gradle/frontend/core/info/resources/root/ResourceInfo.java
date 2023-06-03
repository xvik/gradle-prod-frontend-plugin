package ru.vyarus.gradle.frontend.core.info.resources.root;

import org.jsoup.nodes.Element;
import ru.vyarus.gradle.frontend.core.info.resources.OptimizedEntityInfo;
import ru.vyarus.gradle.frontend.core.info.resources.root.sub.SubResourceInfo;

import java.io.File;
import java.util.List;

/**
 * Root js or css resource info.
 *
 * @author Vyacheslav Rusakov
 * @since 24.02.2023
 */
public interface ResourceInfo extends OptimizedEntityInfo {

    /**
     * @return resource tag (from jsoup tree)
     */
    Element getElement();

    /**
     * For js resource would not include closing tag.
     *
     * @return resource tag original declaration
     */
    String getSourceDeclaration();

    /**
     * Note: for downloaded resource would contain local file path.
     *
     * @return resource location (value of href or url attribute in tag)
     */
    String getTarget();

    /**
     * Note: shows actual value (after html modification).
     *
     * @return value of integrity attribute or null
     */
    String getIntegrity();

    /**
     * @return true if resource was downloaded, false for local file
     */
    boolean isRemote();

    /**
     * @return resource file
     */
    File getFile();

    /**
     * @return source map file or null
     */
    File getSourceMap();

    /**
     * @return gzip file or null if wasn't generated
     */
    File getGzip();

    /**
     * @return sub resources (actual for css)
     */
    List<? extends SubResourceInfo> getSubResources();
}
