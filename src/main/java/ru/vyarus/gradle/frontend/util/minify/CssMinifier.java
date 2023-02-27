package ru.vyarus.gradle.frontend.util.minify;

import com.google.common.css.ExitCodeHandler;
import com.google.common.css.IdentitySubstitutionMap;
import com.google.common.css.JobDescription;
import com.google.common.css.JobDescriptionBuilder;
import com.google.common.css.SourceCode;
import com.google.common.css.compiler.ClosureStylesheetCompiler;
import com.google.common.css.compiler.ast.BasicErrorManager;
import com.google.common.css.compiler.gssfunctions.DefaultGssFunctionMapProvider;
import ru.vyarus.gradle.frontend.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * https://github.com/616slayer616/closure-stylesheets
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

        final JobDescriptionBuilder builder = new JobDescriptionBuilder();
        builder.setOutputFormat(JobDescription.OutputFormat.COMPRESSED);
        builder.setAllowDefPropagation(true);
        builder.setAllowUnrecognizedFunctions(true);
        builder.setAllowUnrecognizedProperties(true);
        builder.setAllowKeyframes(true);
        builder.setAllowWebkitKeyframes(true);
        builder.setProcessDependencies(true);
        builder.setSimplifyCss(true);
        builder.setEliminateDeadStyles(false);
        builder.setCssSubstitutionMapProvider(IdentitySubstitutionMap::new);
        builder.setPreserveComments(false);
        builder.setGssFunctionMapProvider(new DefaultGssFunctionMapProvider());
        builder.setSourceMapLevel(JobDescription.SourceMapDetailLevel.DEFAULT);
        builder.setCreateSourceMap(sourceMaps);

        try {
            builder.addInput(new SourceCode(file.getName(), Files.readString(file.toPath())));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read css: " + file.getAbsolutePath(), e);
        }

        final JobDescription job = builder.getJobDescription();
        final ExitCodeHandler exitHandler = exitCode -> {
            if (exitCode != 0) {
                throw new IllegalStateException("Failed to minify css");
            }
        };
        final BasicErrorManager errorManager = new BasicErrorManager() {
            @Override
            public void print(String msg) {
                // write errors to console
                System.out.println(msg);
            }
        };
        final ClosureStylesheetCompiler compiler = new ClosureStylesheetCompiler(job, exitHandler, errorManager);

        String output = compiler.execute(target, sourceMap);
        if (sourceMaps) {
            output += "\n/*# sourceMappingURL=" + sourceMap.getName() + "*/";
        }
        FileUtils.writeFile(target, output);
        return new MinifyResult(target, sourceMaps ? sourceMap : null);
    }
}
