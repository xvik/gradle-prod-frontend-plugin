package ru.vyarus.gradle.frontend.model;

import org.jsoup.nodes.Document;
import ru.vyarus.gradle.frontend.model.file.CssModel;
import ru.vyarus.gradle.frontend.model.file.FileModel;
import ru.vyarus.gradle.frontend.model.file.JsModel;
import ru.vyarus.gradle.frontend.model.stat.Stat;
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
public class HtmlModel extends OptimizedItem {
    private final OptimizationModel model;
    private final File file;
    private File gzip;
    private Document doc;
    private final List<JsModel> js = new ArrayList<>();
    private final List<CssModel> css = new ArrayList<>();

    public HtmlModel(final OptimizationModel model, final File file) {
        this.model = model;
        this.file = file;
        recordStat(Stat.ORIGINAL, file.length());
    }

    public OptimizationModel getModel() {
        return model;
    }

    public File getBaseDir() {
        return model.getBaseDir();
    }

    public File getJsDir() {
        return model.getJsDir();
    }

    public File getCssDir() {
        return model.getCssDir();
    }

    public File getHtmlDir() {
        return file.getParentFile();
    }

    public Document getDoc() {
        return doc;
    }

    public List<JsModel> getJs() {
        return js;
    }

    public List<CssModel> getCss() {
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
                .anyMatch(OptimizedItem::hasChanges);
        final boolean jsChanges = this.js.stream()
                .anyMatch(OptimizedItem::hasChanges);
        return cssChanges || jsChanges;
    }

    public void minifyJs(boolean generateSourceMaps) {
        js.forEach(jsFileModel -> jsFileModel.minify(generateSourceMaps));
    }

    public void minifyCss(boolean generateSourceMaps) {
        css.forEach(cssFileModel -> cssFileModel.minify(generateSourceMaps));
    }

    public void applyAntiCache() {
        css.forEach(FileModel::applyMd5);
        js.forEach(FileModel::applyMd5);

        if (model.isDebug()) {
            System.out.println("Anti-cache: " + DebugReporter.buildHtmlReport(this));
        }
    }

    // IMPORTANT must be applied after possible minification (last steps!)
    public void gzip() {
        gzip = FileUtils.gzip(file, getBaseDir());
        recordStat(Stat.GZIP, gzip.length());

        css.forEach(FileModel::gzip);
        js.forEach(FileModel::gzip);
    }

    public void updateHtml(boolean minimize) {
        String content = doc.outerHtml();
        if (isChanged()) {
            recordChange("changed links");
        }

        if (minimize) {
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

    public void findResources() {
        final HtmlParser.ParseResult res = HtmlParser.parse(file);
        doc = res.getDocument();
        res.getCss().forEach(element -> css.add(new CssModel(this, element)));
        res.getJs().forEach(element -> js.add(new JsModel(this, element)));
        if (model.isDebug()) {
            System.out.println("Found: " + DebugReporter.buildHtmlReport(this));
        }
    }

    public void resolveResources(final boolean download,
                                 final boolean preferMinified,
                                 final boolean sourceMaps) {
        js.forEach(js -> js.resolve(download, preferMinified, sourceMaps));
        css.forEach(css -> css.resolve(download, preferMinified, sourceMaps));
        if (model.isDebug()) {
            System.out.println("Resolved: " + DebugReporter.buildHtmlReport(this));
        }
    }
}
