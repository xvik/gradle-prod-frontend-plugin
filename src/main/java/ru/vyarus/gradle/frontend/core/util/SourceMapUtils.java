package ru.vyarus.gradle.frontend.core.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Source maps utils.
 *
 * @author Vyacheslav Rusakov
 * @since 02.03.2023
 */
@SuppressWarnings("PMD.SystemPrintln")
public final class SourceMapUtils {

    /**
     * Source map reference in minified file.
     */
    private static final Pattern SOURCE_URL = Pattern.compile("sourceMappingURL=([^ *]+)");

    /**
     * Pure jackson used for source map read/write.
     */
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    private SourceMapUtils() {
    }

    /**
     * Looks for source map reference in file.
     * Assumption: source map reference would be in the last line (ignoring empty lines).
     *
     * @param file file to find source map reference
     * @return source map url or null if not found
     */
    public static String getSourceMapReference(final File file) {
        final String line = ru.vyarus.gradle.frontend.core.util.FileUtils.readLastLine(file);
        return line == null ? null : getSourceMapReference(line);
    }

    /**
     * Looks for source map reference in line.
     *
     * @param line line to search for reference
     * @return source map url or null if not found
     */
    public static String getSourceMapReference(final String line) {
        final Matcher matcher = SOURCE_URL.matcher(line);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * Download sources, declared in source map and add them directly inside map. Do nothing if source map
     * already contain embedded sources.
     *
     * @param sourceMap source map file
     * @param baseUrl   base url for source map (from where source map was downloaded)
     */
    @SuppressFBWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE")
    public static void includeRemoteSources(final File sourceMap, final String baseUrl) {
        final SourceMap map = parse(sourceMap);
        if (map.getSourcesContent() != null && !map.getSourcesContent().isEmpty()) {
            System.out.println("\tSource map " + sourceMap.getName() + " already contain sources");
            // do nothing - content already included
            return;
        }
        final File tmp;
        try {
            tmp = Files.createTempFile("sourceMapSource", "download").toFile();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create tmp file for download", e);
        }

        final String base = baseUrl + (map.getSourceRoot() == null ? "" : map.getSourceRoot());
        final List<String> content = new ArrayList<>();
        for (String src : map.getSources()) {
            try {
                UrlUtils.download(base + src, tmp, "\t");
                content.add(Files.readString(tmp.toPath(), StandardCharsets.UTF_8));
                System.out.println("\t" + src + " (" + FileUtils.byteCountToDisplaySize(tmp.length())
                        + ") embedded into " + sourceMap.getName());
            } catch (Exception e) {
                throw new IllegalStateException("Failed to load source files for source map " + sourceMap.getName(), e);
            }
        }
        tmp.delete();
        map.setSourcesContent(content);
        write(map, sourceMap);
        System.out.println("\tSource map updated: " + sourceMap.getName() + " ("
                + FileUtils.byteCountToDisplaySize(sourceMap.length()) + ")");
    }

    /**
     * Include local sources into source map. Do nothing if sources already embedded.
     *
     * @param sourceMap source map file
     */
    @SuppressWarnings("PMD.ExceptionAsFlowControl")
    public static void includeSources(final File sourceMap) {
        final SourceMap map = parse(sourceMap);
        if (map.getSourcesContent() != null && !map.getSourcesContent().isEmpty()) {
            System.out.println("\tSource map " + sourceMap.getName() + " already contain sources");
            // do nothing - content already included
            return;
        }
        final List<String> content = new ArrayList<>();
        final File baseDir = sourceMap.getParentFile();

        // repackage sources to use relative paths (e.g. google-closure puts absolute paths)
        final List<String> outSrc = new ArrayList<>();
        for (String src : map.getSources()) {
            try {
                // could be absolute path
                File source = new File(src);
                if (!source.exists()) {
                    source = new File(baseDir, src);
                }
                if (!source.exists()) {
                    throw new IllegalStateException("Source file not found: " + source.getAbsolutePath());
                }
                outSrc.add(ru.vyarus.gradle.frontend.core.util.FileUtils.relative(sourceMap, source));
                content.add(Files.readString(source.toPath(), StandardCharsets.UTF_8));
                System.out.println("\t " + source.getName() + " (" + FileUtils.byteCountToDisplaySize(source.length())
                        + ") embedded into " + sourceMap.getName());
            } catch (Exception e) {
                throw new IllegalStateException("Failed to read source file content for source map "
                        + sourceMap.getName(), e);
            }
        }
        map.setSources(outSrc);
        map.setSourcesContent(content);
        write(map, sourceMap);
        System.out.println("\tSource map updated: " + sourceMap.getName() + " ("
                + FileUtils.byteCountToDisplaySize(sourceMap.length()) + ")");
    }

    /**
     * Parse source map file.
     *
     * @param sourceMap source map file
     * @return parsed source map object
     */
    public static SourceMap parse(final File sourceMap) {
        try {
            return MAPPER.readValue(sourceMap, SourceMap.class);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse source mappings file", e);
        }
    }

    /**
     * Write source map into file (overriding existing).
     *
     * @param map  source map object
     * @param file target file
     */
    public static void write(final SourceMap map, final File file) {
        try {
            MAPPER.writeValue(file, map);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write source map file", e);
        }
    }

    /**
     * Source map object.
     *
     * @see <a href="https://sourcemaps.info/spec.html#h.mofvlxcwqzej">format</a>
     */
    public static class SourceMap {
        /**
         * File version (always the first entry in the object) and must be a positive integer.
         */
        private Integer version;
        /**
         * An optional name of the generated code that this source map is associated with.
         */
        private String file;
        /**
         * An optional source root, useful for relocating source files on a server or removing repeated values in
         * the “sources” entry.  This value is prepended to the individual entries in the “source” field.
         */
        private String sourceRoot;
        /**
         * A list of original sources used by the “mappings” entry.
         */
        private List<String> sources;
        /**
         * An optional list of source content, useful when the “source” can’t be hosted. The contents are listed in
         * the same order as the sources in line 5. “null” may be used if some original sources should be retrieved by
         * name.
         */
        private List<String> sourcesContent;
        /**
         * A list of symbol names used by the “mappings” entry.
         */
        private List<String> names;
        /**
         * A string with the encoded mapping data.
         */
        private String mappings;

        /**
         * @return format version
         */
        public Integer getVersion() {
            return version;
        }

        public void setVersion(final Integer version) {
            this.version = version;
        }

        /**
         * @return name of the generated code that this source map is associated with or null
         */
        public String getFile() {
            return file;
        }

        public void setFile(final String file) {
            this.file = file;
        }

        /**
         * @return sources root directory or null
         */
        public String getSourceRoot() {
            return sourceRoot;
        }

        public void setSourceRoot(final String sourceRoot) {
            this.sourceRoot = sourceRoot;
        }

        /**
         * @return list of original sources or null
         */
        public List<String> getSources() {
            return sources;
        }

        public void setSources(final List<String> sources) {
            this.sources = sources;
        }

        /**
         * @return list of embedded sources (content) or null
         */
        public List<String> getSourcesContent() {
            return sourcesContent;
        }

        public void setSourcesContent(final List<String> sourcesContent) {
            this.sourcesContent = sourcesContent;
        }

        /**
         * @return list of symbol names used by the “mappings” entry
         */
        public List<String> getNames() {
            return names;
        }

        public void setNames(final List<String> names) {
            this.names = names;
        }

        /**
         * @return string with the encoded mapping data
         */
        public String getMappings() {
            return mappings;
        }

        public void setMappings(final String mappings) {
            this.mappings = mappings;
        }
    }
}
