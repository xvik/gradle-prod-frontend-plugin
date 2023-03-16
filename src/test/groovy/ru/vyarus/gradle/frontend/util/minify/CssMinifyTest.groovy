package ru.vyarus.gradle.frontend.util.minify

import ru.vyarus.gradle.frontend.AbstractTest

/**
 * @author Vyacheslav Rusakov
 * @since 06.03.2023
 */
class CssMinifyTest extends AbstractTest {

    def "Check css minification"() {

        setup:
        File file = fileFromClasspath('buefy.css', '/min/buefy.css')
        long size = file.length()

        when: "minifying"
        def res = CssMinifier.minify(file, true)

        then: "minified"
        res.minified.name == 'buefy.min.css'
        res.minified.length() < size
        res.sourceMap.exists()
        !file.exists()
    }
}
