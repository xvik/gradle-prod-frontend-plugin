package ru.vyarus.gradle.frontend.model.file;

import org.jsoup.nodes.Element;
import ru.vyarus.gradle.frontend.util.minify.CssMinifier;

import java.io.File;

/**
 * @author Vyacheslav Rusakov
 * @since 30.01.2023
 */
public class CssModel extends FileModel {

    public static final String ATTR = "href";

    public CssModel(Element element, File file) {
        super(element, file, ATTR);
    }

    @Override
    public void minify(boolean generateSourceMaps) {
        if (file.getName().toLowerCase().contains(".min.")) {
            // already minified
            // todo try to only remove comments
            return;
        }
        // todo check if resulted file is LARGER
        minified(CssMinifier.minify(file, generateSourceMaps));
    }
}
