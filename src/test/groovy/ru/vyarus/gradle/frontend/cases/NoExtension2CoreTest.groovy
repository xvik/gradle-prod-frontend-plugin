package ru.vyarus.gradle.frontend.cases

import ru.vyarus.gradle.frontend.core.info.resources.HtmlInfo

/**
 * Case when there is no resource extension, even after all redirects.
 *
 * @author Vyacheslav Rusakov
 * @since 19.10.2023
 */
class NoExtension2CoreTest extends AbstractCoreTest {

    def "Check css without extension support"() {

        fileFromClasspath('webapp/index.html', '/cases/noCssExtension/noCssExtension.html')

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
            changes.containsAll(['integrity token applied', 'minified'])
            target.startsWith("css/fonts.googleapis.min.css?")
            element.attr('integrity') != null
            gzip != null
            file != null
        }
    }

    def "Check sub css without extension support"() {

        fileFromClasspath('webapp/index.html', '/cases/noCssExtension/noCssExtension2.html')
        fileFromClasspath('webapp/style.css', '/cases/noCssExtension/style.css')

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
            changes.containsAll(['integrity token applied', 'minified'])
            target.startsWith("style.min.css?")
            element.attr('integrity') != null
            gzip != null
            file != null

            // no way to append correct extension cause its css sub resource (could be anything)
            subResources[0].target.startsWith('resources/css?')
        }
    }
}
