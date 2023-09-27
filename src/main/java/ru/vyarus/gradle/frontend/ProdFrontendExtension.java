package ru.vyarus.gradle.frontend;

import org.gradle.api.Action;
import org.gradle.api.tasks.Nested;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Production frontend plugin extension.
 *
 * @author Vyacheslav Rusakov
 * @since 28.01.2023
 */
@SuppressWarnings({"checkstyle:AvoidFieldNameMatchingMethodName", "PMD.AvoidFieldNameMatchingMethodName",
"PMD.ExcessivePublicCount"})
public class ProdFrontendExtension {

    /**
     * Print plugin debug information (extended logs).
     */
    private boolean debug;

    /**
     * Directory where html files must be found and processed.
     */
    private String sourceDir = "build/webapp";

    /**
     * Directory name for loaded js files (inside source dir).
     */
    private String jsDir = "js";

    /**
     * Directory name for loaded css files (inside source dir).
     */
    private String cssDir = "css";

    /**
     * File extensions to recognize as html files.
     */
    private List<String> htmlExtensions = new ArrayList<>(Arrays.asList("html", "htm"));

    /**
     * Ignore processing for local resources (globs). Might also affect downloaded resource to prevent further
     * processing.
     * <p>
     * Examples:
     * <ul>
     *     <li>*.html - all html files in root dir</li>
     *     <li>*.{html,htm,js} - all html, htm and js files in root dir</li>
     *     <li>**.html - all html files</li>
     *     <li>*\/*.html - all html files in first level directories</li>
     *     <li>**\/*.html - all html files in any sub directory</li>
     *     <li>index.??? - all files in root directory with name index and 3-chars extension</li>
     * </ul>
     * <p>
     * Rules:
     * <ul>
     *     <li>* It matches zero , one or more than one characters. While matching, it will not cross directories
     *     boundaries.</li>
     *     <li>** It does the same as * but it crosses the directory boundaries.</li>
     *     <li>? It matches only one character for the given name.</li>
     *     <li>\ It helps to avoid characters to be interpreted as special characters.</li>
     *     <li>[] In a set of characters, only single character is matched. If (-) hyphen is used then, it matches a
     *     range of characters. Example: [efg] matches "e","f" or "g" . [a-d] matches a range from a to d.</li>
     *     <li>{} It helps to matches the group of sub patterns.</li>
     * </ul>
     *
     * @see <a href=
     * "https://docs.oracle.com/javase/8/docs/api/java/nio/file/FileSystem.html#getPathMatcher-java.lang.String-">
     * docs (for "glob:" prefix)</a>
     */
    private final List<String> ignore = new ArrayList<>();

    private final Download download = new Download();

    private final Minify minify = new Minify();

    /**
     * Apply MD5 hash into js and css urls (including inner css urls).
     */
    private boolean applyAntiCache = true;

    /**
     * Add integrity attributes to resource tags so browser could verify loaded resource for unwanted changes.
     */
    private boolean applyIntegrity = true;

    /**
     * Create ".gz" versions for all resources.
     */
    private boolean gzip = true;

    /**
     * @return true to show extra logs
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * @param debug true to enable debug mode with extra logs
     */
    public void setDebug(final boolean debug) {
        this.debug = debug;
    }

    /**
     * @return directory where html files must be found and processed
     */
    public String getSourceDir() {
        return sourceDir;
    }

    /**
     * @param sourceDir directory where to search and process html files
     */
    public void setSourceDir(final String sourceDir) {
        this.sourceDir = sourceDir;
    }

    /**
     * @return directory name for downloaded js files
     */
    public String getJsDir() {
        return jsDir;
    }

    /**
     * @param jsDir directory name for downloaded js files (relative to source root)
     */
    public void setJsDir(final String jsDir) {
        this.jsDir = jsDir;
    }

    /**
     * @return directory name for downloaded css files
     */
    public String getCssDir() {
        return cssDir;
    }

