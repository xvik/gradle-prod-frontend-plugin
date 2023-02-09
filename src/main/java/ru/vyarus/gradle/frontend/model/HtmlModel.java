package ru.vyarus.gradle.frontend.model;

import org.jsoup.nodes.Document;
import ru.vyarus.gradle.frontend.model.file.CssModel;
import ru.vyarus.gradle.frontend.model.file.FileModel;
import ru.vyarus.gradle.frontend.model.file.JsModel;
import ru.vyarus.gradle.frontend.model.stat.Stat;
import ru.vyarus.gradle.frontend.util.FileUtils;
import ru.vyarus.gradle.frontend.util.HtmlParser;
import ru.vyarus.gradle.frontend.util.minify.HtmlMinifier;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Vyacheslav Rusakov
 * @since 30.01.2023
 */
public class HtmlModel extends OptimizedItem {
    private final File jsDir;
    private final File cssDir;
    private final File file;
    private File gzip;
    private Document doc;
    private final List<JsModel> js = new ArrayList<>();
    private final List<CssModel> css = new ArrayList<>();

    public HtmlModel(final File jsDir, final File cssDir, final File file) throws Exception {
        this.jsDir = jsDir;
        this.cssDir = cssDir;
        this.file = file;
        recordStat(Stat.ORIGINAL, file.length());
    }

    public File getJsDir() {
        return jsDir;
    }

    public File getCssDir() {
        return cssDir;
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
        final Optional<Boolean> css = this.css.stream()
                .map(OptimizedItem::hasChanges)
                .findAny();
        final Optional<Boolean> js = this.js.stream()
                .map(OptimizedItem::hasChanges)
                .findAny();
        return css.isPresent() || js.isPresent();
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
    }

    // IMPORTANT must be applied after possible minification (last steps!)
    public void gzip() {
        gzip = FileUtils.gzip(file);
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
            content = HtmlMinifier.minify(content);
            recordChange("minimized");
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
    }

    public void resolveResources(final boolean download,
                                 final boolean preferMinified,
                                 final boolean sourceMaps) {
        js.forEach(js -> js.resolve(download, preferMinified, sourceMaps));
        css.forEach(css -> css.resolve(download, preferMinified, sourceMaps));
    }
}
