package ru.vyarus.gradle.frontend.core.util;

import org.apache.commons.io.FileUtils;
import ru.vyarus.gradle.frontend.core.info.OptimizationInfo;
import ru.vyarus.gradle.frontend.core.info.resources.HtmlInfo;
import ru.vyarus.gradle.frontend.core.info.resources.OptimizedEntityInfo;
import ru.vyarus.gradle.frontend.core.info.resources.root.ResourceInfo;
import ru.vyarus.gradle.frontend.core.info.resources.root.sub.SubResourceInfo;
import ru.vyarus.gradle.frontend.core.info.SizeType;

import java.io.File;
import java.util.Arrays;
import java.util.Map;

/**
 * Optimization statistics printer.
 *
 * @author Vyacheslav Rusakov
 * @since 02.02.2023
 */
public final class StatsPrinter {

    private StatsPrinter() {
    }

    /**
     * Print optimization stats table.
     *
     * @param result optimization info
     * @return rendered stats table
     */
    @SuppressWarnings("checkstyle:MultipleStringLiterals")
    public static String print(final OptimizationInfo result) {
        final File baseDir = result.getSettings().getBaseDir();
        final String basePath = baseDir.getAbsolutePath() + "/";
        final String line = repeat('-', 70 + 15 * 3 + 1) + "\n";
        final String sumLine = repeat('-', 15 * 3) + "\n";
        final StringBuilder res = new StringBuilder("\n");
        if (!result.getHtmls().isEmpty()) {
            res.append(String.format("%-70s %-15s%-15s%-15s%n", "", "original", "minified", "gzipped"));
            res.append(line);
        }
        for (HtmlInfo html : result.getHtmls()) {
            res.append(String.format("%-70s %s%n",
                    html.getFile().getAbsolutePath().replace(basePath, ""), formatSizes(html)));
            final boolean debug = result.getSettings().isDebug();
            writeChanges(debug, html, "", res);

            for (ResourceInfo js : html.getJs()) {
                res.append(String.format("%-70s %s%n", "  " + UrlUtils.clearParams(js.getTarget()), formatSizes(js)));
                writeChanges(debug, js, "  ", res);
            }
            for (ResourceInfo css : html.getCss()) {
                res.append(String.format("%-70s %s%n", "  " + UrlUtils.clearParams(css.getTarget()), formatSizes(css)));
                writeChanges(debug, css, "  ", res);
                for (SubResourceInfo resource : css.getSubResources()) {
                    res.append(String.format("%-70s   %s%n",
                            "    " + UrlUtils.clearParams(resource.getTarget()), formatSizes(resource)));
                    writeChanges(debug, resource, "    ", res);
                }
            }

            if (!html.getCss().isEmpty() || html.getJs().isEmpty()) {
                res.append(String.format("%-70s %s", "", sumLine));
                res.append(String.format("%-70s %-15s%-15s%-15s%n", "",
                        sum(html, SizeType.ORIGINAL), sum(html, SizeType.MODIFIED), sum(html, SizeType.GZIPPED)));
            }
        }
        return res.toString();
    }

    private static String formatSizes(final OptimizedEntityInfo file) {
        if (file.isIgnored()) {
            return file.getIgnoreReason();
        }
        return formatSizes(file.getStats(), SizeType.ORIGINAL, SizeType.MODIFIED, SizeType.GZIPPED);
    }

    private static String formatSizes(final Map<SizeType, Long> stats, final SizeType... sequence) {
        final StringBuilder res = new StringBuilder();
        for (SizeType stat : sequence) {
            // use whitespace to keep table columns for files without minifiction
            res.append(String.format("%-15s", stats.containsKey(stat)
                    ? FileUtils.byteCountToDisplaySize(stats.get(stat)) : ""));
        }
        return res.toString();
    }

    private static String sum(final HtmlInfo html, final SizeType stat) {
        long res = getStat(html, stat);
        for (ResourceInfo css : html.getCss()) {
            // avoid ignored
            if (css.getStats().containsKey(SizeType.ORIGINAL)) {
                res += getStat(css, stat);
            }
        }
        for (ResourceInfo js : html.getJs()) {
            // avoid ignored
            if (js.getStats().containsKey(SizeType.ORIGINAL)) {
                res += getStat(js, stat);
            }
        }
        return String.format("%-15s", FileUtils.byteCountToDisplaySize(res));
    }

    private static long getStat(final OptimizedEntityInfo item, final SizeType stat) {
        SizeType target = stat;
        // going backward for stats because something might be missing (e.g. minification if file already minimized)
        while (!item.getStats().containsKey(target)) {
            target = SizeType.values()[target.ordinal() - 1];
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

    private static void writeChanges(final boolean debug,
                                     final OptimizedEntityInfo item,
                                     final String prefix,
                                     final StringBuilder res) {
        if (debug && item.hasChanges()) {
            res.append(prefix).append("| changes:\n");
            item.getChanges().forEach(s -> res.append(prefix).append("| \t").append(s).append('\n'));
            // extra line to separate from the following items
            res.append('\n');
        }
    }
}
