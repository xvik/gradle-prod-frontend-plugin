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

    }
}
