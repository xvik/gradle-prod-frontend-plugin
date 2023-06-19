package ru.vyarus.gradle.frontend.core.util.minify;

import com.google.javascript.jscomp.AbstractCommandLineRunner;
import com.google.javascript.jscomp.BasicErrorManager;
import com.google.javascript.jscomp.CheckLevel;
import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.JSError;
import com.google.javascript.jscomp.LightweightMessageFormatter;
import com.google.javascript.jscomp.MessageFormatter;
import com.google.javascript.jscomp.Result;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.SourceMap;
import com.google.javascript.jscomp.WarningLevel;
import com.google.javascript.jscomp.deps.ModuleLoader;
import ru.vyarus.gradle.frontend.core.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Js minification with <a href="https://github.com/google/closure-compiler">closure-compiler</a>.
 *
 * @author Vyacheslav Rusakov
 * @since 31.01.2023
 */
public class JsMinifier implements ResourceMinifier {

    /**
     * Minify js file.
     *
     * @param file       file to minify
     * @param sourceMaps true to generate source map
     * @return minification result
     */
    @Override
    public MinifyResult minify(final File file, final boolean sourceMaps) {
        final File target = new File(file.getParentFile(), FileUtils.getMinName(file.getName()));
        final File sourceMap = sourceMaps ? new File(target.getAbsolutePath() + ".map") : null;

        final Compiler compiler = new Compiler();
        // hide errors from log (log manually from result)
        final ErrorManager errors = new ErrorManager(compiler);
        compiler.setErrorManager(errors);
        final CompilerOptions options = buildOptions(target, sourceMap);
        final List<SourceFile> externs = buildExterns(options);

        final Result result = compiler.compile(
                externs, Collections.singletonList(SourceFile.fromFile(file.getAbsolutePath())), options);

        final String errorsLog = prepareErrorsLog(errors.getMessages());

        if (result.success) {
            writeFiles(target, sourceMap, compiler.toSource(), result);
        } else {
            throw new IllegalStateException("Failed to minify js: " + file.getAbsolutePath() + "\n" + errorsLog);
        }
        return new MinifyResult(target, sourceMap, errorsLog.isEmpty() ? null : errorsLog);
    }

    private CompilerOptions buildOptions(final File target, final File sourceMap) {
        final CompilerOptions options = new CompilerOptions();
        options.setEnvironment(CompilerOptions.Environment.BROWSER);

        // no checks to avoid warnings - the task is to minify not validate
        options.setStrictModeInput(false);
        options.setCheckSuspiciousCode(false);
        options.setEmitUseStrict(false);

        final CompilationLevel level = CompilationLevel.SIMPLE_OPTIMIZATIONS;
        level.setOptionsForCompilationLevel(options);
        // level.setDebugOptionsForCompilationLevel(options);

        // files compiled separately - no way to properly apply
        options.setRemoveDeadCode(false);

        WarningLevel.DEFAULT.setOptionsForWarningLevel(options);

        options.setModuleResolutionMode(ModuleLoader.ResolutionMode.BROWSER);
        options.setProcessCommonJSModules(false);

        if (sourceMap != null) {
            options.setSourceMapOutputPath(sourceMap.getAbsolutePath());
            // avoid absolute paths in source map
            options.setSourceMapLocationMappings(List.of(
                    new SourceMap.PrefixLocationMapping(target.getParentFile().getAbsolutePath() + "/", "")));
        }

        return options;
    }

    private List<SourceFile> buildExterns(final CompilerOptions options) {
        try {
            return AbstractCommandLineRunner.getBuiltinExterns(options.getEnvironment());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to prepare built-in externs for closure compiler", e);
        }
    }

    private String prepareErrorsLog(final List<String> errors) {
        String extraLog = String.join("\n", errors);
        if (!extraLog.isEmpty()) {
            extraLog = Arrays.stream(extraLog.split("\n")).map(s -> "\t" + s).collect(Collectors.joining("\n"));
        }
        return extraLog;
    }

    private void writeFiles(final File target,
                            final File sourceMap,
                            final String minified,
                            final Result minificationData) {
        String content = minified;
        if (sourceMap != null) {
            final StringBuilder sm = new StringBuilder();
            try {
                minificationData.sourceMap.appendTo(sm, target.getName());
            } catch (IOException e) {
                throw new IllegalStateException("Failed to generate source maps", e);
            }
            FileUtils.writeFile(sourceMap, sm.toString());
            content += "\n//# sourceMappingURL=" + sourceMap.getName();
        }
        FileUtils.writeFile(target, content);
    }

    /**
     * Custom errors manager to avoid "leaking" errors directly in output. Instead, all messages are aggregated
     * to be appended later into error message.
     */
    private static class ErrorManager extends BasicErrorManager {
        private final MessageFormatter formatter;
        private final List<String> messages = new ArrayList<>();

        ErrorManager(final Compiler compiler) {
            formatter = new LightweightMessageFormatter(compiler);
        }

        @Override
        public void println(final CheckLevel level, final JSError error) {
            if (level != CheckLevel.OFF) {
                final String msg = error.format(level, formatter);
                messages.add(msg);
            }
        }

        @Override
        protected void printSummary() {
            // no need
        }

        public List<String> getMessages() {
            return messages;
        }
    }
}
