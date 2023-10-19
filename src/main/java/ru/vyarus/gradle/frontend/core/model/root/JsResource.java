package ru.vyarus.gradle.frontend.core.model.root;

import org.jsoup.nodes.Element;
import ru.vyarus.gradle.frontend.core.info.resources.root.sub.SubResourceInfo;
import ru.vyarus.gradle.frontend.core.model.HtmlPage;
import ru.vyarus.gradle.frontend.core.util.minify.JsMinifier;
import ru.vyarus.gradle.frontend.core.util.minify.ResourceMinifier;

import java.util.Collections;
import java.util.List;

/**
 * JS root resource (referenced from html).
 *
 * @author Vyacheslav Rusakov
 * @since 30.01.2023
 */
public class JsResource extends RootResource {

    /**
     * Script tag attribute with url.
     */
    public static final String ATTR = "src";

    public JsResource(final HtmlPage html, final Element element, final String sourceDeclaration) {
        super(html, element, sourceDeclaration, ATTR, html.getSettings().getJsDir());
    }

    @Override
    public List<? extends SubResourceInfo> getSubResources() {
        return Collections.emptyList();
    }

    @Override
    protected ResourceMinifier getMinifier() {
        return new JsMinifier();
    }

    @Override
    protected String getFileExtension() {
        return "js";
    }
}

