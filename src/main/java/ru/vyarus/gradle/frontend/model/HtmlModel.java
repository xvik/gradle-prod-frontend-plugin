package ru.vyarus.gradle.frontend.model;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.vyarus.gradle.frontend.model.file.CssFileModel;
import ru.vyarus.gradle.frontend.model.file.FileModel;
import ru.vyarus.gradle.frontend.model.file.JsFileModel;
import ru.vyarus.gradle.frontend.model.stat.Stat;
import ru.vyarus.gradle.frontend.util.FileUtils;
import ru.vyarus.gradle.frontend.util.minify.HtmlMinifier;

import java.io.File;
import java.io.IOException;
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
    private final List<JsFileModel> js = new ArrayList<>();
    private final List<CssFileModel> css = new ArrayList<>();

    public HtmlModel(File jsDir, File cssDir, File file) throws Exception {
        this.jsDir = jsDir;
        this.cssDir = cssDir;
        this.file = file;
        recordStat(Stat.ORIGINAL, file.length());
        parse();
    }

    public Document getDoc() {
        return doc;
    }

    public List<JsFileModel> getJs() {
        return js;
    }

    public List<CssFileModel> getCss() {
        return css;
    }

    public File getFile() {
        return file;
    }

    public File getGzip() {
        return gzip;
    }

    public boolean isChanged() {
        if (!getChanges().isEmpty()) {
            return true;
        }
        final Optional<Boolean> css = this.css.stream()
                .map(cssFileModel -> !cssFileModel.getChanges().isEmpty())
                .findAny();
        final Optional<Boolean> js = this.js.stream()
                .map(cssFileModel -> !cssFileModel.getChanges().isEmpty())
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

        if (!getChanges().isEmpty()) {
            FileUtils.writeFile(file, content);
        }
    }

    private void parse() throws Exception {
        doc = Jsoup.parse(file);
        final Elements css = doc.select("link[href]");
        final Elements jss = doc.select("script[src]");

        resolveFiles(css, CssFileModel.ATTR, cssDir, getCss(), CssFileModel::new);
        resolveFiles(jss, JsFileModel.ATTR, jsDir, getJs(), JsFileModel::new);
    }

    private <T extends FileModel> void resolveFiles(final Elements elements,
                                                    final String attr,
                                                    final File targetDir,
                                                    final List<T> models,
                                                    final ResourceFactory<T> factory) {

        for (Element el : elements) {
            final String target = el.attr(attr);

            File file;
            String changedUrl = null;
            if (target.toLowerCase().startsWith("http")) {
                // url - just downloading it to local directory here (as-is)
                // TODO try to download min version
                String name = FileUtils.getFileName(target);
                // todo check existence
                file = new File(targetDir, name);
                try {
                    FileUtils.download(target, file);
                } catch (IOException ex) {
                    System.out.println("ERROR: failed to load url '" + target + "': skipping");
                    ex.printStackTrace();
                    continue;
                }
                changedUrl = this.file.getParentFile().toPath().relativize(file.toPath()).toString();
            } else {
                // local file
                file = new File(this.file.getParentFile(), target);
                if (!file.exists()) {
                    System.out.println("WARNING: " + file.getAbsolutePath() + " referenced from " + this.file.getAbsolutePath()
                            + " not found: no optimizations would be applied");
                    continue;
                }
            }
            T model = factory.create(el, file);
            if (changedUrl != null) {
                model.changeTarget(changedUrl);
            }
            models.add(model);
        }
    }

    @FunctionalInterface
    private interface ResourceFactory<T extends FileModel> {
        T create(Element element, File file);
    }
}
