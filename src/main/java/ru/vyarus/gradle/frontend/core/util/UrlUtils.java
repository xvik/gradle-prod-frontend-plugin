package ru.vyarus.gradle.frontend.core.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
 * Url analysis and download methods.
 *
 * @author Vyacheslav Rusakov
 * @since 04.02.2023
 */
@SuppressWarnings("PMD.SystemPrintln")
public final class UrlUtils {

    private static final Pattern URL_BASE = Pattern.compile("https?://[^/:]+(:\\d+)?");
    private static final List<Integer> REDIRECT_STATUS = Arrays.asList(301, 302, 303, 307, 308);

    private UrlUtils() {
    }

    /**
     * Follows url redirects in order to know the actual url. This is important for general resource urls without
     * file name (e.g. unpkg supports urls like "https://unpkg.com/vue@2" leading to actual file
     * "https://unpkg.com/vue@2.7.14/dist/vue.js").
     *
     * @param url url to check redirects on
     * @return url after the last redirect or original url if no redirects required
     */
    public static String followRedirects(final String url) {
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
                res = followRedirects(res);
            }

            return res;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to check redirect on url: " + url, e);
        }
    }

    /**
     * Gets base url fro file url. E.g base url for "http://some.com/files/file.txt" would be
     * "http://some.com/files/".
     *
     * @param url url to detect base on
     * @return base url
     * @throws java.lang.IllegalStateException if base url can't be detected
     */
    public static String getBaseUrl(final String url) throws IllegalStateException {
        final int idx = getNameSeparatorPos(url);
        if (idx > 0) {
            return url.substring(0, idx + 1);
        } else {
            throw new IllegalStateException("Failed to detect base in url: " + url);
        }
    }

    /**
     * Resolve root server url from url (host + port part).
     *
     * @param url url to resolve server url on
     * @return root server url
     * @throws java.lang.IllegalStateException if root url can't be detected
     */
    public static String getServerRoot(final String url) throws IllegalStateException {
        final Matcher matcher = URL_BASE.matcher(url);
        if (matcher.find()) {
            return matcher.group(0);
        }
        throw new IllegalStateException("Failed to detect server root in url: " + url);
    }

    /**
     * Search for the separator before file name (assuming file url used).
     *
     * @param url url to find filename start
     * @return &gt; 0 if separator found, &lt; 0 if not found
     */
    public static int getNameSeparatorPos(final String url) {
        int idx = url.lastIndexOf('/');
        if (idx <= 0) {
            idx = url.lastIndexOf('\\');
        }
        return idx;
    }

    /**
     * Checks if file url contains extension (must end to ".something"). Extension length can't be longer then 4.
     *
     * @param url url to check file extension in
     * @return true if file extension present, false otherwise
     */
    public static boolean hasExtension(final String url) {
        final String name = getFileName(clearParams(url));
        final int idx = name.lastIndexOf('.');
        return name.length() - idx <= 4;
    }

    /**
     * Extracts file name from url.
     *
     * @param url url to extract file name
     * @return file name
     */
    public static String getFileName(final String url) {
        final String res = clearParams(url);
        final int idx = getNameSeparatorPos(res);
        if (idx >= 0) {
            return res.substring(idx + 1);
        } else {
            // not url, direct file name
            return url;
        }
    }

    /**
     * Remove '?' and '#' parts from url.
     *
     * @param url url to purify
     * @return url without parameters part
     */
    public static String clearParams(final String url) {
        String res = url;
        // cut off possible redundant parts (maybe default anti-cache)
        for (char sep : Arrays.asList('?', '#')) {
            final int i = res.indexOf(sep);
            if (i > 0) {
                res = res.substring(0, i);
            }
        }
        return res;
    }

    /**
     * Download file, renaming if file already exists. After download compares with existing file (MD5) and removes
     * duplicate.
     *
     * @param url    file url
     * @param target local file to download into
     * @return downloaded (local) file
     * @throws Exception on load error
     */
    public static File smartDownload(final String url, final File target) throws Exception {
        File res = ru.vyarus.gradle.frontend.core.util.FileUtils
                .selectNotExistingFile(target.getParentFile(), target.getName());
        download(url, res);
        if (!res.getName().equals(target.getName())) {
            if (ru.vyarus.gradle.frontend.core.util.FileUtils.removeDuplicate(res, target, "\t")) {
                res = target;
            } else {
                // duplicate not detected
                System.out.println("\tDownloaded file stored as " + res.getName() + " because " + target.getName()
                        + " already exists with different content");
            }
        }
        return res;
    }

    /**
     * Download url into local file.
     * Shortcut for {@link #download(String, java.io.File, String)}.
     *
     * @param url  file url
     * @param file local file to store in
     * @throws Exception on download error
     */
    public static void download(final String url, final File file) throws Exception {
        download(url, file, "");
    }

    /**
     * Download url into local file. Overwrites already existing file.
     *
     * @param urlStr    url file url
     * @param file      local file to store in
     * @param logPrefix prefix for all messages (used to show "in context" of something)
     * @throws Exception on download error
     */
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
    @SuppressWarnings({"checkstyle:VariableDeclarationUsageDistance", "PMD.AvoidFileStream"})
    public static void download(final String urlStr, final File file, final String logPrefix) throws Exception {
        System.out.print(logPrefix + "Download ");
        try {
            final long time = System.currentTimeMillis();
            // remove ../ parts in url
            final URL url = new URI(urlStr).normalize().toURL();
            System.out.print(url);
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(3000);
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
