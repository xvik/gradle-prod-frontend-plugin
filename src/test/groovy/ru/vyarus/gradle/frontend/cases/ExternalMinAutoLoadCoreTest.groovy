package ru.vyarus.gradle.frontend.cases

import ru.vyarus.gradle.frontend.core.info.HtmlInfo

/**
 * @author Vyacheslav Rusakov
 * @since 28.04.2023
 */
class ExternalMinAutoLoadCoreTest extends AbstractCoreTest {

    def "Check external minifiable resources"() {

        fileFromClasspath('webapp/index.html', '/cases/externalMinAutoLoad.html')

        when: "processing application"
        def res = run('webapp')

        then: "optimization done"
        res.getHtmls().size() == 1
        HtmlInfo html = res.getHtmls()[0]
        html.changes == ['changed links', 'minified']
        html.js.size() == 1
        with(html.js[0]) {
            remote
            changes.size() == 3
            // min version loaded directly
            !changes.contains('minification')
            changes.contains('integrity token applied')
            target.startsWith("js/vue.min.js?")
            element.attr('integrity').length() > 0
            gzip != null
            file != null
        }
        html.css.size() == 1
        with(html.css[0]) {
            remote
            changes.size() == 3
            // min version loaded directly
            !changes.contains('minification')
            changes.contains('integrity token applied')
            target.startsWith("css/materialdesignicons.min.css?")
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
        with(html2.js[0]) {
            !remote
            changes.size() == 0
//            changes.containsAll(['crossorigin removed', 'integrity removed', 'integrity token applied'])
//            target.startsWith("js/bootstrap.bundle.min.js?")
//            element.attr('integrity') != null
            gzip != null
            file != null
        }
        html2.css.size() == 1
        with(html2.css[0]) {
            !remote
            changes.size() == 0
//            changes.containsAll(['crossorigin removed', 'integrity removed', 'integrity token applied'])
//            target.startsWith("css/bootstrap.min.css?")
//            element.attr('integrity') != null
            gzip != null
            file != null
        }

        and: "index file not changed"
        htmlContent == file('webapp/index.html').text
    }
}
