package ru.vyarus.gradle.frontend.core.util.minify;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import ru.vyarus.gradle.frontend.core.util.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * https://github.com/css/csso
 * latest csso version could be loaded from https://cdn.jsdelivr.net/npm/csso
 *
 * @author Vyacheslav Rusakov
 * @since 31.01.2023
 */
public class CssMinifier implements ResourceMinifier {

    @Override
    public MinifyResult minify(final File file, final boolean sourceMaps) {
        String name = file.getName();
        name = FileUtils.getMinName(name);
        final File target = new File(file.getParentFile(), name);
        final File sourceMap = new File(target.getAbsolutePath() + ".map");

        File localCsso = null;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (Context context = Context.newBuilder("js")
                .currentWorkingDirectory(file.getParentFile().toPath())
                .out(output)
                .err(output)
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
            }

            Files.writeString(target.toPath(), minified, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            String out = output.toString(StandardCharsets.UTF_8);
            if (out.isEmpty()) {
                out = Arrays.stream(out.split("\n"))
                        .map(s -> "\t" + s).collect(Collectors.joining("\n"));
            }
            throw new IllegalStateException("Failed to minify css: " + file.getAbsolutePath() + "\n" + out, ex);
        } finally {
            // remove temporary lib
            if (localCsso != null && localCsso.exists()) {
                localCsso.delete();
            }
        }
        String out = output.toString(StandardCharsets.UTF_8);
        if (out.isEmpty()) {
            out = Arrays.stream(out.split("\n"))
                    .map(s -> "\t" + s).collect(Collectors.joining("\n"));
        }
        return new MinifyResult(target, sourceMaps ? sourceMap : null, out);
    }
}
