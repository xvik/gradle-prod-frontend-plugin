package ru.vyarus.gradle.frontend.model;

import org.gradle.api.GradleException;
import ru.vyarus.gradle.frontend.util.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Why no bundle: https://webspeedtools.com/should-i-combine-css-js/
 * * https://wpjohnny.com/why-you-shouldnt-combine-css-js-performance-reasons/
 *
 * @author Vyacheslav Rusakov
 * @since 30.01.2023
 */
public class OptimizationModel {
    private boolean debug;
    private final File baseDir;
    // for relative urls
    private final File jsDir;
    private final File cssDir;
    private List<HtmlModel> htmls = new ArrayList<>();

    public OptimizationModel(final File baseDir, final File jsDir, final File cssDir, final boolean debug) {
        this.baseDir = baseDir;
        this.jsDir = jsDir;
        this.cssDir = cssDir;
        this.debug = debug;

        if (!jsDir.exists()) {
            jsDir.mkdirs();
        }
        if (!cssDir.exists()) {
            cssDir.mkdirs();
        }
    }

    public File getBaseDir() {
        return baseDir;
    }

    public boolean isDebug() {
        return debug;
    }

    public File getJsDir() {
        return jsDir;
    }

    public File getCssDir() {
        return cssDir;
    }

    public List<HtmlModel> getHtmls() {
        return htmls;
    }

    public void minifyJs(boolean sourceMaps) {
        htmls.forEach(htmlModel -> htmlModel.minifyJs(sourceMaps));
    }

    public void minifyCss(boolean sourceMaps) {
        htmls.forEach(htmlModel -> htmlModel.minifyCss(sourceMaps));
    }

    public void applyAntiCache() {
        htmls.forEach(HtmlModel::applyAntiCache);
    }

    public void updateHtml(boolean minimize) {
        htmls.forEach(htmlModel -> htmlModel.updateHtml(minimize));
    }

    public void generateGzip() {
        htmls.forEach(HtmlModel::gzip);
    }

    public void findFiles() throws GradleException {
        final List<File> files = FileUtils.findHtmls(baseDir);
        for (File file : files) {
            try {
                final HtmlModel html = new HtmlModel(this, file);
                htmls.add(html);
                // todo apply exclusions
                html.findResources();
            } catch (Exception ex) {
                throw new GradleException("Failed to parse html " + file.getAbsolutePath(), ex);
            }
        }
    }

    public void resolveResources(final boolean download, final boolean preferMinified, final boolean sourceMaps) {

        // todo apply integrity tags
        // todo support downloading remote resources only for validation

        // todo sri should be used ONLY on loaded files because cdn min versions could be auto generated on demand
        // in https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.3/font/bootstrap-icons.min.css
        // * Do NOT use SRI with dynamically generated files! More information: https://www.jsdelivr.com/using-sri-with-dynamic-files

        htmls.forEach(html -> html.resolveResources(download, preferMinified, sourceMaps));
    }
}
