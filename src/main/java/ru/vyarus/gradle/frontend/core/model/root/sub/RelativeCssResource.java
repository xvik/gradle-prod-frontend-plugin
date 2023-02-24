package ru.vyarus.gradle.frontend.core.model.root.sub;

import ru.vyarus.gradle.frontend.core.info.root.sub.SubResourceInfo;
import ru.vyarus.gradle.frontend.core.model.OptimizedResource;
import ru.vyarus.gradle.frontend.core.model.root.CssResource;
import ru.vyarus.gradle.frontend.core.stat.Stat;
import ru.vyarus.gradle.frontend.util.FileUtils;
import ru.vyarus.gradle.frontend.util.UrlUtils;
import ru.vyarus.gradle.frontend.util.ResourceLoader;

import java.io.File;
import java.io.IOException;

/**
 * @author Vyacheslav Rusakov
 * @since 06.02.2023
 */
public class RelativeCssResource extends OptimizedResource implements SubResourceInfo {

    private final CssResource css;
    private final String url;
    private File file;
    private String target;
    private boolean remote;

    private File gzip;

    public RelativeCssResource(final CssResource css, final String url) {
        this.css = css;
        this.url = url;
        this.target = url;
    }

    public void resolve(final boolean download, final String baseUrl) {
        if (url.startsWith("http")) {
            remote = true;
            if (download) {
                file = ResourceLoader.download(target, false, false,
                        new File(css.getFile().getParentFile(), "resources"));
                if (file == null) {
                    // leave link as is - no optimizations
                    System.out.println("WARNING: failed to download resource " + target);
                    ignore("download fail");
                } else {
                    target = FileUtils.relative(css.getFile(), file);
                    recordChange(url + " -> " + target);
                }
            } else {
                ignore("remote resource");
            }
        } else if (baseUrl != null) {
            remote = true;
            // css was loaded and all relative resources must be also loaded

            // relative resources (often started with ../) - not loading them would lead to error

            // trying to preserve folder structure, but without going upper css location
            // name extraction required to get rid of anti-cache part (?v=132)
            String name = UrlUtils.getFileName(url);
            String folder = null;
            // extracting folder from relative path
            int idx = UrlUtils.getNameSeparatorPos(url);
            if (idx > 0) {
                folder = url.substring(0, idx).replace("../", "");
                if (folder.startsWith("/")) {
                    folder = folder.substring(1);
                }
            }
            if (folder == null || folder.trim().isEmpty()) {
                folder = "resources";
            }
            File target = new File(css.getFile().getParentFile().getAbsolutePath() + "/" + folder + "/" + name);
            try {
                final String targetUrl = baseUrl + url;
                target = UrlUtils.smartDownload(targetUrl, target);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to load relative css resource: " + url, e);
            }
            file = target;
            this.target = FileUtils.relative(css.getFile(), file);
            recordChange(url + " -> " + this.target);
        } else {
            // local file
            file = new File(css.getFile().getParentFile(), FileUtils.unhash(url));
            if (!file.exists()) {
                System.out.println("WARNING: " + file.getAbsolutePath() + " referenced from "
                        + css.getFile().getAbsolutePath() + " not found");

                ignore("not found");
            }
        }

        if (file != null && file.exists()) {
            recordStat(Stat.ORIGINAL, file.length());
        }
    }


    public CssResource getCss() {
        return css;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public String getTarget() {
        return target;
    }

    @Override
    public boolean isRemote() {
        return remote;
    }
    @Override
    public File getGzip() {
        return gzip;
    }

    public void gzip() {
        gzip = FileUtils.gzip(file, css.getHtml().getBaseDir());
        recordStat(Stat.GZIP, gzip.length());
    }

    public void applyMd5() {
        if (file != null && file.exists()) {
            String md5 = FileUtils.computeMd5(file);// md5 might be already applied
            if (!getTarget().endsWith(md5)) {
                String upd = FileUtils.unhash(getTarget()) + "?" + md5;
                recordChange(target + " -> " + upd);
                target = upd;
            }
        }
    }
}
