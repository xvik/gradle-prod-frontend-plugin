package ru.vyarus.gradle.frontend.model.file;

import org.jsoup.nodes.Element;
import ru.vyarus.gradle.frontend.model.HtmlModel;
import ru.vyarus.gradle.frontend.model.OptimizedItem;
import ru.vyarus.gradle.frontend.model.stat.Stat;
import ru.vyarus.gradle.frontend.util.FileUtils;
import ru.vyarus.gradle.frontend.util.ResourceLoader;
import ru.vyarus.gradle.frontend.util.minify.MinifyResult;

import java.io.File;

/**
 * @author Vyacheslav Rusakov
 * @since 30.01.2023
 */
public abstract class FileModel extends OptimizedItem {

    protected final HtmlModel html;
    protected final Element element;
    protected File file;
    protected File sourceMap;
    protected File gzip;
    protected final String attr;
    protected final File dir;

    protected boolean remote;

    public FileModel(final HtmlModel html, final Element element, final String attr, final File dir) {
        this.html = html;
        this.element = element;
        this.attr = attr;
        this.dir = dir;
    }

    public void resolve(final boolean download, final boolean preferMinified, final boolean sourceMaps) {
        final String target = getTarget();

        if (target.toLowerCase().startsWith("http")) {
            remote = true;
            if (download) {
                // url - just downloading it to local directory here (as-is)
                file = ResourceLoader.download(target, preferMinified, sourceMaps, dir);
                if (file == null) {
                    // leave link as is - no optimizations
                    System.out.println("WARNING: failed to download resource " + target);
                    ignore("download fail");
                } else {
                    // update target
                    changeTarget(FileUtils.relative(html.getFile(), file));
                }
            } else {
                ignore("remote resource");
            }
        } else {
            // local file
            file = new File(html.getHtmlDir(), FileUtils.unhash(target));
            if (!file.exists()) {
                System.out.println("WARNING: " + FileUtils.relative(html.getFile(), file) + " (referenced from "
                        + FileUtils.relative(html.getBaseDir(), html.getFile())
                        + ") not found: no optimizations would be applied");

                ignore("not found");
            }
        }

        if (file != null && file.exists()) {
            recordStat(Stat.ORIGINAL, file.length());
        }
    }

    public HtmlModel getHtml() {
        return html;
    }

    public boolean isRemote() {
        return remote;
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
            recordChange("minified");
        }
    }

    // IMPORTANT must be applied after possible minification (last steps!)
    public void applyMd5() {
        if (file != null && file.exists()) {
            String md5 = FileUtils.computeMd5(file);
            // md5 might be already applied
            if (!getTarget().endsWith(md5)) {
                changeTarget(FileUtils.unhash(getTarget()) + "?" + md5);
            }
        }
    }

    // IMPORTANT must be applied after possible minification (last steps!)
    public void gzip() {
        if (file != null && file.exists()) {
            gzip = FileUtils.gzip(file, html.getBaseDir());
            recordStat(Stat.GZIP, gzip.length());
        }
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
