package ru.vyarus.gradle.frontend.core.model.root;

import org.jsoup.nodes.Element;
import ru.vyarus.gradle.frontend.core.info.root.sub.SubResourceInfo;
import ru.vyarus.gradle.frontend.core.model.HtmlPage;
import ru.vyarus.gradle.frontend.util.FileUtils;
import ru.vyarus.gradle.frontend.util.minify.JsMinifier;
import ru.vyarus.gradle.frontend.util.minify.MinifyResult;

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
        super(html, element, ATTR, html.getJsDir());
    }

    @Override
    public void minify() {
        if (isIgnored() || file.getName().toLowerCase().contains(".min.")) {
            // already minified
            return;
        }
        long size = file.length();
        System.out.print("Minify " + FileUtils.relative(html.getBaseDir(), file));
        final MinifyResult min = JsMinifier.minify(file, getSettings().isSourceMaps());
        System.out.println(", " + (size - min.getMinified().length() * 100) / size + "% size decrease");
        minified(min);
    }

    @Override
    public List<? extends SubResourceInfo> getSubResources() {
        return Collections.emptyList();
    }
}

