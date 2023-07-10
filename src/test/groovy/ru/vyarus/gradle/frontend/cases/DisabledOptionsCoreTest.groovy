package ru.vyarus.gradle.frontend.cases

import ru.vyarus.gradle.frontend.core.info.resources.HtmlInfo

/**
 * @author Vyacheslav Rusakov
 * @since 10.07.2023
 */
class DisabledOptionsCoreTest extends AbstractCoreTest {

    def "Check disable html min"() {

        fileFromClasspath('webapp/index.html', '/cases/cssManualMin/cssManualMin.html')
        fileFromClasspath('webapp/materialdesignicons.css', '/cases/cssManualMin/materialdesignicons.css')

        when: "processing"
        def res = run(builder('webapp').minifyHtml(false).minifyCss(false))

        then: "optimization done"
        res.getHtmls().size() == 1
        HtmlInfo html = res.getHtmls()[0]
        !html.changes.contains('minified')
    }

    def "Check disable manual css min"() {

        fileFromClasspath('webapp/index.html', '/cases/cssManualMin/cssManualMin.html')
        fileFromClasspath('webapp/materialdesignicons.css', '/cases/cssManualMin/materialdesignicons.css')

        when: "processing"
        def res = run(builder('webapp').minifyCss(false))

        then: "optimization done"
        res.getHtmls().size() == 1
        HtmlInfo html = res.getHtmls()[0]
        html.changes == ['changed links', 'minified']
        with(html.css[0]) {
            !remote
            !changes.contains('minified')
            target.startsWith("materialdesignicons.css?")
        }
    }

    def "Check disable manual js min"() {

        fileFromClasspath('webapp/index.html', '/cases/jsManualMin/jsManualMin.html')
        fileFromClasspath('webapp/vue.js', '/cases/jsManualMin/vue.js')

        when: "processing"
        def res = run(builder('webapp').minifyJs(false))

        then: "optimization done"
        res.getHtmls().size() == 1
        HtmlInfo html = res.getHtmls()[0]
        with(html.js[0]) {
            !remote
            !changes.contains('minified')
            target.startsWith("vue.js?")
        }
    }

    def "Check disabled integrity"() {

        fileFromClasspath('webapp/index.html', '/cases/bootstrap.html')

        when: "processing bootstrap application"
        def res = run(builder('webapp').applyIntegrity(false))

        then: "optimization done"
        res.getHtmls().size() == 1
        HtmlInfo html = res.getHtmls()[0]
        with(html.js[0]) {
            remote
            !changes.contains('integrity token applied')
            element.attr('integrity').length() == 0
        }
        with(html.css[0]) {
            remote
            !changes.contains('integrity token applied')
            element.attr('integrity').length() == 0
        }
    }

    def "Check no download"() {

        fileFromClasspath('webapp/index.html', '/cases/bootstrap.html')

        when: "processing bootstrap application"
        def res = run(builder('webapp').downloadResources(false))

        then: "optimization done"
        res.getHtmls().size() == 1
        HtmlInfo html = res.getHtmls()[0]
        with(html.js[0]) {
            remote
            ignored
            target.startsWith("http")
            element.attr('integrity').length() > 0
            element.attr('crossorigin').length() > 0
            gzip == null
            file == null
            sourceMap == null
        }
        with(html.css[0]) {
            remote
            ignored
            target.startsWith("http")
            element.attr('integrity').length() > 0
            element.attr('crossorigin').length() > 0
            gzip == null
            file == null
            sourceMap == null
        }
    }

    def "Check no ani-cache"() {

        fileFromClasspath('webapp/index.html', '/cases/httpSubResource/httpSubResource.html')
        fileFromClasspath('webapp/materialdesignicons.min.css', '/cases/httpSubResource/materialdesignicons.min.css')

        when: "processing fonts"
        def res = run(builder('webapp').applyAntiCache(false))

        then: "optimization done"
        res.getHtmls().size() == 1
        HtmlInfo html = res.getHtmls()[0]
        html.js.size() == 0
        html.css.size() == 1
        with(html.css[0]) {
            target == 'materialdesignicons.min.css'
            element.attr('integrity') != null
            gzip != null
            file != null
            // one duplicate resource, but with different source url
            getSubResources().size() == 6
            getSubResources()[0].target == 'resources/materialdesignicons-webfont.eot'

            file('webapp/resources/materialdesignicons-webfont.eot').exists()
            file('webapp/materialdesignicons.min.css').text.contains('url("resources/materialdesignicons-webfont.eot")')
        }
    }

    def "Check no gzip"() {

        fileFromClasspath('webapp/index.html', '/cases/httpSubResource/httpSubResource.html')
        fileFromClasspath('webapp/materialdesignicons.min.css', '/cases/httpSubResource/materialdesignicons.min.css')

        when: "processing fonts"
        def res = run(builder('webapp').gzip(false))

        then: "optimization done"
        res.getHtmls().size() == 1
        HtmlInfo html = res.getHtmls()[0]
        html.gzip == null
        with(html.css[0]) {
            gzip == null
            file != null
            // one duplicate resource, but with different source url
            getSubResources().size() == 6
            getSubResources()[0].gzip == null
        }
    }
}
