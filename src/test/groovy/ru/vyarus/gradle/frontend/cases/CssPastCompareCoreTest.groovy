package ru.vyarus.gradle.frontend.cases

import ru.vyarus.gradle.frontend.core.info.resources.HtmlInfo

/**
 * @author Vyacheslav Rusakov
 * @since 23.06.2023
 */
class CssPastCompareCoreTest extends AbstractCoreTest {

    def "Check modified css duplicates detection"() {

        fileFromClasspath('webapp/index.html', '/cases/cssFonts.html')

        when: "processing bootstrap application"
        def res = run('webapp')

        then: "optimization done"
        res.getHtmls().size() == 1
        HtmlInfo html = res.getHtmls()[0]
        html.changes == ['changed links', 'minified']
        html.css.size() == 1
        with(html.css[0]) {
            remote
            changes.size() == 3
            changes.containsAll(['integrity token applied'])
            target.startsWith("css/materialdesignicons.min.css?")
        }

        when: "running again with remote links and downloaded files "
        // IMPORTANT: html must contain REMOTE links so resources would be downloaded again
        // for css it would be different file because of further optimizations, but AFTER optimizations
        // duplicate MUST still be detected
        fileFromClasspath('webapp/index.html', '/cases/cssFonts.html')
        res = run('webapp')

        then: "css duplicate finally detected"
        HtmlInfo html2 = res.getHtmls()[0]
        html2.css.size() == 1
        with(html2.css[0]) {
            remote
            target.startsWith("css/materialdesignicons.min.css?")
            !file.getName().contains(".1.")
        }

    }
}
