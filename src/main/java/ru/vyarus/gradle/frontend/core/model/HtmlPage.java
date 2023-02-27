package ru.vyarus.gradle.frontend.core.model;

import org.jsoup.nodes.Document;
import ru.vyarus.gradle.frontend.core.OptimizationFlow;
import ru.vyarus.gradle.frontend.core.model.root.CssResource;
import ru.vyarus.gradle.frontend.core.model.root.JsResource;
import ru.vyarus.gradle.frontend.core.model.root.RootResource;
import ru.vyarus.gradle.frontend.core.stat.Stat;
import ru.vyarus.gradle.frontend.core.info.HtmlInfo;
import ru.vyarus.gradle.frontend.util.DebugReporter;
import ru.vyarus.gradle.frontend.util.FileUtils;
import ru.vyarus.gradle.frontend.util.HtmlParser;
import ru.vyarus.gradle.frontend.util.minify.HtmlMinifier;

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

    public HtmlPage(final OptimizationFlow.Settings settings, final File file) {
        this.settings = settings;
        this.file = file;
        recordStat(Stat.ORIGINAL, file.length());
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
        res.getCss().forEach(element -> css.add(new CssResource(this, element)));
        res.getJs().forEach(element -> js.add(new JsResource(this, element)));
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
        String content = doc.outerHtml();
        if (isChanged()) {
            recordChange("changed links");
        }

        if (getSettings().isMinifyHtml()) {
            // using file length because content would contain DIFFERENT output and on consequent runs result could
            // be the same
            long size = file.length();
            System.out.print("Minify " + FileUtils.relative(getBaseDir(), file));
            content = HtmlMinifier.minify(content);
            System.out.println(", " + (size - content.length()) * 100 / size + "% size decrease");
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