    /**
     * @param cssDir directory name for downloaded css files (relative to source root)
     */
    public void setCssDir(final String cssDir) {
        this.cssDir = cssDir;
    }

    /**
     * @return extensions of files to process (recognized as html)
     */
    public List<String> getHtmlExtensions() {
        return htmlExtensions;
    }

    /**
     * @param htmlExtensions extensions of files to process (as html files)
     */
    public void setHtmlExtensions(final List<String> htmlExtensions) {
        this.htmlExtensions = htmlExtensions;
    }

    /**
     * @return globs for ignored files (html and linked resources)
     */
    public List<String> getIgnore() {
        return ignore;
    }

    /**
     * @param ignore globs to ignore processing files (html and resources)
     */
    public void setIgnore(final List<String> ignore) {
        overwriteList(this.ignore, ignore, "local files ignore configuration overridden");
    }

    /**
     * Might be called multiple times.
     *
     * @param globs file globs to ignore processing files (html and resources)
     */
    public void ignore(final String... globs) {
        ignore.addAll(Arrays.asList(globs));
    }

    @Nested
    public Download getDownload() {
        return download;
    }

    public void download(final Action<Download> action) {
        action.execute(getDownload());
    }

    @Nested
    public Minify getMinify() {
        return minify;
    }

    public void minify(final Action<Minify> action) {
        action.execute(getMinify());
    }

    /**
     * @return true to create .gz files for all resources
     */
    public boolean isGzip() {
        return gzip;
    }

    /**
     * @param gzip true to create .gz files for all resources
     */
    public void setGzip(final boolean gzip) {
        this.gzip = gzip;
    }

    /**
     * @return true to apply MD5 hashes into all file urls (inside html and css)
     */
    public boolean isApplyAntiCache() {
        return applyAntiCache;
    }

    /**
     * @param applyAntiCache true to apply MD5 hashes into all file urls (inside html and css)
     */
    public void setApplyAntiCache(final boolean applyAntiCache) {
        this.applyAntiCache = applyAntiCache;
    }

    /**
     * @return true to apply integrity attributes to resource tags so browser could verify loaded resource for
     * unwanted changes.
     */
    public boolean isApplyIntegrity() {
        return applyIntegrity;
    }

    /**
     * @param applyIntegrity true to apply integrity attributes to resource tags so browser could verify loaded
     *                       resource for unwanted changes.
     */
    public void setApplyIntegrity(final boolean applyIntegrity) {
        this.applyIntegrity = applyIntegrity;
    }

    @SuppressWarnings("PMD.SystemPrintln")
    private static void overwriteList(final List<String> source, final List<String> target, final String err) {
        if (!source.isEmpty()) {
            System.out.println("WARNING: " + err + ": \n\tfrom: "
                    + String.join(",", source) + " \n\tto: "
                    + String.join(",", target));
        }
        source.clear();
        source.addAll(target);
    }

    /**
     * Remote resources download related options.
     */
    public static class Download {

        /**
         * Download js and css (e.g. declared as cdn links).
         */
        private boolean enabled = true;

        /**
         * Try to download ".min" resource version first (applicable for cdn links).
         */
        private boolean preferMin = true;

        /**
         * Load source maps (and source content) for minified versions.
         */
        private boolean sourceMaps = true;

        /**
         * Remote URL regexps to prevent downloading of certain files. Use in cases when some remote links must
         * be preserved.
         */
        private final List<String> ignore = new ArrayList<>();

        /**
         * @return true to download remote js and css links (e.g. cdn links)
         */
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * @param enabled true to download remote js and css links
         */
        public void setEnabled(final boolean enabled) {
            this.enabled = enabled;
        }

        /**
         * @return true to try to download ".min" resource version first (for cdn)
         */
        public boolean isPreferMin() {
            return preferMin;
        }

        /**
         * @param preferMin true to try to download ".min" resource version first (for cdn)
         */
        public void setPreferMin(final boolean preferMin) {
            this.preferMin = preferMin;
        }

