package ru.vyarus.gradle.frontend.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;

/**
 * @author Vyacheslav Rusakov
 * @since 03.02.2023
 */
public final class ResourceLoader {

    private ResourceLoader() {
    }

    public static File download(final String url, final boolean minification, final File targetDir) {
        final String name = getFileName(url);
        final String minName = FileUtils.getMinName(name);
        File res = null;
        if (!name.equals(minName) && minification) {
            // todo load source maps
            // trying to load min version directly (for many cdns .min.js|.min.css is a common convention)
            final String minUrl = url.replace(name, minName);
            final File target = selectFile(targetDir, minName);
            try {
                download(minUrl, target);
                res = target;
            } catch (Exception ex) {
                // failed to download minified version - no problem
                System.out.println("INFO: Minified resource version download failed: " + minUrl
                        + "(" + ex.getMessage() + ")");
            }
        }
        if (res == null) {
            File target = selectFile(targetDir, name);
            try {
                download(url, target);
                res = target;
            } catch (IOException ex) {
                System.out.println("ERROR: Failed to load resource '" + url + "': skipping");
                ex.printStackTrace();
                return null;
            }
        }
        return res;
    }

    public static String getFileName(final String url) {
        int idx = url.lastIndexOf('/');
        if (idx == 0) {
            idx = url.lastIndexOf('\\');
        }
        if (idx > 0) {
            String res =  url.substring(idx + 1);
            // cut off possible redundant parts (maybe default anti-cache)
            for(char sep: Arrays.asList('?', '#')) {
                int i = res.indexOf(sep);
                if (i > 0) {
                    res = res.substring(0, i);
                }
            }
           return res;
        } else {
            throw new IllegalStateException("Failed to detect name in url: " + url);
        }
    }

    public static void download(final String urlStr, final File file) throws IOException {
        final URL url = new URL(urlStr);
        final ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        file.getParentFile().mkdirs();
        final FileOutputStream fos = new FileOutputStream(file);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
        rbc.close();
    }

    private static File selectFile(final File dir, final String name) {
        String target = name;
        int attempt = 0;
        while (new File(dir, target).exists()) {
            FileUtils.appendBeforeExtension(name, "_" + (++attempt));
        }
        return new File(dir, target);
    }
}
