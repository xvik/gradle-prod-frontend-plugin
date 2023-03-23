package ru.vyarus.gradle.frontend.core.model.root;

import org.jsoup.nodes.Element;
import ru.vyarus.gradle.frontend.core.info.root.sub.SubResourceInfo;
import ru.vyarus.gradle.frontend.core.model.HtmlPage;
import ru.vyarus.gradle.frontend.util.minify.JsMinifier;
import ru.vyarus.gradle.frontend.util.minify.ResourceMinifier;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author Vyacheslav Rusakov
 * @since 30.01.2023
 */
public class JsResource extends RootResource {

    public static final String ATTR = "src";

    public JsResource(final HtmlPage html, final Element element) {
        super(html, element, ATTR, html.getSettings().getJsDir());
    }

    @Override
    public List<? extends SubResourceInfo> getSubResources() {
        return Collections.emptyList();
    }

    @Override
    protected ResourceMinifier getMinifier() {
        return new JsMinifier();
    }
}

