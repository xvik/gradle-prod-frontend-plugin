package ru.vyarus.gradle.frontend.task;


import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Console;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskAction;
import ru.vyarus.gradle.frontend.core.OptimizationFlow;

import java.io.File;

/**
 * Frontend optimization task.
 * <p>
 * NOTE: task always execute, because there is no way to detect UP_TO_DATE state, as files being modified
 * directly inside target directory.
 * <p>
 * Task supports execution on already processed folder (in this case simply no optimizations would be performed).
 * <p>
 * System output used instead of gradle logs for all task-related messages.
 *
 * @author Vyacheslav Rusakov
 * @since 28.01.2023
 */
public abstract class OptimizeFrontendTask extends DefaultTask {

    /**
     * Default: false.
     *
     * @return true to enable debug mode with extra logs
     */
    @Console
    public abstract Property<Boolean> getDebug();

    /**
     * Default: build/webapp.
     * NOTE that optimization performed directly inside directory.
     *
     * @return directory where html files must be found and processed
     */
    @InputFiles
    public abstract DirectoryProperty getSourceDir();

    /**
     * Default: js (inside sourceDir).
     *
     * @return directory name for downloaded js files
     */
    @Input
    public abstract Property<String> getJsDir();

    /**
     * Default: css (inside sourceDir).
     *
     * @return directory name for downloaded css files
     */
    @Input
    public abstract Property<String> getCssDir();

    /**
     * Default: htm, html.
     * Different value might be configured to process templates (jsp, jte, freemarker, etc.)
     *
     * @return extensions of files to process (recognized as html)
     */
    @Input
    public abstract ListProperty<String> getHtmlExtensions();

    /**
     * Default: true.
     *
     * @return true to download remote js and css links (e.g. cdn links)
     */
    @Input
    public abstract Property<Boolean> getDownloadResources();

    /**
     * Default: true.
     *
     * @return true to try to download ".min" resource version first (for cdn)
     */
    @Input
    public abstract Property<Boolean> getPreferMinDownload();

    /**
     * Default: true.
     *
     * @return true to download source maps (and sources) for minified version
     */
    @Input
    public abstract Property<Boolean> getDownloadSourceMaps();

    /**
     * Default: true.
     *
     * @return true to minify js (not marked as ".min")
     */
    @Input
    public abstract Property<Boolean> getMinifyJs();

    /**
     * Default: true.
     *
     * @return true to minify css (not marked as ".min")
     */
    @Input
    public abstract Property<Boolean> getMinifyCss();

    /**
     * Default: true.
     * Only for resources minified by task! If minified resource downloaded from cdn miss source map - there is no way
     * to generate it.
     *
     * @return true to generate source maps for minified resources
     */
    @Input
    public abstract Property<Boolean> getGenerateSourceMaps();

    /**
     * Default: true.
     *
     * @return true to minify html
     */
    @Input
    public abstract Property<Boolean> getMinifyHtml();

    /**
     * Default: true.
     *
     * @return true to minify raw js inside html
     */
    @Input
    public abstract Property<Boolean> getMinifyHtmlJs();

    /**
     * Default: true.
     *
     * @return true to minify raw css inside html
     */
    @Input
    public abstract Property<Boolean> getMinifyHtmlCss();

    /**
     * Default: true.
     * This allows to configure forever caching for static resources (except root html).
     *
     * @return true to apply MD5 hashes into all file urls (inside html and css)
     */
    @Input
    public abstract Property<Boolean> getApplyAntiCache();

    /**
     * Default: true.
     * Integrity attribute shields user from malicious resource modifications.
     *
     * @return true to apply integrity attributes to resource tags so browser could verify loaded resource for
     * unwanted changes.
     */
    @Input
    public abstract Property<Boolean> getApplyIntegrity();

    /**
     * Default: true.
     * Gzipped files size also shown in final report (to better understand actual resources "weight").
     *
     * @return true to create .gz files for all resources
     */
    @Input
    public abstract Property<Boolean> getGzip();

    @TaskAction
    public void run() {
        // check target folder
        final File root = getSourceDir().get().getAsFile();
        if (!root.exists()) {
            throw new GradleException("Webapp directory does not exists: " + root.getAbsolutePath());
        }

        OptimizationFlow.create(root)
                .jsDir(getJsDir().get())
                .cssDir(getCssDir().get())
                .htmlExtensions(getHtmlExtensions().get())
                .downloadResources(getDownloadResources().get())
                .preferMinDownload(getPreferMinDownload().get())
                .downloadSourceMaps(getDownloadSourceMaps().get())
                .minifyJs(getMinifyJs().get())
                .minifyCss(getMinifyCss().get())
                .generateSourceMaps(getGenerateSourceMaps().get())
                .minifyHtml(getMinifyHtml().get())
                .minifyHtmlCss(getMinifyHtmlCss().get())
                .minifyHtmlJs(getMinifyHtmlJs().get())
                .applyAntiCache(getApplyAntiCache().get())
                .applyIntegrity(getApplyIntegrity().get())
                .gzip(getGzip().get())
                .debug(getDebug().get())

                .run()
                .printStats();
    }
}
