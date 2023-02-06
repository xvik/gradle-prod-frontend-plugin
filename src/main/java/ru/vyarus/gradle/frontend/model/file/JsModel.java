package ru.vyarus.gradle.frontend.model.file;

import org.jsoup.nodes.Element;
import ru.vyarus.gradle.frontend.model.HtmlModel;
import ru.vyarus.gradle.frontend.util.minify.JsMinifier;

/**
 *
 * @author Vyacheslav Rusakov
 * @since 30.01.2023
 */
public class JsModel extends FileModel {

    public static final String ATTR = "src";

    public JsModel(final HtmlModel html, final Element element) {
        super(html, element, ATTR, html.getJsDir());
    }

    @Override
    public void minify(final boolean generateSourceMaps) {
        if (isIgnored() || file.getName().toLowerCase().contains(".min.")) {
            // already minified
            return;
        }
        minified(JsMinifier.minify(file, generateSourceMaps));
    }
}

