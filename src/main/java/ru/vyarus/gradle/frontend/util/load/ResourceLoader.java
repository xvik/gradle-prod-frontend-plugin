package ru.vyarus.gradle.frontend.util.load;

import ru.vyarus.gradle.frontend.util.FileUtils;
import ru.vyarus.gradle.frontend.util.UrlUtils;

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
            // todo load source maps
            // trying to load min version directly (for many cdns .min.js|.min.css is a common convention)
            final String minUrl = url.replace(name, minName);
            final File target = FileUtils.selectNotExistingFile(targetDir, minName);
            try {
                UrlUtils.download(minUrl, target);
                res = target;
            } catch (Exception ex) {
                // failed to download minified version - no problem
                System.out.println("INFO: Minified resource version download failed: " + minUrl
                        + "(" + ex.getMessage() + ")");
            }
        }
        if (res == null) {
            File target = FileUtils.selectNotExistingFile(targetDir, name);
            try {
                UrlUtils.download(url, target);
                res = target;
            } catch (IOException ex) {
                System.out.println("ERROR: Failed to load resource '" + url + "': skipping");
                ex.printStackTrace();
                return null;
            }
        }
        return res;
    }
}
