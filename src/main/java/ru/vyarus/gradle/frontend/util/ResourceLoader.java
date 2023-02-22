package ru.vyarus.gradle.frontend.util;

import java.io.File;
import java.io.IOException;

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
        final String name = UrlUtils.getFileName(url);
        final String minName = FileUtils.getMinName(name);
        File res = null;
        if (!name.equals(minName) && preferMinified) {
            // trying to load min version directly (for many cdns .min.js|.min.css is a common convention)
            final String minUrl = url.replace(name, minName);
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
                res = UrlUtils.smartDownload(url, new File(targetDir, name));
            } catch (IOException ex) {
                System.out.println("ERROR: Failed to load resource '" + url + "': skipping");
                ex.printStackTrace();
                return null;
            }
        }

        if (sourceMaps) {
            // todo download original if source maps enabled
            String sourceMapUrl = WebUtils.getSourceMapReference(res);
            if (sourceMapUrl != null) {
                String fileName = UrlUtils.getFileName(sourceMapUrl);
                String urlBase = UrlUtils.getBaseUrl(url);
                final String targetUrl = urlBase + sourceMapUrl;
                try {
                    // will override existing file (assuming it would be downloaded AFTER main file
                    UrlUtils.download(targetUrl, new File(targetDir, fileName));
                } catch (IOException ex) {
                    System.out.println("ERROR: Failed to load source mapping file '" + targetUrl + "': skipping");
                    ex.printStackTrace();
                    return null;
                }
            }
        }
        return res;
    }
}
