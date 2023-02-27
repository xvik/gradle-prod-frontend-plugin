package ru.vyarus.gradle.frontend.util

import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 06.02.2023
 */
class WebUtilsTest extends Specification {

    def "Check source map extraction"() {

        expect:
        WebUtils.getSourceMapReference("/*# sourceMappingURL=materialdesignicons.min.css.map */") == 'materialdesignicons.min.css.map'
        // jsdelivr
        WebUtils.getSourceMapReference("//# sourceMappingURL=/sm/9365766bce1527e45586988d0bb7e9064acca1c1d547544ad774220eaebf0c8b.map") == '/sm/9365766bce1527e45586988d0bb7e9064acca1c1d547544ad774220eaebf0c8b.map'
    }
}
