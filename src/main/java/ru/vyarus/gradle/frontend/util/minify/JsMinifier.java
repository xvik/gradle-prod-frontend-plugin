package ru.vyarus.gradle.frontend.util.minify;

import com.google.javascript.jscomp.AbstractCommandLineRunner;
import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.Result;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.WarningLevel;
import com.google.javascript.jscomp.deps.ModuleLoader;
import ru.vyarus.gradle.frontend.util.FileUtils;

import java.io.File;
import java.io.IOException;
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
        // without source maps enabled writing into the same file
        String name = file.getName();
        if (sourceMaps) {
            // if source maps used preserving original file
            name = FileUtils.getMinName(name);
        }
        final File target = new File(file.getParentFile(), name);
        final File sourceMap = new File(target.getAbsolutePath() + ".map");

        final Compiler compiler = new Compiler();
        final CompilerOptions options = new CompilerOptions();
        options.setEnvironment(CompilerOptions.Environment.BROWSER);

        options.setStrictModeInput(false);
        options.setEmitUseStrict(false);

        final CompilationLevel level = CompilationLevel.SIMPLE_OPTIMIZATIONS;
        level.setOptionsForCompilationLevel(options);
//        level.setDebugOptionsForCompilationLevel(options);

        // files compiled separately - no way to properly apply
        options.setRemoveDeadCode(false);

        WarningLevel.DEFAULT.setOptionsForWarningLevel(options);

        options.setModuleResolutionMode(ModuleLoader.ResolutionMode.BROWSER);
        options.setProcessCommonJSModules(true);
        options.setBrowserFeaturesetYear(2022);

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
            result.warnings.forEach(warn -> System.out.println("ERROR: at " + warn.getSourceName() + ":"
                    + warn.getLineNumber() + ":" + warn.getCharno() + "\n\t" + warn.getDescription()));
            result.errors.forEach(err -> System.out.println("ERROR: at " + err.getSourceName() + ":"
                    + err.getLineNumber() + ":" + err.getCharno() + "\n\t" + err.getDescription()));
            throw new IllegalStateException("Failed to minify js: " + file.getAbsolutePath());
        }
        return new MinifyResult(target, sourceMaps ? sourceMap : null);
    }
}
