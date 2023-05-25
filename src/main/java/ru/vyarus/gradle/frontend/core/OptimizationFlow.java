package ru.vyarus.gradle.frontend.core;

import org.gradle.api.GradleException;
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
 * Why no bundle: https://webspeedtools.com/should-i-combine-css-js/
 * * https://wpjohnny.com/why-you-shouldnt-combine-css-js-performance-reasons/
 *
 * @author Vyacheslav Rusakov
 * @since 30.01.2023
 */
public class OptimizationFlow implements OptimizationInfo {

    private final Settings settings;
    private final List<HtmlPage> htmls = new ArrayList<>();

    public static Builder create(final File baseDir) {
        return new Builder(baseDir);
    }

    private OptimizationFlow(final Settings settings) {
        this.settings = settings;
    }

    public List<HtmlPage> getHtmls() {
        return htmls;
    }

    @Override
    public Settings getSettings() {
        return settings;
    }

    public OptimizationFlow findFiles() throws GradleException {
        final List<File> files = FileUtils.findHtmls(settings.getBaseDir(), settings.getHtmlExtensions()
                .stream().map(String::toLowerCase).collect(Collectors.toList()));
        for (File file : files) {
            try {
                final HtmlPage html = new HtmlPage(settings, file);
                htmls.add(html);
                // todo apply exclusions
                html.findResources();
            } catch (Exception ex) {
                throw new GradleException("Failed to parse html " + file.getAbsolutePath(), ex);
            }
        }
        return this;
    }

    public OptimizationFlow resolveResources() {
        htmls.forEach(HtmlPage::resolveResources);
        return this;
    }

    public OptimizationFlow minifyJs() {
        if (settings.isMinifyJs()) {
            htmls.forEach(HtmlPage::minifyJs);
        }
        return this;
    }

    public OptimizationFlow minifyCss() {
        if (settings.isMinifyCss()) {
            htmls.forEach(HtmlPage::minifyCss);
        }
        return this;
    }

    public OptimizationFlow applyIntegrity() {
        if (settings.isApplyIntegrity()) {
            htmls.forEach(HtmlPage::applyIntegrity);
        }
        return this;
    }

    public OptimizationFlow applyAntiCache() {
        if (settings.isApplyAntiCache()) {
            htmls.forEach(HtmlPage::applyAntiCache);
        }
        return this;
    }

    public OptimizationFlow updateHtml() {
        htmls.forEach(HtmlPage::updateHtml);
        return this;
    }


    public OptimizationFlow generateGzip() {
        if (settings.isGzip()) {
            htmls.forEach(HtmlPage::gzip);
        }
        return this;
    }

    @Override
    public void printStats() {
        System.out.println(StatsPrinter.print(this));
    }

    public static class Settings {
        private final File baseDir;
        // for relative urls
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

        public File getBaseDir() {
            return baseDir;
        }

        public File getJsDir() {
            return jsDir;
        }

        public File getCssDir() {
            return cssDir;
        }

        public List<String> getHtmlExtensions() {
            return htmlExtensions;
        }

        public boolean isDownloadResources() {
            return downloadResources;
        }

        public boolean isPreferMinDownload() {
            return preferMinDownload;
        }

        public boolean isDownloadSourceMaps() {
            return downloadSourceMaps;
        }

        public boolean isMinifyJs() {
            return minifyJs;
        }

        public boolean isMinifyCss() {
            return minifyCss;
        }

        public boolean isMinifyHtml() {
            return minifyHtml;
        }

        public boolean isMinifyHtmlCss() {
            return minifyHtmlCss;
        }

        public boolean isMinifyHtmlJs() {
            return minifyHtmlJs;
        }

        public boolean isApplyAntiCache() {
            return applyAntiCache;
        }

        public boolean isApplyIntegrity() {
            return applyIntegrity;
        }

        public boolean isGenerateSourceMaps() {
            return generateSourceMaps;
        }

        public boolean isGzip() {
            return gzip;
        }

