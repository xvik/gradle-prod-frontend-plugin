package ru.vyarus.gradle.frontend.task;


import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Console;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskAction;
import ru.vyarus.gradle.frontend.core.OptimizationFlow;

import java.io.File;

/**
 * @author Vyacheslav Rusakov
 * @since 28.01.2023
 */
public abstract class OptimizeFrontendTask extends DefaultTask {

    @Console
    public abstract Property<Boolean> getDebug();

    @InputFiles
    public abstract DirectoryProperty getSourceDir();

    @Input
    public abstract Property<String> getJsDir();

    @Input
    public abstract Property<String> getCssDir();

    @Input
    public abstract Property<Boolean> getDownloadResources();

    @Input
    public abstract Property<Boolean> getPreferMinDownload();

    @Input
    public abstract Property<Boolean> getDownloadSourceMaps();

    @Input
    public abstract Property<Boolean> getMinifyJs();

    @Input
    public abstract Property<Boolean> getMinifyCss();

    @Input
    public abstract Property<Boolean> getGenerateSourceMaps();

    @Input
    public abstract Property<Boolean> getMinifyHtml();

    @Input
    public abstract Property<Boolean> getMinifyHtmlJs();

    @Input
    public abstract Property<Boolean> getMinifyHtmlCss();

    @Input
    public abstract Property<Boolean> getApplyAntiCache();

    @Input
    public abstract Property<Boolean> getApplyIntegrity();

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
