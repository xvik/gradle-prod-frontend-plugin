package ru.vyarus.gradle.frontend;

import org.gradle.api.Action;
import org.gradle.api.tasks.Nested;

/**
 * prod-frontend plugin extension.
 *
 * @author Vyacheslav Rusakov
 * @since 28.01.2023
 */
public class ProdFrontendExtension {

    /**
     * Print plugin debug information.
     */
    private boolean debug;

    private String sourceDir = "build/webapp";

    // required for loading urls
    private String jsDir = "js";
    private String cssDir = "css";

    private Download download = new Download();

    private Minify minify = new Minify();

    /**
     * Ani-cache (apply md5 instead of name).
     */
    private boolean applyAntiCache = true;

    private boolean applyIntegrity = true;

    /**
     * Create .gz versions for resources
     */
    private boolean gzip = true;

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public String getSourceDir() {
        return sourceDir;
    }

    public void setSourceDir(String sourceDir) {
        this.sourceDir = sourceDir;
    }

    public String getJsDir() {
        return jsDir;
    }

    public void setJsDir(String jsDir) {
        this.jsDir = jsDir;
    }

    public String getCssDir() {
        return cssDir;
    }

    public void setCssDir(String cssDir) {
        this.cssDir = cssDir;
    }

    @Nested
    public Download getDownload() {
        return download;
    }

    public void download(Action<Download> action) {
        action.execute(getDownload());
    }

    @Nested
    public Minify getMinify() {
        return minify;
    }

    public void minify(Action<Minify> action) {
        action.execute(getMinify());
    }

    public boolean isGzip() {
        return gzip;
    }

    public void setGzip(boolean gzip) {
        this.gzip = gzip;
    }

    public boolean isApplyAntiCache() {
        return applyAntiCache;
    }

    public void setApplyAntiCache(boolean applyAntiCache) {
        this.applyAntiCache = applyAntiCache;
    }

    public boolean isApplyIntegrity() {
        return applyIntegrity;
    }

    public void setApplyIntegrity(boolean applyIntegrity) {
        this.applyIntegrity = applyIntegrity;
    }

    public static class Download {
        /**
         * Download rest and css, declared as cdn links.
         */
        private boolean resources = true;
        private boolean preferMin = true;
        private boolean sourceMaps = true;

        public boolean isResources() {
            return resources;
        }

        public void setResources(boolean resources) {
            this.resources = resources;
        }

        public boolean isPreferMin() {
            return preferMin;
        }

        public void setPreferMin(boolean preferMin) {
            this.preferMin = preferMin;
        }

        public boolean isSourceMaps() {
            return sourceMaps;
        }

        public void setSourceMaps(boolean sourceMaps) {
            this.sourceMaps = sourceMaps;
        }
    }

    public static class Minify {

        /**
         * Minimize html files
         */
        private boolean html = true;
        private boolean htmlJs = true;
        private boolean htmlCss = true;
        /**
         * Minify js.
         */
        private boolean js = true;
        /**
         * Minify css.
         */
        private boolean css = true;
        private boolean generateSourceMaps = true;

        public boolean isHtml() {
            return html;
        }

        public void setHtml(boolean html) {
            this.html = html;
        }

        public boolean isHtmlJs() {
            return htmlJs;
        }

        public void setHtmlJs(boolean htmlJs) {
            this.htmlJs = htmlJs;
        }

        public boolean isHtmlCss() {
            return htmlCss;
        }

        public void setHtmlCss(boolean htmlCss) {
            this.htmlCss = htmlCss;
        }

        public boolean isJs() {
            return js;
        }

        public void setJs(boolean js) {
            this.js = js;
        }

        public boolean isCss() {
            return css;
        }

        public void setCss(boolean css) {
            this.css = css;
        }

        public boolean isGenerateSourceMaps() {
            return generateSourceMaps;
        }

        public void setGenerateSourceMaps(boolean generateSourceMaps) {
            this.generateSourceMaps = generateSourceMaps;
        }
    }
}
