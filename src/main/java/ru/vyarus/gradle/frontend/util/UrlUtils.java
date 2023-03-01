package ru.vyarus.gradle.frontend.util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Vyacheslav Rusakov
 * @since 04.02.2023
 */
public final class UrlUtils {

    private static final Pattern URL_BASE = Pattern.compile("https?://[^/:]+(:\\d+)?");
    private static List<Integer> REDIRECT_STATUS = Arrays.asList(301, 302, 303, 307, 308);

    private UrlUtils() {
    }

    // for example, unpkg supports urls like https://unpkg.com/vue@2 leading to actual file
    // https://unpkg.com/vue@2.7.14/dist/vue.js - it is important to follow redirect first to know exact base dir
    public static String checkRedirect(final String url) {

        try {
            String res = url;
            // remove ../ parts in url
            final URL target = new URI(url).normalize().toURL();
            final HttpURLConnection conn = (HttpURLConnection) target.openConnection();
            conn.setInstanceFollowRedirects(false);
            conn.setReadTimeout(1000);
            conn.addRequestProperty("User-Agent", "Mozilla");

            final boolean redirect = REDIRECT_STATUS.contains(conn.getResponseCode());
            conn.getInputStream().close();
            conn.disconnect();

            if (redirect) {
                res = conn.getHeaderField("Location");
                if (!res.startsWith("http")) {
                    res = getServerRoot(url) + res;
                }
                System.out.println("Redirect resolved: " + target + " --> " + res);
                // might be multiple redirects
                res = checkRedirect(res);
            }

            return res;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to check redirect on url: " + url, e);
        }
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

    public static String getServerRoot(final String url) {
        final Matcher matcher = URL_BASE.matcher(url);
        return matcher.find() ? matcher.group(0) : null;
    }

    public static int getNameSeparatorPos(final String url) {
        int idx = url.lastIndexOf('/');
        if (idx == 0) {
            idx = url.lastIndexOf('\\');
        }
        return idx;
    }

    public static boolean hasExtension(final String url) {
        String name = getFileName(url);
        int idx = name.lastIndexOf('.');
        return name.length() - idx <= 4;
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

    public static File smartDownload(final String url, final File target) throws Exception {
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

    public static void download(final String urlStr, final File file) throws Exception {
        System.out.print("Download ");
        try {
            long time = System.currentTimeMillis();
            // remove ../ parts in url
            final URL url = new URI(urlStr).normalize().toURL();
            System.out.print(url);
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(1000);
            connection.addRequestProperty("User-Agent", "Mozilla");
            final ReadableByteChannel rbc = Channels.newChannel(connection.getInputStream());
            file.getParentFile().mkdirs();
            final FileOutputStream fos = new FileOutputStream(file);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fos.close();
            rbc.close();
            connection.disconnect();
            System.out.println(", took " + DurationFormatter.format(System.currentTimeMillis() - time) + " ("
                    + FileUtils.byteCountToDisplaySize(file.length()) + ")");
        } catch (Exception ex) {
            System.out.println(", FAILED");
            throw ex;
        }
    }
}
