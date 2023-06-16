package ru.vyarus.gradle.frontend.core.model;

import org.jsoup.nodes.Document;
import ru.vyarus.gradle.frontend.core.OptimizationFlow;
import ru.vyarus.gradle.frontend.core.info.SizeType;
import ru.vyarus.gradle.frontend.core.info.resources.HtmlInfo;
import ru.vyarus.gradle.frontend.core.model.root.CssResource;
import ru.vyarus.gradle.frontend.core.model.root.JsResource;
import ru.vyarus.gradle.frontend.core.model.root.RootResource;
import ru.vyarus.gradle.frontend.core.util.DebugReporter;
import ru.vyarus.gradle.frontend.core.util.FileUtils;
import ru.vyarus.gradle.frontend.core.util.HtmlParser;
import ru.vyarus.gradle.frontend.core.util.SizeFormatter;
import ru.vyarus.gradle.frontend.core.util.minify.HtmlMinifier;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Html page object (root optimization entity). All optimization logic is directly contained inside this object
 * (but optimization methods must be called in correct order).
 *
 * @author Vyacheslav Rusakov
 * @since 30.01.2023
 */
public class HtmlPage extends OptimizedEntity implements HtmlInfo {
    /**
     * Optimization settings (can't change in time of optimization).
     */
    private final OptimizationFlow.Settings settings;
    /**
     * Html file to optimize.
     */
    private final File file;
    /**
     * Gzipped files (appears after gzipping).
     */
    private File gzip;
    /**
     * Parsed jsoup tree.
     */
    private Document doc;
    /**
     * Detected root js links.
     */
    private final List<JsResource> js = new ArrayList<>();
    /**
     * Detected root css links.
     */
    private final List<CssResource> css = new ArrayList<>();
    /**
     * True if html file has html extension (pure html, not template).
     */
    private final boolean pureHtml;

    public HtmlPage(final OptimizationFlow.Settings settings, final File file) {
        this.settings = settings;
        this.file = file;
        recordSize(SizeType.ORIGINAL, file.length());
        final String name = file.getName().toLowerCase();
        pureHtml = name.endsWith(".htm") || name.endsWith(".html");
    }

    /**
     * @return optimization setting (immutable)
     */
    public OptimizationFlow.Settings getSettings() {
        return settings;
    }

    /**
     * Shortcut for {@code getSettings().getBaseDir()}. Used for reporting to avoid long file paths.
     *
     * @return root scanned directory
     */
    public File getBaseDir() {
        return settings.getBaseDir();
    }

    /**
     * @return directory containing this html file
     */
    public File getHtmlDir() {
        return file.getParentFile();
    }

    @Override
    public Document getParsedDocument() {
        return doc;
    }

    @Override
    public boolean isPureHtml() {
        return pureHtml;
    }

    @Override
    public List<JsResource> getJs() {
        return js;
    }

