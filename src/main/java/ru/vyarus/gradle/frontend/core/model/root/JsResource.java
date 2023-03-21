package ru.vyarus.gradle.frontend.core.model.root;

import org.jsoup.nodes.Element;
import ru.vyarus.gradle.frontend.core.info.root.sub.SubResourceInfo;
import ru.vyarus.gradle.frontend.core.model.HtmlPage;
import ru.vyarus.gradle.frontend.util.FileUtils;
import ru.vyarus.gradle.frontend.util.SourceMapUtils;
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
        super(html, element, ATTR, html.getSettings().getJsDir());
    }

    @Override
    public void minify() {
        if (isIgnored() || file.getName().toLowerCase().contains(".min.")) {
            // already minified
            return;
        }
        long size = file.length();
        System.out.print("Minify " + FileUtils.relative(html.getBaseDir(), file));
        try {
            final MinifyResult min = JsMinifier.minify(file, getSettings().isSourceMaps());
            System.out.println(", " + (size - min.getMinified().length() * 100) / size + "% size decrease");
            if (min.getExtraLog() != null) {
                System.out.println(min.getExtraLog());
            }
            if (min.getSourceMap() != null) {
                System.out.println("\tSource map generated: "
                        + FileUtils.relative(getHtml().getFile(), min.getSourceMap()));
            }
            SourceMapUtils.includeSources(sourceMap);
            // remove original file
            System.out.println("\tMinified file source removed: " + file.getName());
            file.delete();

            minified(min);
        } catch (RuntimeException ex) {
            System.out.println(" FAILED");
            throw ex;
        }
    }

    @Override
    public List<? extends SubResourceInfo> getSubResources() {
        return Collections.emptyList();
    }
}

