package ru.vyarus.gradle.frontend.cases

import ru.vyarus.gradle.frontend.core.info.resources.HtmlInfo

/**
 * @author Vyacheslav Rusakov
 * @since 27.08.2025
 */
class BuefyInvalidCssUrlTest extends AbstractCoreTest {

    def "Check invalid css urls support"() {

        fileFromClasspath('webapp/index.html', '/cases/buefyBadCssUrl.html')

        when: "processing fonts"
        def res = run('webapp')

        then: "optimization done"
        res.getHtmls().size() == 1
        HtmlInfo html = res.getHtmls()[0]
        html.changes == ['changed links', 'minified']
        html.js.size() == 0
        html.css.size() == 1
        with(html.css.get(0)) {
            // all resources are already "data:"
            subResources.size() == 0
        }
    }
}
