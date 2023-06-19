package ru.vyarus.gradle.frontend.core.util;

import ru.vyarus.gradle.frontend.core.info.resources.OptimizedEntityInfo;
import ru.vyarus.gradle.frontend.core.info.resources.root.sub.SubResourceInfo;
import ru.vyarus.gradle.frontend.core.model.HtmlPage;
import ru.vyarus.gradle.frontend.core.model.root.CssResource;
import ru.vyarus.gradle.frontend.core.model.root.JsResource;

/**
 * Debug output reporter. Used to print intermediate optimization state.
 *
 * @author Vyacheslav Rusakov
 * @since 10.02.2023
 */
public final class DebugReporter {

    private DebugReporter() {
    }

    /**
     * Prints detected resources for html page.
     *
     * @param html html page object
     * @return html file and related resources report
     */
    public static String buildReport(final HtmlPage html) {
        final StringBuilder res = new StringBuilder(FileUtils.relative(html.getBaseDir(), html.getFile())).append('\n');
        for (JsResource js : html.getJs()) {
            res.append('\t').append(js.getTarget());
            appendIgnored(res, js);
        }
        for (CssResource css : html.getCss()) {
            res.append('\t').append(css.getTarget());
            appendIgnored(res, css);
            if (!css.getSubResources().isEmpty()) {
                for (SubResourceInfo rel : css.getSubResources()) {
                    res.append("\t\t").append(rel.getTarget());
                    appendIgnored(res, rel);
                }
            }
        }
        return res.toString();
    }

    private static void appendIgnored(final StringBuilder res, final OptimizedEntityInfo item) {
        if (item.isIgnored()) {
            res.append(" (").append(item.getIgnoreReason()).append(')');
        }
        res.append('\n');
    }
}
