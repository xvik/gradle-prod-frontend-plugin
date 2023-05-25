package ru.vyarus.gradle.frontend.core.model;

import org.jsoup.nodes.Document;
import ru.vyarus.gradle.frontend.core.OptimizationFlow;
import ru.vyarus.gradle.frontend.core.info.HtmlInfo;
import ru.vyarus.gradle.frontend.core.model.root.CssResource;
import ru.vyarus.gradle.frontend.core.model.root.JsResource;
import ru.vyarus.gradle.frontend.core.model.root.RootResource;
import ru.vyarus.gradle.frontend.core.stat.Stat;
import ru.vyarus.gradle.frontend.core.util.DebugReporter;
import ru.vyarus.gradle.frontend.core.util.FileUtils;
import ru.vyarus.gradle.frontend.core.util.HtmlParser;
import ru.vyarus.gradle.frontend.core.util.SizeFormatter;
import ru.vyarus.gradle.frontend.core.util.minify.HtmlMinifier;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Vyacheslav Rusakov
 * @since 30.01.2023
 */
public class HtmlPage extends OptimizedResource implements HtmlInfo {
    private final OptimizationFlow.Settings settings;
    private final File file;
    private File gzip;
    private Document doc;
    private final List<JsResource> js = new ArrayList<>();
    private final List<CssResource> css = new ArrayList<>();
    private final boolean pureHtml;

    public HtmlPage(final OptimizationFlow.Settings settings, final File file) {
        this.settings = settings;
        this.file = file;
        recordStat(Stat.ORIGINAL, file.length());
        final String name = file.getName().toLowerCase();
        // only pure html files could be minified (and updated using dom)
        pureHtml = name.endsWith(".htm") || name.endsWith(".html");
    }

    public OptimizationFlow.Settings getSettings() {
        return settings;
    }

    public File getBaseDir() {
        return settings.getBaseDir();
    }

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

    public List<JsResource> getJs() {
        return js;
    }

    public List<CssResource> getCss() {
        return css;
    }

    public File getFile() {
        return file;
    }

    public File getGzip() {
        return gzip;
    }

    public boolean isChanged() {
        if (hasChanges()) {
            return true;
        }
        final boolean cssChanges = this.css.stream()
                .anyMatch(OptimizedResource::hasChanges);
        final boolean jsChanges = this.js.stream()
                .anyMatch(OptimizedResource::hasChanges);
        return cssChanges || jsChanges;
    }

    public void findResources() {
        final HtmlParser.ParseResult res = HtmlParser.parse(file);
        doc = res.getDocument();
        res.getCss().forEach(element -> css.add(new CssResource(this, element.getElement(), element.getSource())));
        res.getJs().forEach(element -> js.add(new JsResource(this, element.getElement(), element.getSource())));
        if (settings.isDebug()) {
            System.out.println("Found: " + DebugReporter.buildReport(this));
        }
    }

    public void resolveResources() {
        js.forEach(RootResource::resolve);
        css.forEach(CssResource::resolve);
        if (settings.isDebug()) {
            System.out.println("Resolved: " + DebugReporter.buildReport(this));
        }
    }

    public void minifyJs() {
        js.forEach(JsResource::minify);
    }

    public void minifyCss() {
        css.forEach(CssResource::minify);
    }

    public void applyIntegrity() {
        css.forEach(RootResource::applyIntegrity);
        js.forEach(RootResource::applyIntegrity);
    }

    public void applyAntiCache() {
        css.forEach(RootResource::applyMd5);
        js.forEach(RootResource::applyMd5);

        if (settings.isDebug()) {
            System.out.println("Anti-cache: " + DebugReporter.buildReport(this));
        }
    }

    public void updateHtml() {
        // jsoup not used because it may apply unwanted changes, instead do manual replacements
        String content = FileUtils.readFile(file);
        if (isChanged()) {
            recordChange("changed links");
            for (JsResource js : getJs()) {
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
            }
            for (CssResource css : getCss()) {
                if (css.hasChanges()) {
                    final String source = css.getSourceDeclaration();
                    if (content.contains(source)) {
                        content = content.replace(source, css.getElement().toString());
                    } else {
                        System.out.println("WARNING: can't replace resource declaration in html file:\n\t" + source);
                    }
                }
            }
        }

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

        recordStat(Stat.MODIFIED, content.length());

        if (hasChanges()) {
            FileUtils.writeFile(file, content);
        }
    }

    // IMPORTANT must be applied after possible minification (last steps!)
    public void gzip() {
        gzip = FileUtils.gzip(file, getBaseDir());
        recordStat(Stat.GZIP, gzip.length());

        css.forEach(RootResource::gzip);
        js.forEach(RootResource::gzip);
    }
}
