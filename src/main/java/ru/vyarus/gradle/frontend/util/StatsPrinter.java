package ru.vyarus.gradle.frontend.util;

import org.apache.commons.io.FileUtils;
import ru.vyarus.gradle.frontend.model.HtmlModel;
import ru.vyarus.gradle.frontend.model.OptimizationModel;
import ru.vyarus.gradle.frontend.model.OptimizedItem;
import ru.vyarus.gradle.frontend.model.file.CssModel;
import ru.vyarus.gradle.frontend.model.file.JsModel;
import ru.vyarus.gradle.frontend.model.file.RelativeCssResource;
import ru.vyarus.gradle.frontend.model.stat.Stat;

import java.io.File;
import java.util.Arrays;
import java.util.Map;

/**
 * @author Vyacheslav Rusakov
 * @since 02.02.2023
 */
public final class StatsPrinter {

    private StatsPrinter() {
    }

    public static String print(final OptimizationModel model) {
        final File baseDir = model.getBaseDir();
        final String basePath = baseDir.getAbsolutePath() + "/";
        final String line = repeat('-', 70 + 15 * 3 + 1) + "\n";
        final String sumLine = repeat('-', 15 * 3) + "\n";
        final StringBuilder res = new StringBuilder("\n");
        if (!model.getHtmls().isEmpty()) {
            res.append(String.format("%-50s %-15s%-15s%-15s%n", "", "original", "minified", "gzipped"));
            res.append(line);
        }
        for (HtmlModel html : model.getHtmls()) {
            if (!html.isChanged()) {
                continue;
            }
            res.append(String.format("%-70s %s%n",
                    html.getFile().getAbsolutePath().replace(basePath, ""), formatSizes(html)));
            String htmlPath = html.getFile().getParentFile().getAbsolutePath() + "/";
            for (JsModel js : html.getJs()) {
                res.append(String.format("%-70s %s%n",
                        "  " + unhash(js.getTarget()), formatSizes(js)));
            }
            for (CssModel css : html.getCss()) {
                res.append(String.format("%-70s %s%n",
                        "  " + unhash(css.getTarget()), formatSizes(css)));
                for (RelativeCssResource resource : css.getUrls()) {
                    res.append(String.format("%-70s   %s%n",
                            "    " + unhash(resource.getTarget()), formatSizes(resource)));
                }
            }

            if (!html.getCss().isEmpty() || html.getJs().isEmpty()) {
                res.append(String.format("%-70s %s", "", sumLine));
                res.append(String.format("%-70s %-15s%-15s%-15s%n", "",
                        sum(html, Stat.ORIGINAL), sum(html, Stat.MODIFIED), sum(html, Stat.GZIP)));
            }
        }
        return res.toString();
    }

    private static String formatSizes(final OptimizedItem file) {
        return formatSizes(file.getStats(), Stat.ORIGINAL, Stat.MODIFIED, Stat.GZIP);
    }

    private static String formatSizes(final Map<Stat, Long> stats, final Stat... sequence) {
        StringBuilder res = new StringBuilder();
        for (Stat stat : sequence) {
            // use whitespace to keep table columns for files without minifiction
            res.append(String.format("%-15s", stats.containsKey(stat)
                    ? FileUtils.byteCountToDisplaySize(stats.get(stat)) : ""));
        }
        if (res.length() == 0) {
            res.append("UNKNOWN");
        }
        return res.toString();
    }

    private static String sum(final HtmlModel html, final Stat stat) {
        long res = getStat(html, stat);
        for (CssModel css : html.getCss()) {
            // avoid ignored
            if (css.getStats().containsKey(Stat.ORIGINAL)) {
                res += getStat(css, stat);
            }
        }
        for (JsModel js : html.getJs()) {
            // avoid ignored
            if (js.getStats().containsKey(Stat.ORIGINAL)) {
                res += getStat(js, stat);
            }
        }
        return String.format("%-15s", FileUtils.byteCountToDisplaySize(res));
    }

    private static long getStat(OptimizedItem item, final Stat stat) {
        Stat target = stat;
        // going backward for stats because something might be missing (e.g. minification if file already minimized)
        while (!item.getStats().containsKey(target)) {
            target = Stat.values()[target.ordinal() - 1];
        }
        return item.getStats().get(target);
    }

    /**
     * @param length number of whitespace characters to aggregate
     * @return string with specified number of whitespace characters
     */
    private static String repeat(final char what, final int length) {
        String res = "";
        if (length > 0) {
            final char[] dash = new char[length];
            Arrays.fill(dash, what);
            res = String.valueOf(dash);
        }
        return res;
    }

    private static String unhash(final String path) {
        int idx = path.indexOf('?');
        return idx > 0 ? path.substring(0, idx) : path;
    }
}
