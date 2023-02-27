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
    public abstract Property<Boolean> getMinifyHtml();


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
                .downloadResources()
                .preferMinDownload()
                .minifyJs()
                .minifyCss()
                .minifyHtml()
                .applyAntiCache()
                .applyIntegrity()
                .sourceMaps()
                .gzip()
                .debug(getDebug().get())

                .run()
                .printStats();
    }
}
