package ru.vyarus.gradle.frontend.util;

import ru.vyarus.gradle.frontend.model.HtmlModel;
import ru.vyarus.gradle.frontend.model.OptimizedItem;
import ru.vyarus.gradle.frontend.model.file.CssModel;
import ru.vyarus.gradle.frontend.model.file.JsModel;
import ru.vyarus.gradle.frontend.model.file.RelativeCssResource;

/**
 * @author Vyacheslav Rusakov
 * @since 10.02.2023
 */
public class DebugReporter {

    public static String buildHtmlReport(HtmlModel html) {
        StringBuilder res = new StringBuilder(FileUtils.relative(html.getBaseDir(), html.getFile())).append("\n");
        for (JsModel js : html.getJs()) {
            res.append("\t").append(js.getTarget());
            appendIgnored(res, js);
        }
        for (CssModel css : html.getCss()) {
            res.append("\t").append(css.getTarget());
            appendIgnored(res, css);
            if (!css.getUrls().isEmpty()) {
                for (RelativeCssResource rel : css.getUrls()) {
                    res.append("\t\t").append(rel.getTarget());
                    appendIgnored(res, rel);
                }
            }
        }
        return res.toString();
    }

    private static void appendIgnored(final StringBuilder res, final OptimizedItem item) {
        if (item.isIgnored()) {
            res.append(" (").append(item.getIgnoreReason()).append(")");
        }
        res.append("\n");
    }
}
