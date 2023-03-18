package ru.vyarus.gradle.frontend.util.minify

import ru.vyarus.gradle.frontend.AbstractTest
import ru.vyarus.gradle.frontend.util.SourceMapUtils

/**
 * @author Vyacheslav Rusakov
 * @since 16.03.2023
 */
class JsMinifyTest extends AbstractTest {

    def "Check buefy minification"() {

        setup:
        File file = fileFromClasspath('buefy.js', '/min/buefy.js')
        long size = file.length()

        when: "minifying"
        def res = JsMinifier.minify(file, true)

        then: "minified"
        res.minified.name == 'buefy.min.js'
        res.minified.length() < size
        res.sourceMap.exists()
        res.sourceMap.name == 'buefy.min.js.map'
        !file.exists()
        with(SourceMapUtils.parse(res.sourceMap)) {
            sources == ['buefy.js']
            sourcesContent.size() == 1
        }
    }

    def "Check bootstrap minification"() {

        setup:
        File file = fileFromClasspath('bootstrap.bundle.js', '/min/bootstrap.bundle.js')
        long size = file.length()

        when: "minifying"
        def res = JsMinifier.minify(file, true)

        then: "minified"
        res.minified.name == 'bootstrap.bundle.min.js'
        res.minified.length() < size
        res.sourceMap.exists()
        res.sourceMap.name == 'bootstrap.bundle.min.js.map'
        !file.exists()
        with(SourceMapUtils.parse(res.sourceMap)) {
            sources == ['bootstrap.bundle.js']
            sourcesContent.size() == 1
        }
    }

    def "Check vue minification"() {

        setup:
        File file = fileFromClasspath('vue.js', '/min/vue.js')
        long size = file.length()

        when: "minifying"
        def res = JsMinifier.minify(file, true)

        then: "minified"
        res.minified.name == 'vue.min.js'
        res.minified.length() < size
        res.sourceMap.exists()
        res.sourceMap.name == 'vue.min.js.map'
        !file.exists()
        with(SourceMapUtils.parse(res.sourceMap)) {
            sources == ['vue.js']
            sourcesContent.size() == 1
        }
    }
}
