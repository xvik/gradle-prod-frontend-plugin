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
 * Css minification with <a href="https://github.com/css/csso">csso</a>.
 * <p>
 * Csso is a js tool, executed using <a href="https://www.graalvm.org/latest/security-guide/polyglot-sandbox/">graalvm
 * </a>, so no local nodejs required.
 * <p>
 * Csso js is bundled inside jar. The latest csso version could be loaded from
 * <a href="https://cdn.jsdelivr.net/npm/csso">cdn</a>.
 *
 * @author Vyacheslav Rusakov
 * @since 31.01.2023
 */
public class CssMinifier implements ResourceMinifier {

    /**
     * Csso tool name (copied nearby to processed file).
     */
    private static final String LOCAL_CSSO = "__csso.js";

    /**
     * Minify css file.
     *
     * @param file       file to minify
     * @param sourceMaps true to generate source map
     * @return minification result
     */
    @Override
    public MinifyResult minify(final File file, final boolean sourceMaps) {
        final File target = new File(file.getParentFile(), FileUtils.getMinName(file.getName()));
        final File sourceMap = sourceMaps ? new File(target.getAbsolutePath() + ".map") : null;

        File localCsso = null;
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (final Context context = Context.newBuilder("js")
                .currentWorkingDirectory(file.getParentFile().toPath())
                .out(output)
                .err(output)
                .allowIO(true)
                .option("engine.WarnInterpreterOnly", "false")
                .build()) {

            // local csso file to simplify referencing
            localCsso = copyCsso(target);

            final Value jsBindings = context.getBindings("js");
            // read entire source into variable because it would be loaded in any case - simpler to do in java
            jsBindings.putMember("css", Files.readString(file.toPath()));

            final String jsExecution = buildExecutionCode(file.getName(), sourceMaps);
            final Value res = context.eval(Source.newBuilder("js", jsExecution, "cssmin.mjs").build());

            writeFiles(target, sourceMap, res);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to minify css: " + file.getAbsolutePath() + "\n"
                    + formatOutput(output), ex);
        } finally {
            // remove temporary lib
            if (localCsso != null && localCsso.exists()) {
                localCsso.delete();
            }
        }
        return new MinifyResult(target, sourceMaps ? sourceMap : null, formatOutput(output));
    }

    private File copyCsso(final File target) throws IOException {
        final File localCsso = new File(target.getParentFile(), LOCAL_CSSO);
        InputStream in = CssMinifier.class.getResourceAsStream("/csso.js");
        Files.write(localCsso.toPath(), in.readAllBytes());
        in.close();
        return localCsso;
    }

    private String buildExecutionCode(final String sourceFileName, final boolean generateSourceMap) {
        String options;
        if (generateSourceMap) {
            options = "{ sourceMap: true, filename: '" + sourceFileName + "'}";
        } else {
            options = "{}";
        }

        return "import { minify } from '" + LOCAL_CSSO + "';" +
                "var res = minify(css, " + options + ");" +
                "if (res.map) {" +
                "   res.sourceMap = res.map.toString();" +
                "}" +
                "res;";
    }

    private String formatOutput(final ByteArrayOutputStream stream) {
        String out = stream.toString(StandardCharsets.UTF_8);
        if (out.isEmpty()) {
            out = Arrays.stream(out.split("\n"))
                    .map(s -> "\t" + s).collect(Collectors.joining("\n"));
        }
        return out;
    }

    private void writeFiles(final File target, final File sourceMap, final Value res) throws IOException {
        String minified = res.getMember("css").asString();
        final String sourceMapContent = res.getMember("sourceMap") != null ?
                res.getMember("sourceMap").asString() : null;

        if (sourceMapContent != null) {
            minified += "\n/*# sourceMappingURL=" + sourceMap.getName() + "*/";
            Files.writeString(sourceMap.toPath(), sourceMapContent, StandardCharsets.UTF_8);
        }

        Files.writeString(target.toPath(), minified, StandardCharsets.UTF_8);
    }
}
