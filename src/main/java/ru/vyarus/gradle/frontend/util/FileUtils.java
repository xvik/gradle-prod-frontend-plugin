package ru.vyarus.gradle.frontend.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.zip.GZIPOutputStream;

/**
 * @author Vyacheslav Rusakov
 * @since 30.01.2023
 */
public final class FileUtils {

    private FileUtils() {
    }

    public static String getFileName(final String url) {
        int idx = url.lastIndexOf('/');
        if (idx == 0) {
            idx = url.lastIndexOf('\\');
        }
        if (idx > 0) {
            return url.substring(idx + 1);
        }
        // better then nothing
        return url.replaceAll("[\\/]", "_");
    }

    public static void download(String urlStr, File file) throws IOException {
        final URL url = new URL(urlStr);
        final ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        file.getParentFile().mkdirs();
        final FileOutputStream fos = new FileOutputStream(file);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
        rbc.close();
    }

    public static void writeFile(final File target, final String content) {
        try {
            org.apache.commons.io.FileUtils.writeStringToFile(target, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write into file: " + target.getAbsolutePath(), e);
        }
    }

    public static String computeMd5(File file) {
        byte[] data;
        try {
            data = Files.readAllBytes(file.toPath());
            byte[] hash = MessageDigest.getInstance("MD5").digest(data);
            return new BigInteger(1, hash).toString(16);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to calculate MD5 for file " + file.getAbsolutePath(), e);
        }
    }

    public static File gzip(final File source) {
        final File target = new File(source.getAbsolutePath() + ".gz");
        try (CustomGzipStream gos = new CustomGzipStream(
                new FileOutputStream(target))) {
            Files.copy(source.toPath(), gos);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to gzip file " + source.getAbsolutePath(), ex);
        }
        return target;
    }

    private static class CustomGzipStream extends GZIPOutputStream {

        public CustomGzipStream(OutputStream out) throws IOException {
            super(out);
            def.setLevel(9);
        }


    }
}
