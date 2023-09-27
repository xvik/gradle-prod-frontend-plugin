package ru.vyarus.gradle.frontend.cases

/**
 * @author Vyacheslav Rusakov
 * @since 27.09.2023
 */
class IgnoreCoreTest extends AbstractCoreTest {

    def "Check html ignore"() {
        fileFromClasspath('webapp/index.html', '/cases/bootstrap.html')

        when: "processing"
        def res = run(builder('webapp').ignore('**.html'))

        then: "ignored"
        res.getHtmls().size() == 0


        when: "processing"
        run(builder('webapp').ignore('*.html'))

        then: "ignored"
        res.getHtmls().size() == 0


        when: "processing"
        run(builder('webapp').ignore('index.html'))

        then: "ignored"
        res.getHtmls().size() == 0
    }

    def "Check download ignore"() {
        fileFromClasspath('webapp/index.html', '/cases/bootstrap.html')

        when: "processing"
        def res = run(builder('webapp').downloadIgnore('cdn.jsdelivr.net'))

        then: "ignored"
        res.getHtmls()[0].js[0].ignored
        res.getHtmls()[0].css[0].ignored


        when: "processing"
        res = run(builder('webapp').downloadIgnore('.*/bootstrap.*'))

        then: "ignored"
        res.getHtmls()[0].js[0].ignored
        res.getHtmls()[0].css[0].ignored
    }

    def "Check sub-resource ignore"() {
        fileFromClasspath('webapp/index.html', '/cases/httpSubResource/httpSubResource.html')
        // important to have absolute urls in css file - relative urls are always downloaded
        fileFromClasspath('webapp/materialdesignicons.min.css', '/cases/httpSubResource/materialdesignicons.min.css')

        when: "ignore web fonts loading only"
        def res = run(builder('webapp').downloadIgnore('.*/fonts/.*'))

        then: "ignored"
        with(res.getHtmls()[0].css[0]) {
            !ignored
            subResources.size() == 6
            with(subResources[0]) {
                ignored
                remote
                file == null
            }
        }
    }

    def "Check ignore after download"() {
        fileFromClasspath('webapp/index.html', '/cases/bootstrap.html')

        when: "processing"
        def res = run(builder('webapp').ignore('*/bootstrap.*'))

        then: "ignored"
        with(res.getHtmls()[0].js[0]) {
            remote
            ignored
            file != null
        }
        with(res.getHtmls()[0].css[0]) {
            remote
            ignored
            file != null
        }
    }

    def "Check minification ignore"() {
        fileFromClasspath('webapp/index.html', '/cases/jsManualMin/jsManualMin.html')
        fileFromClasspath('webapp/vue.js', '/cases/jsManualMin/vue.js')

        when: "processing"
        def res = run(builder('webapp').minifyIgnore('index.html', 'vue.js'))

        then: "ignored"
        !res.getHtmls()[0].changes.contains("minified")
        !res.getHtmls()[0].js[0].changes.contains("minified")


        fileFromClasspath('webapp/index.html', '/cases/cssManualMin/cssManualMin.html')
        fileFromClasspath('webapp/materialdesignicons.css', '/cases/cssManualMin/materialdesignicons.css')

        when: "processing"
        res = run(builder('webapp').minifyIgnore('*.css'))

        then: "ignored"
        res.getHtmls()[0].changes.contains("minified")
        !res.getHtmls()[0].css[0].changes.contains("minified")

    }
}
