package ru.vyarus.gradle.frontend.cases

import ru.vyarus.gradle.frontend.core.info.resources.HtmlInfo

/**
 * @author Vyacheslav Rusakov
 * @since 03.05.2023
 */
class ManualJsMinCoreTest extends AbstractCoreTest {

    def "Check manual js minification"() {

        fileFromClasspath('webapp/index.html', '/cases/jsManualMin/jsManualMin.html')
        fileFromClasspath('webapp/vue.js', '/cases/jsManualMin/vue.js')

        when: "processing"
        def res = run('webapp')

        then: "optimization done"
        res.getHtmls().size() == 1
        HtmlInfo html = res.getHtmls()[0]
        html.changes == ['changed links', 'minified']
        html.js.size() == 1
        html.css.size() == 0
        with(html.js[0]) {
            !remote
            changes.size() == 5
            changes.containsAll(['minified', 'integrity token applied', 'source map generated: vue.min.js.map'])
            target.startsWith("vue.min.js?")
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
        html2.getJs().size() == 1
        html2.getCss().size() == 0
        with(html2.js[0]) {
            !remote
            changes.size() == 0
            gzip != null
            file != null
        }

        and: "index file not changed"
        htmlContent == file('webapp/index.html').text
    }
}
