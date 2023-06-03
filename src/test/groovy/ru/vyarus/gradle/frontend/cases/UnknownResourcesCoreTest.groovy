package ru.vyarus.gradle.frontend.cases

import ru.vyarus.gradle.frontend.core.info.resources.HtmlInfo

/**
 * @author Vyacheslav Rusakov
 * @since 02.05.2023
 */
class UnknownResourcesCoreTest extends AbstractCoreTest {

    def "Check unknown files support"() {

        fileFromClasspath('webapp/index.html', '/cases/unknown.html')

        when: "processing"
        def res = run('webapp')

        then: "optimization done"
        res.getHtmls().size() == 1
        HtmlInfo html = res.getHtmls()[0]
        html.changes == ['minified']
        html.js.size() == 1
        with(html.js[0]) {
            ignored
            changes.size() == 0
            file != null
            !file.exists()
            gzip == null
        }
        html.css.size() == 1
        with(html.css[0]) {
            ignored
            changes.size() == 0
            file != null
            !file.exists()
            gzip == null
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
            ignored
            changes.size() == 0
        }
        html2.css.size() == 1
        with(html2.css[0]) {
            ignored
            changes.size() == 0
        }

        and: "index file not changed"
        htmlContent == file('webapp/index.html').text
    }
}
