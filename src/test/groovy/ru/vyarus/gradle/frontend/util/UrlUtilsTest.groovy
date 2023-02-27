package ru.vyarus.gradle.frontend.util

import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 27.02.2023
 */
class UrlUtilsTest extends Specification {

    def "Check base url resolution"() {

        expect:
        UrlUtils.getServerRoot('http://some.com/somewhere') == 'http://some.com'
        UrlUtils.getServerRoot('https://some.com/somewhere') == 'https://some.com'
        UrlUtils.getServerRoot('http://some.com:225/somewhere') == 'http://some.com:225'
    }
}
