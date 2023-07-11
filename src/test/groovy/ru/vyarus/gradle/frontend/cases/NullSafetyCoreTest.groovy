package ru.vyarus.gradle.frontend.cases

import ru.vyarus.gradle.frontend.core.info.resources.HtmlInfo

/**
 * @author Vyacheslav Rusakov
 * @since 10.07.2023
 */
class NullSafetyCoreTest extends AbstractCoreTest {

    def "Check null safety"() {

        fileFromClasspath('webapp/index.html', '/cases/bootstrap.html')

        when: "processing bootstrap application"
        def res = run(builder('webapp')
                .jsDir((File) null)
                .jsDir((String) null)
                .cssDir((File) null)
                .cssDir((String) null)
                .downloadResources(null)
                .preferMinDownload(null)
                .downloadSourceMaps(null)
                .minifyCss(null)
                .minifyJs(null)
                .minifyHtml(null)
                .minifyHtmlCss(null)
                .minifyHtmlJs(null)
                .applyAntiCache(null)
                .applyIntegrity(null)
                .htmlExtensions(null)
                .htmlExtensions([])
                .generateSourceMaps(null)
                .gzip(null)
                .debug(null))

        then: "optimization done"
        res.getHtmls().size() == 1
        HtmlInfo html = res.getHtmls()[0]
        html.changes == ['changed links', 'minified']
        html.js.size() == 1
        html.pureHtml
        html.parsedDocument != null
        with(html.js[0]) {
            remote
            changes.size() == 5
            changes.containsAll(['crossorigin removed', 'integrity removed', 'integrity token applied'])
            target.startsWith("js/bootstrap.bundle.min.js?")
            element.attr('integrity').length() > 0
            element.attr('crossorigin').length() == 0
            gzip != null
            file != null
            sourceMap != null
        }
        html.css.size() == 1
        with(html.css[0]) {
            remote
            changes.size() == 5
            changes.containsAll(['crossorigin removed', 'integrity removed', 'integrity token applied'])
            target.startsWith("css/bootstrap.min.css?")
            element.attr('integrity').length() > 0
            element.attr('crossorigin').length() == 0
            gzip != null
            file != null
            sourceMap != null
        }
    }
}
