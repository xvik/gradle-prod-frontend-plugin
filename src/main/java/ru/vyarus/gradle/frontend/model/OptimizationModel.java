package ru.vyarus.gradle.frontend.model;

import org.gradle.api.GradleException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Why no bundle: https://webspeedtools.com/should-i-combine-css-js/
 * * https://wpjohnny.com/why-you-shouldnt-combine-css-js-performance-reasons/
 *
 * @author Vyacheslav Rusakov
 * @since 30.01.2023
 */
public class OptimizationModel {
    private final File baseDir;
    // for relative urls
    private final File jsDir;
    private final File cssDir;
    private List<HtmlModel> htmls = new ArrayList<>();

    public OptimizationModel(final File baseDir, final File jsDir, final File cssDir) {
        this.baseDir = baseDir;
        this.jsDir = jsDir;
        this.cssDir = cssDir;

        if (!jsDir.exists()) {
            jsDir.mkdirs();
        }
        if (!cssDir.exists()) {
            cssDir.mkdirs();
        }

        registerFiles();
    }

    public File getBaseDir() {
        return baseDir;
    }

    public List<HtmlModel> getHtmls() {
        return htmls;
    }

    public void minifyJs(boolean sourceMaps) {
        htmls.forEach(htmlModel -> htmlModel.minifyJs(sourceMaps));
    }

    public void minifyCss(boolean sourceMaps) {
        htmls.forEach(htmlModel -> htmlModel.minifyCss(sourceMaps));
    }

    public void applyAntiCache() {
        htmls.forEach(HtmlModel::applyAntiCache);
    }

    public void updateHtml(boolean minimize) {
        htmls.forEach(htmlModel -> htmlModel.updateHtml(minimize));
    }

    public void generateGzip() {
        htmls.forEach(HtmlModel::gzip);
    }

    private void registerFiles() throws GradleException {
        final List<File> files;
        try {
            files = Files.walk(baseDir.toPath(), 100).filter(path -> {
                File fl = path.toFile();
                if (fl.isDirectory()) {
                    return false;
                }
                final String name = fl.getName().toLowerCase();
                return name.endsWith(".html") || name.endsWith(".htm");
            }).map(Path::toFile).collect(Collectors.toList());
        } catch (IOException ex) {
            throw new GradleException("Error searching for html files in " + baseDir.getAbsolutePath(), ex);
        }

        for (File file : files) {
            try {
                htmls.add(new HtmlModel(jsDir, cssDir, file));
            } catch (Exception ex) {
                throw new GradleException("Failed to parse html " + file.getAbsolutePath(), ex);
            }
        }
    }
}