        /**
         * @return true to download source maps (and sources) for minified version
         */
        public boolean isSourceMaps() {
            return sourceMaps;
        }

        /**
         * @param sourceMaps true to try to download source maps (and sources) for minified version
         */
        public void setSourceMaps(final boolean sourceMaps) {
            this.sourceMaps = sourceMaps;
        }

        /**
         * @return remote URL regexps for ignored links
         */
        public List<String> getIgnore() {
            return ignore;
        }

        /**
         * @param ignore URL regexps to prevent downloading remote resources
         */
        public void setIgnore(final List<String> ignore) {
            overwriteList(this.ignore, ignore, "download ignore configuration overridden");
        }

        /**
         * Might be called multiple times. Applied for all downloaded resources: root links and css sub-links.
         * Case-insensitive match.
         *
         * @param regex URL regexps to ignore downloading
         */
        public void ignore(final String... regex) {
            ignore.addAll(Arrays.asList(regex));
        }
    }

    /**
     * Resources minification related options.
     */
    public static class Minify {

        /**
         * Minify html files.
         */
        private boolean html = true;

        /**
         * Minify raw js inside html (only if html minification active).
         */
        private boolean htmlJs = true;

        /**
         * Minify raw css inside html (only if html minification active).
         */
        private boolean htmlCss = true;

        /**
         * Minify js (not marked as ".min").
         */
        private boolean js = true;

        /**
         * Minify css (not marked as ".min").
         */
        private boolean css = true;

        /**
         * Generate source maps for minified resources.
         */
        private boolean generateSourceMaps = true;

        /**
         * Ignore minification for resources (globs).
         */
        private final List<String> ignore = new ArrayList<>();

        /**
         * @return true to minify html
         */
        public boolean isHtml() {
            return html;
        }

        /**
         * @param html true to minify html
         */
        public void setHtml(final boolean html) {
            this.html = html;
        }

        /**
         * @return true to minify raw js inside html
         */
        public boolean isHtmlJs() {
            return htmlJs;
        }

        /**
         * @param htmlJs true to minify raw js inside html
         */
        public void setHtmlJs(final boolean htmlJs) {
            this.htmlJs = htmlJs;
        }

        /**
         * @return true to minify raw css inside html
         */
        public boolean isHtmlCss() {
            return htmlCss;
        }

        /**
         * @param htmlCss true to minify raw css inside html
         */
        public void setHtmlCss(final boolean htmlCss) {
            this.htmlCss = htmlCss;
        }

        /**
         * @return true to minify js (not marked as ".min")
         */
        public boolean isJs() {
            return js;
        }

        /**
         * @param js true to minify js (not marked as ".min")
         */
        public void setJs(final boolean js) {
            this.js = js;
        }

        /**
         * @return true to minify css (not marked as ".min")
         */
        public boolean isCss() {
            return css;
        }

        /**
         * @param css true to minify js (not marked as ".min")
         */
        public void setCss(final boolean css) {
            this.css = css;
        }

        /**
         * @return true to generate source maps for minified resources
         */
        public boolean isGenerateSourceMaps() {
            return generateSourceMaps;
        }

        /**
         * @param generateSourceMaps true to generate source maps for minified resources
         */
        public void setGenerateSourceMaps(final boolean generateSourceMaps) {
            this.generateSourceMaps = generateSourceMaps;
        }

        /**
         * @return globs for ignored files (html and linked resources)
         */
        public List<String> getIgnore() {
            return ignore;
        }

        /**
         * @param ignore globs to ignore processing files (html and resources)
         */
        public void setIgnore(final List<String> ignore) {
            overwriteList(this.ignore, ignore, "local files minification ignore configuration overridden");
        }

        /**
         * Might be called multiple times.
         *
         * @param globs file globs to ignore minifying files (html and resources)
         */
        public void ignore(final String... globs) {
            ignore.addAll(Arrays.asList(globs));
        }
    }
}