    @Override
    public List<CssResource> getCss() {
        return css;
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public File getGzip() {
        return gzip;
    }

    /**
     * @return true if html was changed or any related resource (js, css)
     */
    public boolean isAnythingChanged() {
        if (hasChanges()) {
            return true;
        }
        return this.css.stream().anyMatch(OptimizedEntity::hasChanges)
                || this.js.stream().anyMatch(OptimizedEntity::hasChanges);
    }

    // ACTIONS ---------------------------------------------------------------------------

    /**
     * Parse html file and search for js and css links inside html.
     */
    public void findResources() {
        final HtmlParser.ParseResult res = HtmlParser.parse(file);
        doc = res.getDocument();
        res.getCss().forEach(element -> css.add(new CssResource(this, element.getElement(), element.getSource())));
        res.getJs().forEach(element -> js.add(new JsResource(this, element.getElement(), element.getSource())));
        if (settings.isDebug()) {
            System.out.println("Found: " + DebugReporter.buildReport(this));
        }
    }

    /**
     * Check detected resources existence, try to download remote resources.
     * If tags contain integrity attribute it is checked. Removes crossorigin attribute for downloaded resources.
     */
    public void resolveResources() {
        js.forEach(RootResource::resolve);
        css.forEach(CssResource::resolve);
        if (settings.isDebug()) {
            System.out.println("Resolved: " + DebugReporter.buildReport(this));
        }
    }

    /**
     * Minify js resources, if required (file does not contain ".min" in name).
     */
    public void minifyJs() {
        js.forEach(JsResource::minify);
    }

    /**
     * Minify css resources, if required (file does not contain ".min" in name).
     */
    public void minifyCss() {
        css.forEach(CssResource::minify);
    }

    /**
     * Compute SRI hash and apply integrity attribute into js and css tags.
     */
    public void applyIntegrity() {
        css.forEach(RootResource::applyIntegrity);
        js.forEach(RootResource::applyIntegrity);
    }

    /**
     * Compute MD5 for js and css files and apply it into urls.
     * NOTE: MD5 applied to urls in css sub resources under {@link #resolveResources()} (because without it impossible
     * to properly apply integrity and md5 for root css).
     */
    public void applyAntiCache() {
        css.forEach(RootResource::applyMd5);
        js.forEach(RootResource::applyMd5);

        if (settings.isDebug()) {
            System.out.println("Anti-cache: " + DebugReporter.buildReport(this));
        }
    }

    /**
     * Update js and css tags. Update performed with direct replacement of old tags instead of updating jsoup tree
     * because jsoup could damage templates (jsp, freemarker etc.).
     * Also, html content being minified (together with inner js and css).
     */
    public void updateHtml() {
        // jsoup not used because it may apply unwanted changes, instead do manual replacements
        String content = FileUtils.readFile(file);
        if (isAnythingChanged()) {
            recordChange("changed links");
            // replace js and css tags if resource changed
            for (JsResource js : getJs()) {
                content = updateJs(js, content);
            }
            for (CssResource css : getCss()) {
                content = updateCss(css, content);
            }
        }

        content = minifyHtml(content);
        recordSize(SizeType.MODIFIED, content.length());

        // has changes affects only html's own changes
        if (hasChanges()) {
            FileUtils.writeFile(file, content);
        }
    }

    /**
     * Generate gzip files for html and all related resources.
     * IMPORTANT must be applied after possible minification (last steps!) because, obviously, any further
     * modifications would make generated gzip invalid.
     */
    public void gzip() {
        gzip = FileUtils.gzip(file, getBaseDir());
        recordSize(SizeType.GZIPPED, gzip.length());

        css.forEach(RootResource::gzip);
        js.forEach(RootResource::gzip);
    }

    private String updateJs(final JsResource js, final String html) {
        String content = html;
        if (js.hasChanges()) {
            final String source = js.getSourceDeclaration();
            if (content.contains(source)) {
                String actualSource = js.getElement().toString();
                // jsoup can't properly track closing script element so known source doesn't include it,
                // but generated element would contain this tag - remove manually to avoid duplicates
                if (actualSource.endsWith("</script>")) {
                    actualSource = actualSource.substring(0, actualSource.length() - 9);
                }
                content = content.replace(source, actualSource);
            } else {
                System.out.println("WARNING: can't replace resource declaration in html file:\n\t" + source);
            }
        }
        return content;
    }

    private String updateCss(final CssResource css, final String html) {
        String content = html;
        if (css.hasChanges()) {
            final String source = css.getSourceDeclaration();
            if (content.contains(source)) {
                content = content.replace(source, css.getElement().toString());
            } else {
                System.out.println("WARNING: can't replace resource declaration in html file:\n\t" + source);
            }
        }
        return content;
    }

    private String minifyHtml(final String html) {
        String content = html;
        if (getSettings().isMinifyHtml()) {
            // using file length because content would contain DIFFERENT output and on consequent runs result could
            // be the same
            long size = file.length();
            System.out.print("Minify " + FileUtils.relative(getBaseDir(), file));
            content = HtmlMinifier.minify(content, settings.isMinifyHtmlCss(), settings.isMinifyHtmlJs());
            // html size MIGHT increase due to added integrity tags (if overall html was very small)
            System.out.println(", " + SizeFormatter.formatChangePercent(size, content.length()));
            if (size != content.length()) {
                recordChange("minified");
            }
        }
        return content;
    }
}