        public boolean isDebug() {
            return debug;
        }
    }

    public static class Builder {
        private final Settings settings;

        public Builder(File basePath) {
            this.settings = new Settings(basePath);
        }

        public Builder jsDir(File dir) {
            if (dir != null) {
                settings.jsDir = dir;
            }
            return this;
        }

        public Builder jsDir(String relative) {
            if (relative != null) {
                jsDir(new File(settings.baseDir, relative));
            }
            return this;
        }

        public Builder cssDir(File dir) {
            if (dir != null) {
                settings.cssDir = dir;
            }
            return this;
        }

        public Builder cssDir(String relative) {
            if (relative != null) {
                cssDir(new File(settings.baseDir, relative));
            }
            return this;
        }

        public Builder htmlExtensions(final String... extensions) {
            return htmlExtensions(Arrays.asList(extensions));
        }

        public Builder htmlExtensions(final List<String> extensions) {
            if (!extensions.isEmpty()) {
                settings.htmlExtensions = extensions;
            }
            return this;
        }

        public Builder downloadResources(final Boolean download) {
            if (download != null) {
                settings.downloadResources = download;
            }
            return this;
        }

        public Builder downloadResources() {
            return downloadResources(true);
        }

        public Builder preferMinDownload(final Boolean min) {
            if (min != null) {
                settings.preferMinDownload = min;
            }
            return this;
        }

        public Builder preferMinDownload() {
            return preferMinDownload(true);
        }

        public Builder downloadSourceMaps(final Boolean sourceMaps) {
            if (sourceMaps != null) {
                settings.downloadSourceMaps = sourceMaps;
            }
            return this;
        }

        public Builder downloadSourceMaps() {
            return downloadSourceMaps(true);
        }

        public Builder minifyJs(final Boolean minify) {
            if (minify != null) {
                settings.minifyJs = minify;
            }
            return this;
        }

        public Builder minifyJs() {
            return minifyJs(true);
        }

        public Builder minifyCss(final Boolean minify) {
            if (minify != null) {
                settings.minifyCss = minify;
            }
            return this;
        }

        public Builder minifyCss() {
            return minifyCss(true);
        }

        public Builder minifyHtml(final Boolean minify) {
            if (minify != null) {
                settings.minifyHtml = minify;
            }
            return this;
        }

        public Builder minifyHtml() {
            return minifyHtml(true);
        }

        public Builder minifyHtmlCss(final Boolean minify) {
            if (minify != null) {
                settings.minifyHtmlCss = minify;
            }
            return this;
        }

        public Builder minifyHtmlCss() {
            return minifyHtmlCss(true);
        }

        public Builder minifyHtmlJs(final Boolean minify) {
            if (minify != null) {
                settings.minifyHtmlJs = minify;
            }
            return this;
        }

        public Builder minifyHtmlJs() {
            return minifyHtmlJs(true);
        }

        public Builder applyAntiCache(final Boolean antiCache) {
            if (antiCache != null) {
                settings.applyAntiCache = antiCache;
            }
            return this;
        }

        public Builder applyAntiCache() {
            return applyAntiCache(true);
        }

        public Builder applyIntegrity(final Boolean integrity) {
            if (integrity != null) {
                settings.applyIntegrity = integrity;
            }
            return this;
        }

        public Builder applyIntegrity() {
            return applyIntegrity(true);
        }

        public Builder generateSourceMaps(final Boolean generateSourceMaps) {
            if (generateSourceMaps != null) {
                settings.generateSourceMaps = generateSourceMaps;
            }
            return this;
        }

        public Builder generateSourceMaps() {
            return generateSourceMaps(true);
        }

        public Builder gzip(final Boolean gzip) {
            if (gzip != null) {
                settings.gzip = gzip;
            }
            return this;
        }

        public Builder gzip() {
            return gzip(true);
        }

        public Builder debug(final Boolean debug) {
            if (debug != null) {
                settings.debug = debug;
            }
            return this;
        }

        public Builder debug() {
            return debug(true);
        }

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
