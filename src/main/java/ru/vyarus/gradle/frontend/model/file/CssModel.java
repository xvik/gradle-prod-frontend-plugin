package ru.vyarus.gradle.frontend.model.file;

import org.jsoup.nodes.Element;
import ru.vyarus.gradle.frontend.model.HtmlModel;
import ru.vyarus.gradle.frontend.util.UrlUtils;
import ru.vyarus.gradle.frontend.util.load.CssResourcesLoader;
import ru.vyarus.gradle.frontend.util.minify.CssMinifier;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Vyacheslav Rusakov
 * @since 30.01.2023
 */
public class CssModel extends FileModel {
    // link to relative file reference
    private final List<RelativeCssResource> urls = new ArrayList<>();
    public static final String ATTR = "href";

    public CssModel(final HtmlModel html, final Element element) {
        super(html, element, ATTR, html.getCssDir());
    }

    @Override
    public void resolve(boolean download, boolean preferMinified, boolean sourceMaps) {
        String url = getTarget();
        super.resolve(download, preferMinified, sourceMaps);

        if (remote && !ignored) {
            String urlBase = UrlUtils.getBaseUrl(url);
            // load links to css (@import), font references and image urls
            // NOTE: css links updated here with md5 computation
            final Map<String, File> urls = CssResourcesLoader.processLinks(file, urlBase);
            if (urls != null) {
                urls.forEach((s, file1) -> this.urls.add(new RelativeCssResource(this, s, file1)));
            }
        }
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

        // relative resources are not minified! (even css from import)
        // small images are not converted into data-urls!
    }

    @Override
    public void gzip() {
        super.gzip();
        urls.forEach(RelativeCssResource::gzip);
    }

    public List<RelativeCssResource> getUrls() {
        return urls;
    }
}
