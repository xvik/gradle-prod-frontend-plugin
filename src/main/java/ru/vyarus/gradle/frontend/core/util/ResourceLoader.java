package ru.vyarus.gradle.frontend.core.util;

import java.io.File;

/**
 * Load remote resource. For CDN links could load minified version instead together with source maps.
 *
 * @author Vyacheslav Rusakov
 * @since 03.02.2023
 */
@SuppressWarnings("PMD.SystemPrintln")
public final class ResourceLoader {

    private ResourceLoader() {
    }

    /**
     * Load remote resource into local directory. For CDN link, could load minified resource version (following
     * common ".min" pattern) and its source map. Source map is loaded together with related sources (referenced
     * sources loaded and embedded inside source map).
     *
     * @param url               remote resource url
     * @param preferMinified    true to load minified version first
     * @param sourceMaps        true to load source map for minified version
     * @param targetDir         target directory to store loaded files
     * @return resulting object with local file representing loaded resource or null if load failed and source map
     * if it was loaded
     */
    public static LoadResult download(final String url,
                                      final boolean preferMinified,
                                      final boolean sourceMaps,
                                      final File targetDir) {
        return download(url, preferMinified, sourceMaps, targetDir, null);
    }

    /**
     * Load remote resource into local directory. For CDN link, could load minified resource version (following
     * common ".min" pattern) and its source map. Source map is loaded together with related sources (referenced
     * sources loaded and embedded inside source map).
     * <p>
     * When target extension is provided, validates downloaded file correctness and, when required, generates
     * custom name with correct extension (required for cases when link does not contain actual file name).
     *
     * @param url               remote resource url
     * @param preferMinified    true to load minified version first
     * @param sourceMaps        true to load source map for minified version
     * @param targetDir         target directory to store loaded files
     * @param requiredExtension required target extension (might be null)
     * @return resulting object with local file representing loaded resource or null if load failed and source map
     * if it was loaded
     */
    public static LoadResult download(final String url,
                                      final boolean preferMinified,
                                      final boolean sourceMaps,
                                      final File targetDir,
                                      final String requiredExtension) {
        // check redirects only when target file is unknown (folder references)
        final String realUrl = UrlUtils.hasExtension(url) ? url : UrlUtils.followRedirects(url);
        final String name = requiredExtension != null
                ? UrlUtils.selectFilename(realUrl, requiredExtension) : UrlUtils.getFileName(realUrl);
        File res = null;
        // don't try to download min version if file name was generated (could be if url does not contain extension,
        // like google fonts: https://fonts.googleapis.com/css?family=Roboto
        if (realUrl.contains(name)) {
            final String minName = FileUtils.getMinName(name);
            if (!name.equals(minName) && preferMinified) {
                // trying to load min version directly (for many cdns .min.js|.min.css is a common convention)
                res = tryLoadMin(realUrl.replace(name, minName), minName, targetDir);
            }
        }
        if (res == null) {
            // try to load as-is (load failure is OK)
            res = tryLoad(realUrl, name, targetDir);
        }

        File sourceMap = null;
        if (res != null && sourceMaps) {
            sourceMap = loadSourceMap(res, realUrl);
        }
        return new LoadResult(res, sourceMap);
    }

    private static File tryLoadMin(final String url, final String name, final File targetDir) {
        File res = null;
        try {
            res = UrlUtils.smartDownload(url, new File(targetDir, name));
            if (res.exists() && res.length() > 0) {
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

    private static File loadSourceMap(final File resource, final String downloadUrl) {
        File res = null;
        final String sourceMapUrl = SourceMapUtils.getSourceMapReference(resource);
        if (sourceMapUrl == null) {
            System.out.println("\tNo source map file reference found");

            // source maps file might be embedded!
        } else if (!sourceMapUrl.startsWith("data:")) {
            final String fileName = UrlUtils.selectFilename(sourceMapUrl, "map");
            // jsdeliver links sourcemaps to server root instead of relative to file
            final String urlBase = sourceMapUrl.startsWith("/") ? UrlUtils.getServerRoot(downloadUrl)
                    : UrlUtils.getBaseUrl(downloadUrl);
            final String targetUrl = urlBase + sourceMapUrl;
            try {
                // will override existing file (assuming it would be downloaded AFTER main file
                res = new File(resource.getParent(), fileName);
                UrlUtils.download(targetUrl, res);
                // load and append sources inside source map file
                SourceMapUtils.includeRemoteSources(res, urlBase);
            } catch (Exception ex) {
                System.out.println("ERROR: Failed to load source mapping file '" + targetUrl + "': "
                        + ex.getMessage() + ". Skipping");
            }
        }
        return res;
    }

    /**
     * Download result.
     */
    public static class LoadResult {
        private final File file;
        private final File sourceMap;

        public LoadResult(final File file, final File sourceMap) {
            this.file = file;
            this.sourceMap = sourceMap;
        }

        /**
         * @return downloaded file or null if file was not loaded
         */
        public File getFile() {
            return file;
        }

        /**
         * @return loaded source map or null
         */
        public File getSourceMap() {
            return sourceMap;
        }
    }
}
