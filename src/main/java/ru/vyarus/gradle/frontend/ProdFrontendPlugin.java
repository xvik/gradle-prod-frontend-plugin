package ru.vyarus.gradle.frontend;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import ru.vyarus.gradle.frontend.task.OptimizeFrontendTask;

/**
 * Production frontend plugin. Plugin detects all html files and optimize them and related resources.
 * <p>
 * IMPORTANT: Plugin is intended to be used on delivery generation phase only and not during development.
 * Plugin DOES NOT bundle multiple js or css files together because it makes no sense now:
 * <a href="https://webspeedtools.com/should-i-combine-css-js/">article 1</a>,
 * <a href="https://wpjohnny.com/why-you-shouldnt-combine-css-js-performance-reasons/">article 2</a>.
 * <p>
 * Usage scenario: copy application assets somewhere inside build directory, run plugin on then and then bundle
 * result into final delivery.
 * NOTE that it is a bad idea to run plugin on directory inside sources (it will work, but your sources would be
 * modified!).
 * <p>
 * Plugin intended to be used in simple cases when not too complex html page must be created and nodejs tools are
 * not wanted. During development cdn links to resources are used and for delivery all remote files are loaded
 * and bundled with application (plus, additional security applied). One example is a html page with simple SPA
 * application (e.g. with vuejs), when nodejs tooling is an overkill.
 * <p>
 * If you already use some nodejs bundler, then this plugin would be useless.
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
 * Plugin updates files directly inside specified directory. Re-runs on already processed directory would not harm.
 * <p>
 * Could be used on non-html files (like jsp, freemarker, etc. templates), but, js and css resource tags must be
 * pure html (parsable). Also, html minification may harm some templates and so should be disabled.
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
 * Gradle war plugin is not supported directly: you need to copy html-related resources into some directory first and
 * generate war only from that directory.
 *
 * @author Vyacheslav Rusakov
 * @since 28.01.2023
 */
public class ProdFrontendPlugin implements Plugin<Project> {

    @Override
    public void apply(final Project project) {
        final ProdFrontendExtension extension = project.getExtensions().create("prodFrontend",
                ProdFrontendExtension.class);

        project.getTasks().register("prodFrontend", OptimizeFrontendTask.class, task -> {
            task.getDebug().convention(extension.isDebug());
            task.getSourceDir().convention(project.getLayout().getProjectDirectory().dir(extension.getSourceDir()));
            task.getJsDir().convention(extension.getJsDir());
            task.getCssDir().convention(extension.getCssDir());
            task.getHtmlExtensions().convention(extension.getHtmlExtensions());

            final ProdFrontendExtension.Download download = extension.getDownload();
            task.getDownloadResources().convention(download.isEnabled());
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
