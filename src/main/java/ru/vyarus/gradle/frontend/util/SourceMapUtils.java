package ru.vyarus.gradle.frontend.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Vyacheslav Rusakov
 * @since 02.03.2023
 */
public class SourceMapUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    public static void includeRemoteSources(final File sourceMap, final String baseUrl) {
        SourceMap map = parse(sourceMap);
        if (map.getSourcesContent() != null && !map.getSourcesContent().isEmpty()) {
            // do nothing - content already included
            return;
        }
        final List<String> content = new ArrayList<>();
        File tmp;
        try {
            tmp = Files.createTempFile("sourceMapSource", "download").toFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Embedding " + map.getSources().size() + " source files into source map "
                + sourceMap.getName() + " (" + FileUtils.byteCountToDisplaySize(sourceMap.length()) + ")");
        final String base = baseUrl + (map.getSourceRoot() == null ? "" : map.getSourceRoot());
        for (String src : map.getSources()) {
            try {
                UrlUtils.download(base + src, tmp);
                content.add(Files.readString(tmp.toPath(), StandardCharsets.UTF_8));
                System.out.println("Source file " + src + " embedded into source map " + sourceMap.getName());
            } catch (Exception e) {
                throw new IllegalStateException("Failed to load source files for source map " + sourceMap.getName());
            }
        }
        tmp.delete();
        map.setSourcesContent(content);
        write(map, sourceMap);
        System.out.println("Source map updated: " + sourceMap.getName() + " ("
                + FileUtils.byteCountToDisplaySize(sourceMap.length()) + ")");
    }

    public static void includeSources(final File sourceMap) {
        SourceMap map = parse(sourceMap);
        if (map.getSourcesContent() != null && !map.getSourcesContent().isEmpty()) {
            // do nothing - content already included
            return;
        }
        final List<String> content = new ArrayList<>();
        final File baseDir = sourceMap.getParentFile();

        System.out.println("Embedding " + map.getSources().size() + " source files into source map "
                + sourceMap.getName() + " (" + FileUtils.byteCountToDisplaySize(sourceMap.length()) + ")");
        // repackage sources to use relative paths (e.g. google-closure puts absolute paths)
        List<String> outSrc = new ArrayList<>();
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
                outSrc.add(ru.vyarus.gradle.frontend.util.FileUtils.relative(sourceMap, source));
                content.add(Files.readString(source.toPath(), StandardCharsets.UTF_8));
                System.out.println("\t " + source.getName() + " ("+ FileUtils.byteCountToDisplaySize(source.length())
                        + ") embedded into source map");
            } catch (Exception e) {
                throw new IllegalStateException("Failed to read source file content for source map "
                        + sourceMap.getName(), e);
            }
        }
        map.setSources(outSrc);
        map.setSourcesContent(content);
        write(map, sourceMap);
        System.out.println("Source map updated: " + sourceMap.getName() + " ("
                + FileUtils.byteCountToDisplaySize(sourceMap.length()) + ")");
    }

    public static SourceMap parse(final File sourceMap) {
        try {
            return MAPPER.readValue(sourceMap, SourceMap.class);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse source mappings file", e);
        }
    }

    public static void write(final SourceMap map, final File file) {
        try {
            MAPPER.writeValue(file, map);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write source map file", e);
        }
    }

    // https://sourcemaps.info/spec.html#h.mofvlxcwqzej
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

        public Integer getVersion() {
            return version;
        }

        public void setVersion(Integer version) {
            this.version = version;
        }

        public String getFile() {
            return file;
        }

        public void setFile(String file) {
            this.file = file;
        }

        public String getSourceRoot() {
            return sourceRoot;
        }

        public void setSourceRoot(String sourceRoot) {
            this.sourceRoot = sourceRoot;
        }

        public List<String> getSources() {
            return sources;
        }

        public void setSources(List<String> sources) {
            this.sources = sources;
        }

        public List<String> getSourcesContent() {
            return sourcesContent;
        }

        public void setSourcesContent(List<String> sourcesContent) {
            this.sourcesContent = sourcesContent;
        }

        public List<String> getNames() {
            return names;
        }

        public void setNames(List<String> names) {
            this.names = names;
        }

        public String getMappings() {
            return mappings;
        }

        public void setMappings(String mappings) {
            this.mappings = mappings;
        }
    }
}
