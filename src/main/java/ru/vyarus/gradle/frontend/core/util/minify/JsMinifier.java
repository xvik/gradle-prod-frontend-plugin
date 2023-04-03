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
 * https://github.com/google/closure-compiler
 *
 * @author Vyacheslav Rusakov
 * @since 31.01.2023
 */
public class JsMinifier implements ResourceMinifier {

    @Override
    public MinifyResult minify(final File file, final boolean sourceMaps) {
        String name = file.getName();
        // if source maps used preserving original file
        name = FileUtils.getMinName(name);
        final File target = new File(file.getParentFile(), name);
        final File sourceMap = new File(target.getAbsolutePath() + ".map");

        final Compiler compiler = new Compiler();
        // hide errors from log (log manually from result)
        ErrorManager errors = new ErrorManager(compiler);
        compiler.setErrorManager(errors);
        final CompilerOptions options = new CompilerOptions();
        options.setEnvironment(CompilerOptions.Environment.BROWSER);

        // no checks to avoid warnings - the task is to minify not validate
        options.setStrictModeInput(false);
        options.setCheckSuspiciousCode(false);
        options.setEmitUseStrict(false);

        final CompilationLevel level = CompilationLevel.SIMPLE_OPTIMIZATIONS;
        level.setOptionsForCompilationLevel(options);
//        level.setDebugOptionsForCompilationLevel(options);

        // files compiled separately - no way to properly apply
        options.setRemoveDeadCode(false);

        WarningLevel.DEFAULT.setOptionsForWarningLevel(options);

        options.setModuleResolutionMode(ModuleLoader.ResolutionMode.BROWSER);
        options.setProcessCommonJSModules(false);

        if (sourceMaps) {
            options.setSourceMapOutputPath(sourceMap.getAbsolutePath());
            // avoid absolute paths in source map
            options.setSourceMapLocationMappings(List.of(
                    new SourceMap.PrefixLocationMapping(file.getParentFile().getAbsolutePath() + "/", "")));
        }

        final List<SourceFile> externs;
        try {
            externs = AbstractCommandLineRunner.getBuiltinExterns(options.getEnvironment());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to prepare built-in externs for closure compiler", e);
        }
        final Result result = compiler.compile(
                externs, Collections.singletonList(SourceFile.fromFile(file.getAbsolutePath())), options);

        String extraLog = String.join("\n", errors.getMessages());
        if (!extraLog.isEmpty()) {
            extraLog = Arrays.stream(extraLog.split("\n")).map(s -> "\t" + s).collect(Collectors.joining("\n"));
        }

        if (result.success) {
            String content = compiler.toSource();

            if (sourceMaps) {
                StringBuilder sm = new StringBuilder();
                try {
                    result.sourceMap.appendTo(sm, target.getName());
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to generate source maps", e);
                }
                FileUtils.writeFile(sourceMap, sm.toString());
                content += "\n//# sourceMappingURL=" + sourceMap.getName();
            }

            FileUtils.writeFile(target, content);
        } else {
            throw new IllegalStateException("Failed to minify js: " + file.getAbsolutePath() + "\n" + extraLog);
        }
        return new MinifyResult(target, sourceMaps ? sourceMap : null, extraLog.isEmpty() ? null : extraLog);
    }

    private static class ErrorManager extends BasicErrorManager {
        private final MessageFormatter formatter;
        private final List<String> messages = new ArrayList<>();

        public ErrorManager(Compiler compiler) {
            formatter = new LightweightMessageFormatter(compiler);
        }

        @Override
        public void println(CheckLevel level, JSError error) {
            if (level != CheckLevel.OFF) {
                final String msg = error.format(level, formatter);
                messages.add(msg);
            }
        }

        @Override
        protected void printSummary() {
        }

        public List<String> getMessages() {
            return messages;
        }
    }
}
