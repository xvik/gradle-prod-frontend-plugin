package ru.vyarus.gradle.frontend.core.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.io.input.ReversedLinesFileReader;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.security.MessageDigest;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

/**
 * File utilities.
 *
 * @author Vyacheslav Rusakov
 * @since 30.01.2023
 */
@SuppressWarnings({"PMD.SystemPrintln", "PMD.GodClass"})
public final class FileUtils {

    private FileUtils() {
    }

    /**
     * Searches for html files in provided directory (and subdirectories). Html files recognized with provided list
     * of extensions (required to support not pure html templates).
     *
     * @param baseDir    base directory to search files in
     * @param extensions html files extensions
     * @return list of recognized files
     */
    public static List<File> findHtmls(final File baseDir, final List<String> extensions) {
        try (Stream<Path> walk = Files.walk(baseDir.toPath(), 100)) {
            return walk.filter(path -> {
                final File fl = path.toFile();
                if (fl.isDirectory()) {
                    return false;
                }
                final String name = fl.getName().toLowerCase();
                final int dot = name.lastIndexOf('.');
                return dot > 0 && extensions.contains(name.substring(dot + 1).toLowerCase());
            }).map(Path::toFile).collect(Collectors.toList());
        } catch (IOException ex) {
            throw new IllegalStateException("Error searching for html files in " + baseDir.getAbsolutePath(), ex);
        }
    }

    /**
     * Searches for not existing file name. If file exists, appends ".N" before extension (e.g. vue.js become vue.1.js).
     *
     * @param dir  file directory
     * @param name file name
     * @return file name not existing in directory
     */
    public static File selectNotExistingFile(final File dir, final String name) {
        String target = name;
        int attempt = 0;
        while (new File(dir, target).exists()) {
            // use this scheme to preserve .min. in file name (used for minified file detection)
            final String append = "." + (++attempt);
            target = FileUtils.appendBeforeExtension(name, append);
        }
        return new File(dir, target);
    }

    /**
     * Common convention to have ".min" before file extension (indicates minified file).
     *
     * @param name file name
     * @return file name with ".min" postfix in name (could be passed name as is)
     */
    public static String getMinName(final String name) {
        if (name.contains(".min.")) {
            return name;
        }
        return appendBeforeExtension(name, ".min");
    }

    /**
     * @param name   fila name
     * @param append postfix to append before extension (after file name)
     * @return file name with appended paer (before extension)
     */
    public static String appendBeforeExtension(final String name, final String append) {
        final int dotIdx = name.lastIndexOf('.');
        if (dotIdx <= 0) {
            throw new IllegalStateException("Can't find extension in file name: " + name);
        }
        return name.substring(0, dotIdx) + append + name.substring(dotIdx);
    }

    /**
     * Computes relative path for file, relative to some other file (base dir). Used to compute local paths for
     * downloaded files, relative to html page.
     * <p>
     * Converts windows line separators into unix backslashes (for universal output).
     *
     * @param from base file to compute path relative to its directory
     * @param file file to build relative path for
     * @return relative path from directory containing base file
     */
    public static String relative(final File from, final File file) {
        final String path = (from.isDirectory() ? from : from.getParentFile())
                .toPath().relativize(file.toPath()).toString();
        // convert windows separators into unix style backslashes to unify outputs
        return path.replace('\\', '/');
    }

    /**
     * Method overwrites file line separators with system separator. It may seem weird, but it is important
     * because js or css declaration could be multi-line and without such unification replacement could be
     * impossible (it is possible to properly extract resource declaration with exact line separators, but
     * simpler to just unify; see how declarations extracted in {@link HtmlParser#parse(File)}).
     *
     * @param file file to read
     * @return file content as string
     */
    public static String readFile(final File file) {
        try (Stream<String> lines = Files.lines(file.toPath(), StandardCharsets.UTF_8)) {
            return lines.collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read file content", e);
        }
    }

