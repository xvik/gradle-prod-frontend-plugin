package ru.vyarus.gradle.frontend.model.file;

import org.jsoup.nodes.Element;
import ru.vyarus.gradle.frontend.model.OptimizedItem;
import ru.vyarus.gradle.frontend.util.minify.MinifyResult;
import ru.vyarus.gradle.frontend.model.stat.Stat;
import ru.vyarus.gradle.frontend.util.FileUtils;

import java.io.File;

/**
 * @author Vyacheslav Rusakov
 * @since 30.01.2023
 */
public abstract class FileModel extends OptimizedItem {

    protected final Element element;
    protected File file;
    protected File sourceMap;
    protected File gzip;
    protected final String attr;

    public FileModel(final Element element, final File file, final String attr) {
        this.element = element;
        this.file = file;
        this.attr = attr;
        recordStat(Stat.ORIGINAL, file.length());
    }

    public File getFile() {
        return file;
    }

    public Element getElement() {
        return element;
    }

    public String getTarget() {
        return element.attr(attr);
    }

    public File getSourceMap() {
        return sourceMap;
    }

    public File getGzip() {
        return gzip;
    }

    public void changeTarget(String url) {
        final String old = element.attr(attr);
        element.attr(attr, url);
        recordChange(old + " -> " + url);
    }

    public void minified(final MinifyResult result) {
        if (result.isChanged(file)) {
            // might be the same file if source maps disabled
            changeFile(result.getMinified());
            sourceMap(result.getSourceMap());

            recordStat(Stat.MODIFIED, result.getMinified().length());
            recordChange("minimized");
        }
    }

    // IMPORTANT must be applied after possible minification (last steps!)
    public void applyMd5() {
        String md5 = FileUtils.computeMd5(file);
        changeTarget(getTarget() + "?" + md5);
    }

    // IMPORTANT must be applied after possible minification (last steps!)
    public void gzip() {
        gzip = FileUtils.gzip(file);
        recordStat(Stat.GZIP, gzip.length());
    }

    public abstract void minify(boolean generateSourceMaps);

    private void changeFile(final File file) {
        if (!this.file.equals(file)) {
            // assuming files are in the same directory
            String url = getTarget().replace(this.file.getName(), file.getName());
            this.file = file;
            changeTarget(url);
        }
    }

    private void sourceMap(final File file) {
        if (file != null) {
            this.sourceMap = file;
            recordChange("source map generated: " + file.getName());
        }
    }
}
