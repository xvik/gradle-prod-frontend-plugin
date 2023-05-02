package ru.vyarus.gradle.frontend.cases

import ru.vyarus.gradle.frontend.core.info.HtmlInfo

/**
 * Jsdeliver auto-generates min files and also source maps, but link to source map points to server root!
 *
 * @author Vyacheslav Rusakov
 * @since 02.05.2023
 */
class JsdelivrMinCoreTest extends AbstractCoreTest {

    def "Check jsdelivr maps support"() {

        fileFromClasspath('webapp/index.html', '/cases/jsDeliverMinCase.html')

        when: "processing"
        def res = run('webapp')

        then: "optimization done"
        res.getHtmls().size() == 1
        HtmlInfo html = res.getHtmls()[0]
        html.changes == ['changed links', 'minified']
        html.js.size() == 0
        html.css.size() == 1
        with(html.css[0]) {
            remote
            changes.size() == 3
            changes.containsAll(['integrity token applied'])
            target.startsWith("css/bootstrap-icons.min.css?")
            element.attr('integrity') != null
            gzip != null
            file != null
            // one duplicate resource, but with different source url
            getSubResources().size() == 2
            getSubResources()[0].target.startsWith('fonts/bootstrap-icons.woff2?')
        }

        when: "running on already processed"
        String htmlContent = file('webapp/index.html').text
        res = run('webapp')

        then: "no actions performed"
        res.getHtmls().size() == 1
        HtmlInfo html2 = res.getHtmls()[0]
        html2.changes.isEmpty()
        html2.getJs().size() == 0
        html2.css.size() == 1
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
