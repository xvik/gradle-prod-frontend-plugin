package ru.vyarus.gradle.frontend.cases

import ru.vyarus.gradle.frontend.core.info.resources.HtmlInfo

/**
 * @author Vyacheslav Rusakov
 * @since 02.05.2023
 */
class ManualCssMinCoreTest extends AbstractCoreTest {

    def "Check manual css minification"() {

        fileFromClasspath('webapp/index.html', '/cases/cssManualMin/cssManualMin.html')
        fileFromClasspath('webapp/materialdesignicons.css', '/cases/cssManualMin/materialdesignicons.css')

        when: "processing"
        def res = run('webapp')

        then: "optimization done"
        res.getHtmls().size() == 1
        HtmlInfo html = res.getHtmls()[0]
        html.changes == ['changed links', 'minified']
        html.js.size() == 0
        html.css.size() == 1
        with(html.css[0]) {
            !remote
            changes.size() == 5
            changes.containsAll(['minified', 'integrity token applied', 'source map generated: materialdesignicons.min.css.map'])
            target.startsWith("materialdesignicons.min.css?")
            element.attr('integrity').length() > 0
            gzip != null
            file != null
        }

        when: "running on already processed"
        String htmlContent = file('webapp/index.html').text
        res = run('webapp')

        then: "no actions performed"
        res.getHtmls().size() == 1
        HtmlInfo html2 = res.getHtmls()[0]
        html2.changes.isEmpty()
        html2.getJs().size() == 0
        html2.getCss().size() == 1
        with(html2.css[0]) {
            !remote
            changes.size() == 0
            gzip != null
            file != null
        }

        and: "index file not changed"
        htmlContent == file('webapp/index.html').text
    }
}
