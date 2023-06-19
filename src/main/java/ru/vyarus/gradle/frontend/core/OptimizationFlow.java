package ru.vyarus.gradle.frontend.core;

import ru.vyarus.gradle.frontend.core.info.OptimizationInfo;
import ru.vyarus.gradle.frontend.core.model.HtmlPage;
import ru.vyarus.gradle.frontend.core.util.FileUtils;
import ru.vyarus.gradle.frontend.core.util.StatsPrinter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Html resources optimization tool (with configuration, suitable for templates like jsp or freemarker).
 * Tool optimizes resources DIRECTLY in target directory because it is assumed to be used as one of the latest
 * steps of delivery building (when all resources collected into some build directory and optimization must be
 * performed there just before bundling to zip or war).
 * <p>
 * The main idea is to simplify simple projects development when cdn js libraries used directly (possibly not min
 * version). This way dev process does not need any nodejs tools or local resources copying - tool would download
 * everything in time of delivery bundling. One example is a html page with simple SPA application (e.g. with vuejs),
 * when nodejs tooling is an overkill.
 * <p>
 * If you already use some nodejs bundler, then this tool would be useless.
 * <p>
 * Tool DOES NOT bundle multiple js or css files together because it makes no sense now:
 * <a href="https://webspeedtools.com/should-i-combine-css-js/">article 1</a>,
 * <a href="https://wpjohnny.com/why-you-shouldnt-combine-css-js-performance-reasons/">article 2</a>.
 * <p>
 * Usage: {@code OptimizationFlow.create('target dir').<settings>.run()[.printStats()]}. Note that {@code .run()}
 * returns {@link OptimizationInfo} object containing all optimization info ({@code .printStats()} above use this
 * info) and can be used directly, for example, for alternative statistics view.
 * <p>
 * Optimization steps:
 * <ul>
 *     <li>Load remote resources (if cdn links used). Tries to load minified version (with source maps)</li>
 *     <li>For css resource, loads inner urls (fonts, images, etc,)</li>
 *     <li>If integrity attribute present on resource tag - validates resource before loading</li>
 *     <li>Minify html, js and css resources (not minified already).</li>
 *     <li>Html minification includes inner js and css minification</li>
 *     <li>Applies ani-cache: MD5 hash applied to all links to local files (in html and for css links)</li>
 *     <li>Applies integrity attributes to prevent malicious resources modification</li>
 *     <li>Embed original sources for loaded or generated source maps </li>
 *     <li>Generate gzip versions for all resources (.gz files) to see gzip size and avoid runtime gzip generation
 *      (http server must be configured to serve prepared gz files).</li>
 * </ul>
 * <p>
 * Used tools:
 * <ul>
 *     <li><a href="https://jsoup.org/">Html parser: jsoup (java)</a></li>
 *     <li><a href="https://github.com/wilsonzlin/minify-html">Html minifier: minify-html (native)</a></li>
 *     <li><a href="https://github.com/google/closure-compiler">Js minifier: closure compiler (java)</a></li>
 *     <li><a href="https://github.com/css/csso">Css minifier: csso (js)</a> (run with
 *     <a href="https://www.graalvm.org/">graalvm (no nodejs!)</a></li>
 *     <li><a href="https://github.com/FasterXML/jackson">Source maps manipulation: jackson</a></li>
 * </ul>
 * <p>
 * Architecture note: instead of separation of data and actions, object approach was used - e.g. html object provides
 * all methods for html manipulation, css and js resource objects provide methods for manipulation on exact resource.
 * {@code .run()} method actually returns all objects as-is: action methods are hidden by interfaces (objects pretend
 * to be information-only).
 *
 * @author Vyacheslav Rusakov
 * @since 30.01.2023
 */
@SuppressWarnings({"PMD.ExcessivePublicCount", "PMD.SystemPrintln"})
public final class OptimizationFlow implements OptimizationInfo {

    private final Settings settings;
    private final List<HtmlPage> htmls = new ArrayList<>();

    private OptimizationFlow(final Settings settings) {
        this.settings = settings;
    }

    /**
     * After configuration simply call {@code .run()}.
     * <p>
     * NOTE that optimization performed directly inside directory.
     *
     * @param baseDir base directory where to search html files for optimization
     * @return builder for optimization flow configuration
     */
    public static Builder create(final File baseDir) {
        return new Builder(baseDir);
    }

    /**
     * Available after {@link #findFiles()}.
     *
     * @return all detected html pages
     */
    @Override
    public List<HtmlPage> getHtmls() {
        return htmls;
    }

    /**
     * @return unmodifiable settings object (settings prepared before execution)
     */
    @Override
    public Settings getSettings() {
        return settings;
    }

    /**
     * Search for html files inside configured directory for further optimization. Searched file extensions could
     * be configured (in order to search templates like freemarker or jsp).
     * <p>
     * Method will also trigger html parsing and resources resolution (js and css tags detection).
     *
     * @return flow object
     * @throws java.lang.IllegalStateException on processing errors
     */
    public OptimizationFlow findFiles() throws IllegalStateException {
        final List<File> files = FileUtils.findHtmls(settings.getBaseDir(), settings.getHtmlExtensions()
                .stream().map(String::toLowerCase).collect(Collectors.toList()));
        for (File file : files) {
            try {
                final HtmlPage html = new HtmlPage(settings, file);
                htmls.add(html);
                html.findResources();
            } catch (Exception ex) {
                throw new IllegalStateException("Failed to parse html " + file.getAbsolutePath(), ex);
            }
        }
        return this;
    }

    /**
     * Download (if required) detected js and css files. Analyze css and download referenced resources (fonts, images,
     * etc.).
     * <p>
     * If integrity attribute present on js or css tag, performs integrity validation (and fail if hash does not match).
     * Removes "crossorigin" and "integrity" attributes for downloaded resources.
     * <p>
     * Css files with urls being updated: urls replaced with local paths to downloaded files, md5 hashes applied
     * for anti-cache. Note that css updates can't be delayed because calculation of md5 for root css requires
     * already updated files. Css sub-resources md5 calculation might be skipped if not configured for root resources.
     *
     * @return flow object
     */
    public OptimizationFlow resolveResources() {
        htmls.forEach(HtmlPage::resolveResources);
        return this;
    }

    /**
     * Perform js resources minification. Only for resources without ".min" in name.
     *
     * @return flow object
     */
    public OptimizationFlow minifyJs() {
        if (settings.isMinifyJs()) {
            htmls.forEach(HtmlPage::minifyJs);
        }
        return this;
    }

    /**
     * Perform css resources minification. Only for resources without ".min" in name.
     *
     * @return flow object
     */
    public OptimizationFlow minifyCss() {
        if (settings.isMinifyCss()) {
            htmls.forEach(HtmlPage::minifyCss);
        }
        return this;
    }

    /**
     * Add "integrity" attribute to resource tags so browsers could verify loaded file correctness (prevent
     * malicious modifications).
     *
     * @return flow object
     */
    public OptimizationFlow applyIntegrity() {
        if (settings.isApplyIntegrity()) {
            htmls.forEach(HtmlPage::applyIntegrity);
        }
        return this;
    }

    /**
     * Adds MD5 hash for resource urls ("?hash"). Note that css sub-urls already contain hashes at this stage
     * because their application implies root css modification which leads to MD5 hash change.
     *
     * @return flow object
     */
    public OptimizationFlow applyAntiCache() {
        if (settings.isApplyAntiCache()) {
            htmls.forEach(HtmlPage::applyAntiCache);
        }
        return this;
    }

    /**
     * Change resource tags and minify html. Resource tags replacement implemented as direct string replacement
     * (and not with jsoup) to avoid possible structure damages (e.g. jsoup might add additional tags in jsp or
     * other template files). Minification could lead to unwanted changes in not pure html templates.
     *
     * @return flow object
     */
    public OptimizationFlow updateHtml() {
        htmls.forEach(HtmlPage::updateHtml);
        return this;
    }

    /**
     * Generate gzipped files (.gz) for all resources (html, root cs and js and relative css resources).
     * Produces files could be used by http server to avoid hot gzipping.
     *
     * @return flow object
     */
    public OptimizationFlow generateGzip() {
        if (settings.isGzip()) {
            htmls.forEach(HtmlPage::gzip);
        }
        return this;
    }

    /**
     * Print optimization stats.
     */
    @Override
    public void printStats() {
        System.out.println(StatsPrinter.print(this));
    }

    /**
     * Optimization settings.
     */
    @SuppressWarnings("PMD.TooManyFields")
    public static class Settings {
        private final File baseDir;
        private File jsDir;
        private File cssDir;
        private List<String> htmlExtensions = Arrays.asList("html", "htm");
        private boolean downloadResources;
        private boolean preferMinDownload;
        private boolean downloadSourceMaps;
        private boolean minifyJs;
        private boolean minifyCss;
        private boolean minifyHtml;
        private boolean minifyHtmlCss;
        private boolean minifyHtmlJs;
        private boolean applyAntiCache;
        private boolean applyIntegrity;
        private boolean generateSourceMaps;
        private boolean gzip;
        private boolean debug;

        public Settings(final File baseDir) {
            this.baseDir = baseDir;
            jsDir = new File(baseDir, "js");
            cssDir = new File(baseDir, "css");
        }

        /**
         * @return directory where html files must be found and processed
         */
        public File getBaseDir() {
            return baseDir;
        }

        /**
         * @return directory name for downloaded js files
         */
        public File getJsDir() {
            return jsDir;
        }

        /**
         * @return directory name for downloaded css files
         */
        public File getCssDir() {
            return cssDir;
        }

        /**
         * @return extensions of files to process (recognized as html)
         */
        public List<String> getHtmlExtensions() {
            return htmlExtensions;
        }

        /**
         * @return true to download remote js and css links (e.g. cdn links)
         */
        public boolean isDownloadResources() {
            return downloadResources;
        }

        /**
         * @return true to try to download ".min" resource version first (for cdn)
         */
        public boolean isPreferMinDownload() {
            return preferMinDownload;
        }

        /**
         * @return true to download source maps (and sources) for minified version
         */
        public boolean isDownloadSourceMaps() {
            return downloadSourceMaps;
        }

        /**
         * @return true to minify js (not marked as ".min")
         */
        public boolean isMinifyJs() {
            return minifyJs;
        }

        /**
         * @return true to minify css (not marked as ".min")
         */
        public boolean isMinifyCss() {
            return minifyCss;
        }

        /**
         * @return true to minify html
         */
        public boolean isMinifyHtml() {
            return minifyHtml;
        }

        /**
         * @return true to minify raw css inside html
         */
        public boolean isMinifyHtmlCss() {
            return minifyHtmlCss;
        }

        /**
         * @return true to minify raw js inside html
         */
        public boolean isMinifyHtmlJs() {
            return minifyHtmlJs;
        }

        /**
         * @return true to apply MD5 hashes into all file urls (inside html and css)
         */
        public boolean isApplyAntiCache() {
            return applyAntiCache;
        }

        /**
         * @return true to apply integrity attributes to resource tags so browser could verify loaded resource for
         * unwanted changes.
         */
        public boolean isApplyIntegrity() {
            return applyIntegrity;
        }

        /**
         * @return true to generate source maps for minified resources
         */
        public boolean isGenerateSourceMaps() {
            return generateSourceMaps;
        }

        /**
         * @return true to create .gz files for all resources
         */
        public boolean isGzip() {
            return gzip;
        }

        /**
         * @return true to show extra logs
         */
        public boolean isDebug() {
            return debug;
        }
    }

    /**
     * Optimization configuration builder. Builder used because configuration must be immutable.
     * For all builder methods null is ignored (specially for case when configuration applied from some nullable
     * source, like {@code builder.jsDir(System.getProperty("custom.js.dir))}).
     */
    @SuppressWarnings({"PMD.GodClass", "PMD.TooManyMethods"})
    public static class Builder {
        private final Settings settings;

        public Builder(final File basePath) {
            this.settings = new Settings(basePath);
        }

        /**
         * @param dir directory for downloaded js files
         * @return builder instance
         */
        public Builder jsDir(final File dir) {
            if (dir != null) {
                settings.jsDir = dir;
            }
            return this;
        }

        /**
         * @param relative directory for downloaded js files (relative to configured base directory)
         * @return builder instance
         */
        public Builder jsDir(final String relative) {
            if (relative != null) {
                jsDir(new File(settings.baseDir, relative));
            }
            return this;
        }

        /**
         * @param dir directory for downloaded css files
         * @return builder instance
         */
        public Builder cssDir(final File dir) {
            if (dir != null) {
                settings.cssDir = dir;
            }
            return this;
        }

        /**
         * @param relative directory for downloaded css files (relative to configured base directory).
         * @return builder instance
         */
        public Builder cssDir(final String relative) {
            if (relative != null) {
                cssDir(new File(settings.baseDir, relative));
            }
            return this;
        }

        /**
         * Different value might be configured to process templates (jsp, jte, freemarker, etc.).
         *
         * @param extensions extensions of files to process (as html files)
         * @return builder instance
         */
        public Builder htmlExtensions(final String... extensions) {
            return htmlExtensions(Arrays.asList(extensions));
        }

        /**
         * Different value might be configured to process templates (jsp, jte, freemarker, etc.).
         *
         * @param extensions extensions of files to process (as html files)
         * @return builder instance
         */
        public Builder htmlExtensions(final List<String> extensions) {
            if (!extensions.isEmpty()) {
                settings.htmlExtensions = extensions;
            }
            return this;
        }

        /**
         * @param download true to download remote js and css links
         * @return builder instance
         */
        public Builder downloadResources(final Boolean download) {
            if (download != null) {
                settings.downloadResources = download;
            }
            return this;
        }

        /**
         * Shortcut for {@link #downloadResources(Boolean)}.
         *
         * @return builder instance
         */
        public Builder downloadResources() {
            return downloadResources(true);
        }

        /**
         * Use case: use dev resources version from cdn and for delivery update them to .min cdn version (instead
         * of manual minification).
         *
         * @param min true to try to download ".min" resource version first (for cdn)
         * @return builder instance
         */
        public Builder preferMinDownload(final Boolean min) {
            if (min != null) {
                settings.preferMinDownload = min;
            }
            return this;
        }

        /**
         * Shortcut for {@link #preferMinDownload(Boolean)}.
         *
         * @return builder instance
         */
        public Builder preferMinDownload() {
            return preferMinDownload(true);
        }

        /**
         * @param sourceMaps true to try to download source maps (and sources) for minified version
         * @return builder instance
         */
        public Builder downloadSourceMaps(final Boolean sourceMaps) {
            if (sourceMaps != null) {
                settings.downloadSourceMaps = sourceMaps;
            }
            return this;
        }

        /**
         * Shortcut for {@link #downloadSourceMaps(Boolean)}.
         *
         * @return builder instance
         */
        public Builder downloadSourceMaps() {
            return downloadSourceMaps(true);
        }

        /**
         * @param minify true to minify js (not marked as ".min")
         * @return builder instance
         */
        public Builder minifyJs(final Boolean minify) {
            if (minify != null) {
                settings.minifyJs = minify;
            }
            return this;
        }

        /**
         * Shortcut for {@link #minifyJs(Boolean)}.
         *
         * @return builder instance
         */
        public Builder minifyJs() {
            return minifyJs(true);
        }

        /**
         * @param minify true to minify js (not marked as ".min")
         * @return builder instance
         */
        public Builder minifyCss(final Boolean minify) {
            if (minify != null) {
                settings.minifyCss = minify;
            }
            return this;
        }

        /**
         * Shortcut for {@link #minifyCss(Boolean)}.
         *
         * @return builder instance
         */
        public Builder minifyCss() {
            return minifyCss(true);
        }

        /**
         * @param minify true to minify html
         * @return builder instance
         */
        public Builder minifyHtml(final Boolean minify) {
            if (minify != null) {
                settings.minifyHtml = minify;
            }
            return this;
        }

        /**
         * Shortcut for {@link #minifyHtml(Boolean)}.
         *
         * @return builder instance
         */
        public Builder minifyHtml() {
            return minifyHtml(true);
        }

        /**
         * @param minify true to minify raw css inside html
         * @return builder instance
         */
        public Builder minifyHtmlCss(final Boolean minify) {
            if (minify != null) {
                settings.minifyHtmlCss = minify;
            }
            return this;
        }

        /**
         * Shortcut for {@link #minifyHtmlCss(Boolean)}.
         *
         * @return builder instance
         */
        public Builder minifyHtmlCss() {
            return minifyHtmlCss(true);
        }

        /**
         * @param minify true to minify raw js inside html
         * @return builder instance
         */
        public Builder minifyHtmlJs(final Boolean minify) {
            if (minify != null) {
                settings.minifyHtmlJs = minify;
            }
            return this;
        }

        /**
         * Shortcut for {@link #minifyHtmlCss(Boolean)}.
         *
         * @return builder instance
         */
        public Builder minifyHtmlJs() {
            return minifyHtmlJs(true);
        }

        /**
         * This allows to configure forever caching for static resources (except root html).
         *
         * @param antiCache true to apply MD5 hashes into all file urls (inside html and css)
         * @return builder instance
         */
        public Builder applyAntiCache(final Boolean antiCache) {
            if (antiCache != null) {
                settings.applyAntiCache = antiCache;
            }
            return this;
        }

        /**
         * Shortcut for {@link #applyAntiCache(Boolean)}.
         *
         * @return builder instance
         */
        public Builder applyAntiCache() {
            return applyAntiCache(true);
        }

        /**
         * Integrity attribute shields user from malicious resource modifications.
         *
         * @param integrity true to apply integrity attributes to resource tags so browser could verify loaded resource
         *                  for unwanted changes
         * @return builder instance
         */
        public Builder applyIntegrity(final Boolean integrity) {
            if (integrity != null) {
                settings.applyIntegrity = integrity;
            }
            return this;
        }

        /**
         * Shortcut for {@link #applyIntegrity(Boolean)}.
         *
         * @return builder instance
         */
        public Builder applyIntegrity() {
            return applyIntegrity(true);
        }

        /**
         * NOTE: source maps generated only for manually minified resources (source map for minified resources
         * downloaded from cdn is impossible to build)!
         *
         * @param generateSourceMaps true to generate source maps for minified resources
         * @return builder instance
         */
        public Builder generateSourceMaps(final Boolean generateSourceMaps) {
            if (generateSourceMaps != null) {
                settings.generateSourceMaps = generateSourceMaps;
            }
            return this;
        }

        /**
         * Shortcut for {@link #generateSourceMaps(Boolean)}.
         *
         * @return builder instance
         */
        public Builder generateSourceMaps() {
            return generateSourceMaps(true);
        }

        /**
         * Gzipped files size also shown in final report (to better understand actual resources "weight").
         *
         * @param gzip true to create .gz files for all resources
         * @return builder instance
         */
        public Builder gzip(final Boolean gzip) {
            if (gzip != null) {
                settings.gzip = gzip;
            }
            return this;
        }

        /**
         * Shortcut for {@link #gzip(Boolean)}.
         *
         * @return builder instance
         */
        public Builder gzip() {
            return gzip(true);
        }

        /**
         * @param debug true to enable debug mode with extra logs
         * @return builder instance
         */
        public Builder debug(final Boolean debug) {
            if (debug != null) {
                settings.debug = debug;
            }
            return this;
        }

        /**
         * Shortcut for {@link #debug(Boolean)}.
         *
         * @return builder instance
         */
        public Builder debug() {
            return debug(true);
        }

        /**
         * Run configured optimization. See {@link ru.vyarus.gradle.frontend.core.info.OptimizationInfo#printStats()}
         * for quick stats output.
         *
         * @return optimization info object
         */
        public OptimizationInfo run() {
            return new OptimizationFlow(settings)
                    .findFiles()
                    .resolveResources()
                    .minifyJs()
                    .minifyCss()
                    .applyIntegrity()
                    .applyAntiCache()

                    .updateHtml()
                    .generateGzip();
        }
    }
}
