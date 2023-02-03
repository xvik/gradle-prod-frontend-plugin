package ru.vyarus.gradle.frontend.task;


import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Console;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskAction;
import ru.vyarus.gradle.frontend.model.Context;
import ru.vyarus.gradle.frontend.model.OptimizationModel;
import ru.vyarus.gradle.frontend.util.StatsPrinter;

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

        // search htmls
        OptimizationModel optimizationModel = new OptimizationModel(
                root,
                new File(root, getJsDir().get()),
                new File(root, getCssDir().get()));

        optimizationModel.minifyCss(true);
        optimizationModel.minifyJs(true);
        optimizationModel.applyAntiCache();

        optimizationModel.updateHtml(getMinifyHtml().get());
        optimizationModel.generateGzip();

//        if (getDebug().get()) {
//            getLogger().lifecycle("Found html files in {}:\n{}", getProject().relativePath(root), htmls.stream()
//                    .map(file -> "\t" + getProject().relativePath(file))
//                    .sorted()
//                    .collect(Collectors.joining("\n")));
//        }

        System.out.println(StatsPrinter.print(optimizationModel));
    }
}
