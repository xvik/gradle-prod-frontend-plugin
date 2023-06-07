package ru.vyarus.gradle.frontend.core.util;

import java.io.File;

/**
 * @author Vyacheslav Rusakov
 * @since 03.02.2023
 */
public final class ResourceLoader {

    private ResourceLoader() {
    }

    public static File download(final String url,
                                final boolean preferMinified,
                                final boolean sourceMaps,
                                final File targetDir) {
        // check redirects only when target file is unknown (folder references)
        String realUrl = UrlUtils.hasExtension(url) ? url : UrlUtils.followRedirects(url);
        String name = UrlUtils.getFileName(realUrl);
        final String minName = FileUtils.getMinName(name);
        File res = null;
        if (!name.equals(minName) && preferMinified) {
            // trying to load min version directly (for many cdns .min.js|.min.css is a common convention)
            final String minUrl = realUrl.replace(name, minName);
            try {
                res = UrlUtils.smartDownload(minUrl, new File(targetDir, minName));
            } catch (Exception ex) {
                // failed to download minified version - no problem
                System.out.println("INFO: Minified resource version download failed: " + minUrl
                        + "(" + ex.getMessage() + ")");
            }
        }
        if (res == null) {
            try {
                res = UrlUtils.smartDownload(realUrl, new File(targetDir, name));
            } catch (Exception ex) {
                System.out.println("ERROR: Failed to load resource '" + realUrl + "': skipping");
                ex.printStackTrace();
                return null;
            }
        }

        if (sourceMaps) {
            String sourceMapUrl = SourceMapUtils.getSourceMapReference(res);
            if (sourceMapUrl == null) {
                System.out.println("No source map file reference found");

                // source maps file might be embedded!
            } else if (!sourceMapUrl.startsWith("data:")) {
                String fileName = UrlUtils.getFileName(sourceMapUrl);
                // jsdeliver links sourcemaps to server root instead of relative to file
                String urlBase = sourceMapUrl.startsWith("/") ? UrlUtils.getServerRoot(realUrl)
                        : UrlUtils.getBaseUrl(realUrl);
                final String targetUrl = urlBase + sourceMapUrl;
                try {
                    // will override existing file (assuming it would be downloaded AFTER main file
                    final File mapFile = new File(targetDir, fileName);
                    UrlUtils.download(targetUrl, mapFile);
                    // load and append sources inside source map file
                    SourceMapUtils.includeRemoteSources(mapFile, urlBase);
                } catch (Exception ex) {
                    System.out.println("ERROR: Failed to load source mapping file '" + targetUrl + "': skipping");
                    ex.printStackTrace();
                    return null;
                }
            }
        }
        return res;
    }
}
