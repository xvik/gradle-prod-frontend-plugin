package ru.vyarus.gradle.frontend;

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

    private String source = "build/webapp";

    // required for loading urls
    private String jsFolder = "js";
    private String cssFolder = "css";

    /**
     * Minimize html files
     */
    private boolean minifyHtml = true;
    /**
     * Download rest and css, declared as cdn links.
     */
    private boolean downloadResources = true;

    /**
     * Minify js.
     */
    private boolean minifyJs = true;
    /**
     * Minify css.
     */
    private boolean minifyCss = true;
    /**
     * Create .gz versions for resources
     */
    private boolean gzip = true;
    /**
     * Ani-cache (apply md5 instead of name).
     */
    private boolean appendMD5 = true;

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getJsFolder() {
        return jsFolder;
    }

    public void setJsFolder(String jsFolder) {
        this.jsFolder = jsFolder;
    }

    public String getCssFolder() {
        return cssFolder;
    }

    public void setCssFolder(String cssFolder) {
        this.cssFolder = cssFolder;
    }

    public boolean isMinifyHtml() {
        return minifyHtml;
    }

    public void setMinifyHtml(boolean minifyHtml) {
        this.minifyHtml = minifyHtml;
    }

    public boolean isDownloadResources() {
        return downloadResources;
    }

    public void setDownloadResources(boolean downloadResources) {
        this.downloadResources = downloadResources;
    }

    public boolean isMinifyJs() {
        return minifyJs;
    }

    public void setMinifyJs(boolean minifyJs) {
        this.minifyJs = minifyJs;
    }

    public boolean isMinifyCss() {
        return minifyCss;
    }

    public void setMinifyCss(boolean minifyCss) {
        this.minifyCss = minifyCss;
    }

    public boolean isGzip() {
        return gzip;
    }

    public void setGzip(boolean gzip) {
        this.gzip = gzip;
    }

    public boolean isAppendMD5() {
        return appendMD5;
    }

    public void setAppendMD5(boolean appendMD5) {
        this.appendMD5 = appendMD5;
    }
}
