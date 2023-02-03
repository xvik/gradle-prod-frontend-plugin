package ru.vyarus.gradle.frontend.model.file;

import org.jsoup.nodes.Element;
import ru.vyarus.gradle.frontend.model.HtmlModel;
import ru.vyarus.gradle.frontend.util.minify.CssMinifier;

import java.io.File;

/**
 * @author Vyacheslav Rusakov
 * @since 30.01.2023
 */
public class CssModel extends FileModel {

    public static final String ATTR = "href";

    public CssModel(final HtmlModel html, final Element element) {
        super(html, element, ATTR, html.getCssDir());
    }

    @Override
    public void minify(final boolean generateSourceMaps) {
        if (isIgnored() || file.getName().toLowerCase().contains(".min.")) {
            // already minified
            // todo try to only remove comments
            return;
        }
        // todo check if resulted file is LARGER
        minified(CssMinifier.minify(file, generateSourceMaps));
    }
}
