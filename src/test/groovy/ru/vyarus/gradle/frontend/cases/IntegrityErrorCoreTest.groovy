package ru.vyarus.gradle.frontend.cases

import ru.vyarus.gradle.frontend.core.info.HtmlInfo

/**
 * @author Vyacheslav Rusakov
 * @since 29.04.2023
 */
class IntegrityErrorCoreTest extends AbstractCoreTest {

    def "Check integrity check fail"() {

        fileFromClasspath('webapp/index.html', '/cases/integrityError.html')

        when: "processing bootstrap application"
        def res = run('webapp')

        then: "integrity check failed"
        def ex = thrown(IllegalStateException)
        ex.message.startsWith("Integrity check failed for downloaded file")
    }
}
