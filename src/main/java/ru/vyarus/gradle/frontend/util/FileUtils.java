package ru.vyarus.gradle.frontend.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

/**
 * @author Vyacheslav Rusakov
 * @since 30.01.2023
 */
public final class FileUtils {

    private FileUtils() {
    }

    public static List<File> findHtmls(final File baseDir) {
        try {
            return Files.walk(baseDir.toPath(), 100).filter(path -> {
                File fl = path.toFile();
                if (fl.isDirectory()) {
                    return false;
                }
                final String name = fl.getName().toLowerCase();
                return name.endsWith(".html") || name.endsWith(".htm");
            }).map(Path::toFile).collect(Collectors.toList());
        } catch (IOException ex) {
            throw new IllegalStateException("Error searching for html files in " + baseDir.getAbsolutePath(), ex);
        }
    }

    public static String getMinName(final String name) {
        if (name.contains(".min.")) {
            return name;
        }
        return appendBeforeExtension(name, ".min");
    }

    public static String appendBeforeExtension(final String name, final String append) {
        final int dotIdx = name.lastIndexOf('.');
        if (dotIdx <= 0) {
            throw new IllegalStateException("Can't find extension in file name: " + name);
        }
        return name.substring(0, dotIdx) + append + name.substring(dotIdx);
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
