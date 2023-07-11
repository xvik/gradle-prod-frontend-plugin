package ru.vyarus.gradle.frontend.util

import ru.vyarus.gradle.frontend.core.util.DurationFormatter
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 11.03.2018
 */
class DurationFormatTest extends Specification {

    def "Check duration format"() {

        expect: 'correct format'
        DurationFormatter.format(2000) == "2s"
        DurationFormatter.format(2*24*60*60*1000 + 80*60*1000 + 10*1000) == "2d 1h 20m 10s"
    }
}
