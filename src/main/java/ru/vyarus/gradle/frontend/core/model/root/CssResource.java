package ru.vyarus.gradle.frontend.core.model.root;

import org.jsoup.nodes.Element;
import ru.vyarus.gradle.frontend.core.model.HtmlPage;
import ru.vyarus.gradle.frontend.core.model.root.sub.RelativeCssResource;
import ru.vyarus.gradle.frontend.core.util.CssUtils;
import ru.vyarus.gradle.frontend.core.util.FileUtils;
import ru.vyarus.gradle.frontend.core.util.UrlUtils;
import ru.vyarus.gradle.frontend.core.util.minify.CssMinifier;
import ru.vyarus.gradle.frontend.core.util.minify.ResourceMinifier;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * relative resources are not minified! (even css from import)
 * small images are not converted into data-urls!
 *
 * @author Vyacheslav Rusakov
 * @since 30.01.2023
 */
public class CssResource extends RootResource {
    // link to relative file reference
    private final List<RelativeCssResource> urls = new ArrayList<>();
    public static final String ATTR = "href";

    public CssResource(final HtmlPage html, final Element element) {
        super(html, element, ATTR, html.getSettings().getCssDir());
    }


    @Override
    public void resolve() {
        // important to remember root css url before resolution (when it would be loaded locally)
        String url = getTarget();
        super.resolve();

        if (!isIgnored()) {
            resolveSubLinks(url);

            // overwrite css with new links
            final List<RelativeCssResource> overrides = urls.stream()
                    .filter(resource -> resource.isRemote() && !resource.isIgnored())
                    .collect(Collectors.toList());
            if (!overrides.isEmpty()) {
                try {
                    // replacing like this to not harm minification
                    String content = Files.readString(file.toPath());
                    for (RelativeCssResource resource : overrides) {
                        if (getSettings().isApplyAntiCache()) {
                            resource.applyMd5();
                        }
                        content = content.replace(resource.getUrl(), resource.getTarget());
                    }
                    FileUtils.writeFile(file, content);
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to update css file links", e);
                }

                // downloaded css file may appear different with existing file just after download due to
                // changed urls inside css, so checking one more time after url changes were applied
                // Situation: html was overridden in already optimized folder (with loaded and processes css) so
                // css was loaded again on current processing into different file
                if (remote) {
                    // todo IMPLEMENT
                    // todo implement additional check after minification (for JS TOO!)
                }
            }
        }
    }

    @Override
    public void gzip() {
        super.gzip();
        urls.forEach(RelativeCssResource::gzip);
    }

    @Override
    public List<RelativeCssResource> getSubResources() {
        return urls;
    }

    @Override
    protected ResourceMinifier getMinifier() {
        return new CssMinifier();
    }

    private void resolveSubLinks(final String url) {
        // css could link other css (@import), fonts and images
        CssUtils.findLinks(file).forEach(link -> urls.add(new RelativeCssResource(this, link)));
        // if css was loaded, relative resources must be also loaded
        final String urlBase = remote ? UrlUtils.getBaseUrl(url) : null;
        urls.forEach(relativeCssResource -> relativeCssResource.resolve(getSettings().isDownloadResources(), urlBase));

        // check for duplicates (e.g. there might be similar links to web-font files)
        // remove duplicates only with exactly the same url!, preserving different urls for the same file
        // (important for proper replacement in css)
        final Iterator<RelativeCssResource> it = urls.iterator();
        final Set<String> added = new HashSet<>();
        while (it.hasNext()) {
            final RelativeCssResource res = it.next();
            if (res.getFile() != null) {
                final String path = res.getUrl();
                if (added.contains(path)) {
                    // duplicate
                    it.remove();
                } else {
                    added.add(path);
                }
            }
        }
    }
}
