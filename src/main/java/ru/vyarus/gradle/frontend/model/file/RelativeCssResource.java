package ru.vyarus.gradle.frontend.model.file;

import ru.vyarus.gradle.frontend.model.OptimizedItem;
import ru.vyarus.gradle.frontend.model.stat.Stat;
import ru.vyarus.gradle.frontend.util.FileUtils;

import java.io.File;

/**
 * @author Vyacheslav Rusakov
 * @since 06.02.2023
 */
public class RelativeCssResource extends OptimizedItem {

    private final CssModel css;
    private final String url;
    private final File file;
    private final String target;
    private File gzip;

    public RelativeCssResource(final CssModel css, final String url, final File file) {
        this.css = css;
        this.url = url;
        this.file = file;
        this.target = FileUtils.relative(css.getFile(), file);
        recordStat(Stat.ORIGINAL, file.length());
        recordChange(url + " -> " + target);
    }

    public CssModel getCss() {
        return css;
    }

    public String getUrl() {
        return url;
    }

    public File getFile() {
        return file;
    }

    public String getTarget() {
        return target;
    }

    public File getGzip() {
        return gzip;
    }

    public void gzip() {
        gzip = FileUtils.gzip(file);
        recordStat(Stat.GZIP, gzip.length());
    }
}
