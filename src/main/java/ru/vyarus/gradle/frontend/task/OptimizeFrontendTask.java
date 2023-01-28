package ru.vyarus.gradle.frontend.task;


import in.wilsonl.minifyhtml.Configuration;
import in.wilsonl.minifyhtml.MinifyHtml;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Console;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.TaskAction;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

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


    @TaskAction
    public void run() {
        // check target folder
        final File root = getSourceDir().get().getAsFile();
        if (!root.exists()) {
            throw new GradleException("Webapp directory does not exists: " + root.getAbsolutePath());
        }

        // search htmls
        final List<File> htmls;
        try {
            htmls = Files.walk(root.toPath(), 100).filter(path -> {
                File fl = path.toFile();
                if (fl.isDirectory()) {
                    return false;
                }
                final String name = fl.getName().toLowerCase();
                return name.endsWith(".html") || name.endsWith(".htm");
            }).map(Path::toFile).collect(Collectors.toList());
        } catch (IOException e) {
            throw new GradleException("Error searching for html files in " + root.getAbsolutePath(), e);
        }
        if (getDebug().get()) {
            getLogger().lifecycle("Found html files in {}:\n{}", getProject().relativePath(root), htmls.stream()
                    .map(file -> "\t" + getProject().relativePath(file))
                    .sorted()
                    .collect(Collectors.joining("\n")));
        }

        File jsDir = new File(root, getJsDir().get());
        File cssDir = new File(root, getCssDir().get());


        // inspect htmls
        for (File html : htmls) {

            System.out.println("Processing: " + getProject().relativePath(html));

            try {
                final Document doc = Jsoup.parse(html);
                final Elements css = doc.select("link[href]");
                final Elements jss = doc.select("script[src]");
                boolean changed = false;

                for (Element cs : css) {
                    String target = cs.attr("href");
                    if (target.toLowerCase().startsWith("http")) {
                        // url - just downloading it to local directory here (as-is)
                        String name = getFileName(target);
                        // todo check existence
                        File path = new File(cssDir, name);
                        path.getParentFile().mkdirs();
                        download(target, path.getAbsolutePath());

                        final String localTarget = html.getParentFile().toPath().relativize(path.toPath()).toString();
                        cs.attr("href", localTarget);
                        System.out.println(target + " loaded as " + localTarget);

                        changed = true;
                    }
                }

                for (Element js : jss) {
                    String target = js.attr("src");
                    if (target.toLowerCase().startsWith("http")) {
                        // url - just downloading it to local directory here (as-is)
                        String name = getFileName(target);
                        // todo check existence
                        File path = new File(jsDir, name);
                        path.getParentFile().mkdirs();
                        download(target, path.getAbsolutePath());

                        final String localTarget = html.getParentFile().toPath().relativize(path.toPath()).toString();
                        js.attr("src", localTarget);
                        System.out.println(target + " loaded as " + localTarget);

                        changed = true;
                    }
                }

                if (changed) {
                    // overwrite file
                    FileUtils.writeStringToFile(html, doc.outerHtml(), StandardCharsets.UTF_8);
                }

            } catch (IOException e) {
                throw new GradleException("Error parsing html file: " + html.getAbsolutePath(), e);
            }


//            Configuration cfg = new Configuration.Builder()
//                    .setKeepHtmlAndHeadOpeningTags(true)
//                    .setMinifyCss(true)
//                    .setMinifyJs(true)
//                    .build();
//
//            String minified = MinifyHtml.minify("<p>  Hello, world!  </p>", cfg);
        }
    }

    private String getFileName(String url) {
        int idx = url.lastIndexOf('/');
        if (idx == 0) {
            idx = url.lastIndexOf('\\');
        }
        if (idx > 0) {
            return url.substring(idx + 1);
        }
        // better then nothing
        return url.replaceAll("[\\/]", "_");
    }

    private static void download(String urlStr, String file) throws IOException {
        URL url = new URL(urlStr);
        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        FileOutputStream fos = new FileOutputStream(file);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        fos.close();
        rbc.close();
    }
}
