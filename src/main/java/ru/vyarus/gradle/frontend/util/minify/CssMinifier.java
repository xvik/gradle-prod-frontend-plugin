package ru.vyarus.gradle.frontend.util.minify;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import ru.vyarus.gradle.frontend.util.FileUtils;
import ru.vyarus.gradle.frontend.util.SourceMapUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * https://github.com/css/csso
 * latest csso version could be loaded from https://cdn.jsdelivr.net/npm/csso
 *
 * @author Vyacheslav Rusakov
 * @since 31.01.2023
 */
public final class CssMinifier {

    private CssMinifier() {
    }

    public static MinifyResult minify(final File file, final boolean sourceMaps) {
        String name = file.getName();
        name = FileUtils.getMinName(name);
        final File target = new File(file.getParentFile(), name);
        final File sourceMap = new File(target.getAbsolutePath() + ".map");

        File localCsso = null;
        try (Context context = Context.newBuilder("js")
                .currentWorkingDirectory(file.getParentFile().toPath())
                .allowIO(true)
                .option("engine.WarnInterpreterOnly", "false")
                .build()) {

            // local file to simplify referencing
            localCsso = new File(file.getParentFile(), "__csso.js");
            InputStream in = CssMinifier.class.getResourceAsStream("/csso.js");
            Files.write(localCsso.toPath(), in.readAllBytes());
            in.close();

            Value jsBindings = context.getBindings("js");
            jsBindings.putMember("css", Files.readString(file.toPath()));

            String options;
            if (sourceMaps) {

                options = "{ sourceMap: true, filename: '" + file.getName() + "'}";
            } else {
                options = "{}";
            }
            jsBindings.putMember("opts", options);

            String code = "import { minify } from '__csso.js';" +
                    "var res = minify(css, " + options + ");" +
                    "if (res.map) {" +
                    "   res.sourceMap = res.map.toString();" +
                    "}" +
                    "res;";

            Value res = context.eval(Source.newBuilder("js", code, "cssmin.mjs").build());
            String minified = res.getMember("css").asString();
            String sourceMapContent = res.getMember("sourceMap") != null ? res.getMember("sourceMap").asString() : null;

            if (sourceMapContent != null) {
                minified += "\n/*# sourceMappingURL=" + sourceMap.getName() + "*/";
                Files.writeString(sourceMap.toPath(), sourceMapContent, StandardCharsets.UTF_8);
                SourceMapUtils.includeSources(sourceMap);
            }

            Files.writeString(target.toPath(), minified, StandardCharsets.UTF_8);

            // remove original file
            System.out.println("Minified file source removed: " + file.getName());
            file.delete();

        } catch (IOException ex) {
            throw new IllegalStateException("Failed to minify", ex);
        } finally {
            // remove temporary lib
            if (localCsso != null && localCsso.exists()) {
                localCsso.delete();
            }
        }
        return new MinifyResult(target, sourceMaps ? sourceMap : null);
    }
}
