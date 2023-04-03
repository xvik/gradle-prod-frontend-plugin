package ru.vyarus.gradle.frontend.util

import ru.vyarus.gradle.frontend.core.util.CssUtils
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 28.02.2023
 */
class CssUtilsTest extends Specification {

    def "Check url recognition"() {

        expect:
        CssUtils.findLinks(cont as String) == [res]

        where:
        cont                                       | res
        "background-image: url('test/test.gif');"  | 'test/test.gif'
        'background: url("test2/test2.gif");'      | 'test2/test2.gif'
        'background-image: url(test3/test3.gif);'  | 'test3/test3.gif'
        'background: url   ( test4/ test4.gif );'  | 'test4/ test4.gif'
        'background: url( " test5/test5.gif"   );' | 'test5/test5.gif'

    }

    def "Check data urls ignores"() {

        expect:
        CssUtils.findLinks("--bs-navbar-toggler-icon-bg: url(\"data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 30 30'%3e%3cpath stroke='rgba%280, 0, 0, 0.55%29' stroke-linecap='round' stroke-miterlimit='10' stroke-width='2' d='M4 7h22M4 15h22M4 23h22'/%3e%3c/svg%3e\");")
            == []
    }

    def "Check css urls extraction"() {

        when: 'searching for links'
        def res = CssUtils.findLinks(new File('src/test/resources/links/materialdesignicons.css'))
        then: 'links found'
        res.size() == 6
        res == ['../fonts/materialdesignicons-webfont.eot?v=2.5.94',
                '../fonts/materialdesignicons-webfont.eot?#iefix&v=2.5.94',
                '../fonts/materialdesignicons-webfont.woff2?v=2.5.94',
                '../fonts/materialdesignicons-webfont.woff?v=2.5.94',
                '../fonts/materialdesignicons-webfont.ttf?v=2.5.94',
                '../fonts/materialdesignicons-webfont.svg?v=2.5.94#materialdesigniconsregular']
    }
}
