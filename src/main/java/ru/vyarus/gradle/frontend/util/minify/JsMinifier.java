package ru.vyarus.gradle.frontend.util.minify;

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
import com.google.javascript.jscomp.WarningLevel;
import com.google.javascript.jscomp.deps.ModuleLoader;
import ru.vyarus.gradle.frontend.util.FileUtils;
import ru.vyarus.gradle.frontend.util.SourceMapUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * https://github.com/google/closure-compiler
 *
 * @author Vyacheslav Rusakov
 * @since 31.01.2023
 */
public final class JsMinifier {
    private JsMinifier() {
    }

    public static MinifyResult minify(final File file, final boolean sourceMaps) {
        // todo unify logging with upper level (add debug option for additional logs)
        System.out.println("Minifying " + file.getName() + " (" + (sourceMaps ? "with" : "no") + " source maps)");
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
        }

        final List<SourceFile> externs;
        try {
            externs = AbstractCommandLineRunner.getBuiltinExterns(options.getEnvironment());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to prepare built-in externs for closure compiler", e);
        }
        final Result result = compiler.compile(
                externs, Collections.singletonList(SourceFile.fromFile(file.getAbsolutePath())), options);

        if (!errors.getMessages().isEmpty()) {
            for(String msg: errors.getMessages()) {
                System.out.println(msg);
            }
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
                SourceMapUtils.includeSources(sourceMap);
                content += "\n//# sourceMappingURL=" + sourceMap.getName();
            }

            FileUtils.writeFile(target, content);
            System.out.println("-------------------\n"+content+"\n------------------------");

            // remove original file
            System.out.println("Minified file source removed: " + file.getName());
            file.delete();
        } else {
            throw new IllegalStateException("Failed to minify js: " + file.getAbsolutePath());
        }
        return new MinifyResult(target, sourceMaps ? sourceMap : null);
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
