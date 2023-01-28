package ru.vyarus.gradle.frontend;

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import ru.vyarus.gradle.frontend.task.OptimizeFrontendTask;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * prod-frontend plugin.
 *
 * @author Vyacheslav Rusakov
 * @since 28.01.2023
 */
public class ProdFrontendPlugin implements Plugin<Project> {
    @Override
    public void apply(final Project project) {
        final ProdFrontendExtension extension = project.getExtensions().create("prodFrontend", ProdFrontendExtension.class);

        project.getTasks().register("prodFrontend", OptimizeFrontendTask.class, task -> {
            task.getDebug().convention(extension.isDebug());
            task.getSourceDir().convention(project.getLayout().getProjectDirectory().dir(extension.getSource()));
            task.getJsDir().convention(extension.getJsFolder());
            task.getCssDir().convention(extension.getCssFolder());
        });
    }

}
