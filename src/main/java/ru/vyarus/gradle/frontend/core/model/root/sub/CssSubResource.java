package ru.vyarus.gradle.frontend.core.model.root.sub;

import ru.vyarus.gradle.frontend.core.info.resources.root.sub.SubResourceInfo;
import ru.vyarus.gradle.frontend.core.model.OptimizedEntity;
import ru.vyarus.gradle.frontend.core.model.root.CssResource;
import ru.vyarus.gradle.frontend.core.info.SizeType;
import ru.vyarus.gradle.frontend.core.util.FileUtils;
import ru.vyarus.gradle.frontend.core.util.ResourceLoader;
import ru.vyarus.gradle.frontend.core.util.UrlUtils;

import java.io.File;

/**
 * Resource, referenced from root css file (web font, image or other css file). Such resources must be also
 * downloaded and urls to them must be hashed with md5 (anti-cache). There is no post-processing for sub resources.
 *
 * @author Vyacheslav Rusakov
 * @since 06.02.2023
 */
@SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName", "PMD.SystemPrintln"})
public class CssSubResource extends OptimizedEntity implements SubResourceInfo {

    /**
     * Root css, referenced this resource.
     */
    private final CssResource css;
    /**
     * Original resource url.
     */
    private final String url;
    /**
     * Local resource files (null before it gets downloaded).
     */
    private File file;
    /**
     * Actual resource link: initially, same as url, but after downloading - path to local file.
     */
    private String target;
    /**
     * True for remote resource.
     */
    private boolean remote;
    /**
     * Gzip file (null before gzip generation).
     */
    private File gzip;

    public CssSubResource(final CssResource css, final String url) {
        this.css = css;
        this.url = url;
        this.target = url;
    }

    /**
     * @return root css, referenced this resource
     */
    public CssResource getCss() {
        return css;
    }

    /**
     * NOTE: does not change after download, see {@link #getTarget()} for actual link.
     *
     * @return original resource url
     */
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

    /**
     * Download remote resource or check local file for existence.
     *
     * @param download true to download remote resources
     * @param baseUrl  base url from where root css was downloaded (important for relative urls) or null if root css
     *                 is local
     */
    public void resolve(final boolean download, final String baseUrl) {
        if (url.startsWith("http")) {
            download(download);
        } else if (baseUrl != null) {
            // NOTE: no-download flag not checked gere because relative url for remote file would appear only if
            // root css was downloaded and so this one would need to be downloaded too
            downloadRelative(baseUrl);
        } else {
            // local file
            file = new File(css.getFile().getParentFile(), UrlUtils.clearParams(url));
            if (!file.exists()) {
                System.out.println("WARNING: " + file.getAbsolutePath() + " referenced from "
                        + css.getFile().getAbsolutePath() + " not found");

                ignore("not found");
            }
        }

        if (file != null && file.exists()) {
            recordSize(SizeType.ORIGINAL, file.length());
        }
    }

    /**
     * Generate gzip for resource.
     */
    public void gzip() {
        if (file != null && file.exists()) {
            gzip = FileUtils.gzip(file, css.getHtml().getBaseDir());
            recordSize(SizeType.GZIPPED, gzip.length());
        }
    }

    /**
     * Compute MD5 and apply it to target (url in root css still must be updated with new target).
     */
    public void applyMd5() {
        if (file != null && file.exists()) {
            final String md5 = FileUtils.computeMd5(file);
            // md5 might be already applied
            if (!getTarget().endsWith(md5)) {
                final String upd = UrlUtils.clearParams(getTarget()) + "?" + md5;
                recordChange(formatChange(target, upd));
                target = upd;
            }
        }
    }

    private void download(final boolean download) {
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
                recordChange(formatChange(url, target));
            }
        } else {
            ignore("remote resource");
        }
    }

    @SuppressWarnings("PMD.InefficientEmptyStringCheck")
    private void downloadRelative(final String baseUrl) {
        remote = true;
        // css was loaded and all relative resources must be also loaded

        // relative resources (often started with ../) - not loading them would lead to error

        // trying to preserve folder structure, but without going upper css location
        // name extraction required to get rid of anti-cache part (?v=132)
        final String name = UrlUtils.getFileName(url);
        String folder = null;
        // extracting folder from relative path
        final int idx = UrlUtils.getNameSeparatorPos(url);
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
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load relative css resource: " + url, e);
        }
        file = target;
        this.target = FileUtils.relative(css.getFile(), file);
        recordChange(formatChange(url, this.target));
    }

    private String formatChange(final String from, final String to) {
        return from + " -> " + to;
    }
}
