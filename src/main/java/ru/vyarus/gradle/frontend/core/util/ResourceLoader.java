package ru.vyarus.gradle.frontend.core.util;

import java.io.File;

/**
 * Load remote resource. For CDN links could load minified version instead together with source maps.
 *
 * @author Vyacheslav Rusakov
 * @since 03.02.2023
 */
public final class ResourceLoader {

    private ResourceLoader() {
    }

    /**
     * Load remote resource into local directory. For CDN link, could load minified resource version (following
     * common ".min" pattern) and its source map. Source map is loaded together with related sources (referenced
     * sources loaded and embedded inside source map).
     *
     * @param url            remote resource url
     * @param preferMinified true to load minified version first
     * @param sourceMaps     true to load source map for minified version
     * @param targetDir      target directory to store loaded files
     * @return local file representing loaded resource or null if load failed
     */
    public static File download(final String url,
                                final boolean preferMinified,
                                final boolean sourceMaps,
                                final File targetDir) {
        // check redirects only when target file is unknown (folder references)
        final String realUrl = UrlUtils.hasExtension(url) ? url : UrlUtils.followRedirects(url);
        final String name = UrlUtils.getFileName(realUrl);
        final String minName = FileUtils.getMinName(name);
        File res = null;
        if (!name.equals(minName) && preferMinified) {
            // trying to load min version directly (for many cdns .min.js|.min.css is a common convention)
            res = tryLoadMin(realUrl.replace(name, minName), minName, targetDir);
        }
        if (res == null) {
            // try to load as-is (load failure is OK)
            res = tryLoad(realUrl, name, targetDir);
        }

        if (res != null && sourceMaps) {
            loadSourceMap(res, realUrl);
        }
        return res;
    }

    private static File tryLoadMin(final String url, final String name, final File targetDir) {
        File res = null;
        try {
            res = UrlUtils.smartDownload(url, new File(targetDir, name));
            if (res != null) {
                System.out.println("\tMinified version found and downloaded");
            }
        } catch (Exception ex) {
            // failed to download minified version - no problem
            System.out.println("INFO: Minified resource version download failed: " + url
                    + "(" + ex.getMessage() + ")");
        }
        return res;
    }

    private static File tryLoad(final String url, final String name, final File targetDir) {
        try {
            return UrlUtils.smartDownload(url, new File(targetDir, name));
        } catch (Exception ex) {
            System.out.println("ERROR: Failed to load resource '" + url + "': " + ex.getMessage() + ". Skipping");
            return null;
        }
    }

    private static void loadSourceMap(final File resource, final String downloadUrl) {
        final String sourceMapUrl = SourceMapUtils.getSourceMapReference(resource);
        if (sourceMapUrl == null) {
            System.out.println("\tNo source map file reference found");

            // source maps file might be embedded!
        } else if (!sourceMapUrl.startsWith("data:")) {
            final String fileName = UrlUtils.getFileName(sourceMapUrl);
            // jsdeliver links sourcemaps to server root instead of relative to file
            final String urlBase = sourceMapUrl.startsWith("/") ? UrlUtils.getServerRoot(downloadUrl)
                    : UrlUtils.getBaseUrl(downloadUrl);
            final String targetUrl = urlBase + sourceMapUrl;
            try {
                // will override existing file (assuming it would be downloaded AFTER main file
                final File mapFile = new File(resource.getParent(), fileName);
                UrlUtils.download(targetUrl, mapFile);
                // load and append sources inside source map file
                SourceMapUtils.includeRemoteSources(mapFile, urlBase);
            } catch (Exception ex) {
                System.out.println("ERROR: Failed to load source mapping file '" + targetUrl + "': "
                        + ex.getMessage() + ". Skipping");
            }
        }
    }
}
