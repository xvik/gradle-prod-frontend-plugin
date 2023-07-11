package ru.vyarus.gradle.frontend.util.minify

import ru.vyarus.gradle.frontend.AbstractTest
import ru.vyarus.gradle.frontend.core.util.SourceMapUtils
import ru.vyarus.gradle.frontend.core.util.minify.CssMinifier

/**
 * @author Vyacheslav Rusakov
 * @since 06.03.2023
 */
class CssMinifyTest extends AbstractTest {

    def "Check buefy minification"() {

        setup:
        File file = fileFromClasspath('buefy.css', '/min/buefy.css')
        long size = file.length()

        when: "minifying"
        def res = new CssMinifier().minify(file, true)


        then: "minified"
        res.minified.name == 'buefy.min.css'
        res.minified.length() < size
        res.sourceMap.exists()
        res.sourceMap.name == 'buefy.min.css.map'
        with(SourceMapUtils.parse(res.sourceMap)) {
            sources == ['buefy.css']
            sourcesContent.size() == 1
        }
    }

    def "Check bootstrap minification"() {

        setup:
        File file = fileFromClasspath('bootstrap.css', '/min/bootstrap.css')
        long size = file.length()

        when: "minifying"
        def res = new CssMinifier().minify(file, true)

        then: "minified"
        res.minified.name == 'bootstrap.min.css'
        res.minified.length() < size
        res.sourceMap.exists()
        res.sourceMap.name == 'bootstrap.min.css.map'
    }

    def "Check materialdesignicons minification"() {

        setup:
        File file = fileFromClasspath('materialdesignicons.css', '/min/materialdesignicons.css')
        long size = file.length()

        when: "minifying"
        def res = new CssMinifier().minify(file, true)

        then: "minified"
        res.minified.name == 'materialdesignicons.min.css'
        res.minified.length() < size
        res.sourceMap.exists()
        res.sourceMap.name == 'materialdesignicons.min.css.map'
    }

    def "Check minification without source map"() {

        setup:
        File file = fileFromClasspath('bootstrap.css', '/min/bootstrap.css')
        long size = file.length()

        when: "minifying"
        def res = new CssMinifier().minify(file, false)

        then: "minified"
        res.minified.name == 'bootstrap.min.css'
        res.minified.length() < size
        res.sourceMap == null
    }
}
