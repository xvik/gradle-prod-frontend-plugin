package ru.vyarus.gradle.frontend.util

import ru.vyarus.gradle.frontend.AbstractTest
import ru.vyarus.gradle.frontend.core.util.SizeFormatter

/**
 * @author Vyacheslav Rusakov
 * @since 09.06.2023
 */
class SizeFormatterTest extends AbstractTest {

    def "Check size change message correctness"() {

        expect:
        SizeFormatter.formatChangePercent(100, 100) == 'not changed'
        SizeFormatter.formatChangePercent(100, 50) == '50% size decrease'
        SizeFormatter.formatChangePercent(50, 100) == '100% size increase(!)'
    }
}
