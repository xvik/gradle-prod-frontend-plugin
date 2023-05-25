package ru.vyarus.gradle.frontend;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import ru.vyarus.gradle.frontend.task.OptimizeFrontendTask;

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
            task.getSourceDir().convention(project.getLayout().getProjectDirectory().dir(extension.getSourceDir()));
            task.getJsDir().convention(extension.getJsDir());
            task.getCssDir().convention(extension.getCssDir());
            task.getHtmlExtensions().convention(extension.getHtmlExtensions());

            final ProdFrontendExtension.Download download = extension.getDownload();
            task.getDownloadResources().convention(download.isResources());
            task.getPreferMinDownload().convention(download.isPreferMin());
            task.getDownloadSourceMaps().convention(download.isSourceMaps());

            final ProdFrontendExtension.Minify minify = extension.getMinify();
            task.getMinifyHtml().convention(minify.isHtml());
            task.getMinifyJs().convention(minify.isJs());
            task.getMinifyCss().convention(minify.isCss());
            task.getMinifyHtmlJs().convention(minify.isHtmlJs());
            task.getMinifyHtmlCss().convention(minify.isHtmlJs());
            task.getGenerateSourceMaps().convention(minify.isGenerateSourceMaps());

            task.getApplyAntiCache().convention(extension.isApplyAntiCache());
            task.getApplyIntegrity().convention(extension.isApplyIntegrity());
            task.getGzip().convention(extension.isGzip());
        });
    }

}
