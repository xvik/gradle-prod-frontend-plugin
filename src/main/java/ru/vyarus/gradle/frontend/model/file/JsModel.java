package ru.vyarus.gradle.frontend.model.file;

import org.jsoup.nodes.Element;
import ru.vyarus.gradle.frontend.util.minify.JsMinifier;

import java.io.File;

/**
 *
 * @author Vyacheslav Rusakov
 * @since 30.01.2023
 */
public class JsModel extends FileModel {

    public static final String ATTR = "src";

    public JsModel(final Element element, final File file) {
        super(element, file, ATTR);
    }

    @Override
    public void minify(boolean generateSourceMaps) {
        if (file.getName().toLowerCase().contains(".min.")) {
            // already minified
            return;
        }
        minified(JsMinifier.minify(file, generateSourceMaps));
    }
}