    /**
     * Write content to file. Creates file if it does not exist. Overwrite existing file content.
     *
     * @param target  target file
     * @param content content to write to file
     */
    public static void writeFile(final File target, final String content) {
        try {
            org.apache.commons.io.FileUtils.writeStringToFile(target, content, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write into file: " + target.getAbsolutePath(), e);
        }
    }

    /**
     * @param file file
     * @return md5 of file content
     */
    public static String computeMd5(final File file) {
        final byte[] data;
        try {
            data = Files.readAllBytes(file.toPath());
            final byte[] hash = MessageDigest.getInstance("MD5").digest(data);
            return new BigInteger(1, hash).toString(16);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to calculate MD5 for file " + file.getAbsolutePath(), e);
        }
    }

    /**
     * Remove duplicate file if files are the same (MD5). Used to avoid duplicates after downloading file
     * (appeared, usually, after executions on already processed folder). If duplicate detected, downloaded file
     * is removed.
     *
     * @param file     downloaded file to check for duplicates
     * @param existing existing file
     * @param prefix   console output prefix
     * @return true if duplicate file removed, false otherwise
     */
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
    public static boolean removeDuplicate(final File file, final File existing, final String prefix) {
        if (!file.getName().equals(existing.getName())
                && file.length() == existing.length() && computeMd5(file).equals(computeMd5(existing))) {
            System.out.println(prefix + "Duplicate file '" + file.getName() + "' removed in favour of existing '"
                    + existing.getName() + "'");
            file.delete();
            return true;
        }
        return false;
    }

    /**
     * Create gzip file for specified source: "sourcefilename.gz'. Avoids gzip generation if gzip file already exists
     * (and not older than source file).
     *
     * @param source  source file for gzip
     * @param baseDir "context" base directory to show file paths in logs relative to its location
     * @return gzip file (could be already existing)
     */
    public static File gzip(final File source, final File baseDir) {
        final File target = new File(source.getAbsolutePath() + ".gz");
        if (target.exists() && target.lastModified() >= source.lastModified()) {
            // avoid redundant re-generation
            return target;
        }
        if (baseDir != null) {
            System.out.print("Gzip " + relative(baseDir, source));
        }
        try {
            final File gzip = gzip(source);
            if (baseDir != null) {
                System.out.println(", " + SizeFormatter.formatChangePercent(source.length(), gzip.length()));
            }
            return gzip;
        } catch (RuntimeException ex) {
            if (baseDir != null) {
                // newline
                System.out.println();
            }
            throw ex;
        }
    }

    /**
     * Generate gzip file for provided (without check for already existing file).
     *
     * @param source file to create gzip for,
     * @return gzip file
     */
    public static File gzip(final File source) {
        final File target = new File(source.getAbsolutePath() + ".gz");
        if (target.exists() && target.lastModified() > source.lastModified()) {
            // avoid redundant  re-generation
            return target;
        }
        try (CustomGzipStream gos = new CustomGzipStream(Files.newOutputStream(target.toPath()))) {
            Files.copy(source.toPath(), gos);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to gzip file " + source.getAbsolutePath(), ex);
        }
        return target;
    }

    /**
     * @param file file
     * @return last non-empty file line
     */
    public static String readLastLine(final File file) {
        try (ReversedLinesFileReader reader = ReversedLinesFileReader.builder()
                .setCharset(StandardCharsets.UTF_8)
                .setFile(file).get()) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    return line;
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read last file line " + file.getAbsolutePath(), e);
        }
        return null;
    }

    /**
     * Validates if file matches glob patters.
     *
     * @param file   file to validate
     * @param base   base directory
     * @param ignore ignore globs
     * @return true if file must be ignored, false otherwise
     */
    public static boolean isIgnored(final File file, final File base, final List<PathMatcher> ignore) {
        if (!ignore.isEmpty()) {
            // using path, relative to base dir
            final Path path = base.toPath().relativize(file.toPath());
            for (PathMatcher matcher : ignore) {
                if (matcher.matches(path)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gzip output stream with max gzip compression level.
     */
    private static class CustomGzipStream extends GZIPOutputStream {

        CustomGzipStream(final OutputStream out) throws IOException {
            super(out);
            def.setLevel(9);
        }
    }
}
