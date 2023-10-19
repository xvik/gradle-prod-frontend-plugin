package ru.vyarus.gradle.frontend.cases

import ru.vyarus.gradle.frontend.core.info.resources.HtmlInfo

/**
 * Case when remote link does not contain extension, but link redirects to actual file with extension.
 *
 * @author Vyacheslav Rusakov
 * @since 02.05.2023
 */
class NoExtensionCoreTest extends AbstractCoreTest {

    def "Check js without extension support"() {

        fileFromClasspath('webapp/index.html', '/cases/noJsExtension.html')

        when: "processing"
        def res = run('webapp')

        then: "optimization done"
        res.getHtmls().size() == 1
        HtmlInfo html = res.getHtmls()[0]
        html.changes == ['changed links', 'minified']
        html.js.size() == 1
        html.css.size() == 0
        with(html.js[0]) {
            remote
            changes.size() == 3
            changes.containsAll(['integrity token applied'])
            target.startsWith("js/vue.min.js?")
            element.attr('integrity') != null
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
        html2.css.size() == 0
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
