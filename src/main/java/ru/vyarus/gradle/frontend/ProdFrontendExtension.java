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
@SuppressWarnings({"checkstyle:AvoidFieldNameMatchingMethodName", "PMD.AvoidFieldNameMatchingMethodName"})
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
    }
}
