package ru.vyarus.gradle.frontend.model.file;

import org.jsoup.nodes.Element;
import ru.vyarus.gradle.frontend.model.HtmlModel;
import ru.vyarus.gradle.frontend.util.CssUtils;
import ru.vyarus.gradle.frontend.util.FileUtils;
import ru.vyarus.gradle.frontend.util.UrlUtils;
import ru.vyarus.gradle.frontend.util.minify.CssMinifier;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

        if (!isIgnored()) {
            // css could link other css (@import), fonts and images
            CssUtils.findLinks(file).forEach(link -> urls.add(new RelativeCssResource(this, link)));
            // if css was loaded, relative resources must be also loaded
            final String urlBase = remote ? UrlUtils.getBaseUrl(url) : null;
            urls.forEach(relativeCssResource -> relativeCssResource.resolve(download, urlBase));

            // overwrite css with new links
            final List<RelativeCssResource> overrides = urls.stream()
                    .filter(resource -> resource.isRemote() && !resource.isIgnored())
                    .collect(Collectors.toList());
            if (!overrides.isEmpty()) {
                try {
                    // replacing like this to not harm minification
                    String content = Files.readString(file.toPath());
                    for (RelativeCssResource resource : overrides) {
                        String md5 = FileUtils.computeMd5(resource.getFile());
                        content = content.replace(resource.getUrl(), resource.getTarget() + "?" + md5);
                    }
                    FileUtils.writeFile(file, content);
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to fix css file links", e);
                }
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
