package ru.vyarus.gradle.frontend.util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;

/**
 * @author Vyacheslav Rusakov
 * @since 04.02.2023
 */
public final class UrlUtils {

    private UrlUtils() {
    }

    public static String getBaseUrl(final String url) {
        int idx = url.lastIndexOf('/');
        if (idx == 0) {
            idx = url.lastIndexOf('\\');
        }
        if (idx > 0) {
            return url.substring(0, idx + 1);
        } else {
            throw new IllegalStateException("Failed to detect base in url: " + url);
        }
    }

    public static int getNameSeparatorPos(final String url) {
        int idx = url.lastIndexOf('/');
        if (idx == 0) {
            idx = url.lastIndexOf('\\');
        }
        return idx;
    }

    public static String getFileName(final String url) {
        int idx = getNameSeparatorPos(url);
        String res = url;
        if (idx > 0) {
            res = res.substring(idx + 1);
        }
        // cut off possible redundant parts (maybe default anti-cache)
        for (char sep : Arrays.asList('?', '#')) {
            int i = res.indexOf(sep);
            if (i > 0) {
                res = res.substring(0, i);
            }
        }
        return res;
    }

    public static File smartDownload(final String url, final File target) throws IOException {
        File res = ru.vyarus.gradle.frontend.util.FileUtils
                .selectNotExistingFile(target.getParentFile(), target.getName());
        download(url, res);
        if (!res.getName().equals(target.getName())) {
            if (res.length() == target.length() &&
                    ru.vyarus.gradle.frontend.util.FileUtils.computeMd5(res)
                            .equals(ru.vyarus.gradle.frontend.util.FileUtils.computeMd5(target))) {
                System.out.println("Downloaded file is the same as already existing file, using existing file");
                // same as existing file, remove downloaded
                res.delete();
                res = target;
            }
        }
        return res;
    }

    public static void download(final String urlStr, final File file) throws IOException {
        System.out.print("Download " + urlStr);
        try {
            long time = System.currentTimeMillis();
            final URL url = new URL(urlStr);
            final URLConnection connection = url.openConnection();
            connection.setConnectTimeout(1000);
            final ReadableByteChannel rbc = Channels.newChannel(connection.getInputStream());
            file.getParentFile().mkdirs();
            final FileOutputStream fos = new FileOutputStream(file);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fos.close();
            rbc.close();
            System.out.println(", took " + DurationFormatter.format(System.currentTimeMillis() - time) + " ("
                    + FileUtils.byteCountToDisplaySize(file.length()) + ")");
        } catch (Exception ex) {
            System.out.println(", FAILED");
            throw ex;
        }
    }
}
