package ru.vyarus.gradle.frontend.util

import ru.vyarus.gradle.frontend.AbstractTest
import spock.lang.Specification

/**
 * @author Vyacheslav Rusakov
 * @since 02.03.2023
 */
class SourceMapUtilsTest extends AbstractTest {

    def "Check sources inclusion"() {

        setup: "prepare map"
        File file = fileFromClasspath("materialdesignicons.min.css.map", '/sourcemap/materialdesignicons.min.css.map')

        when: "download and embed content"
        SourceMapUtils.includeSources(file, "https://cdn.jsdelivr.net/npm/@mdi/font@2.5.94/css/")

        then: "map updated"
        def parse = SourceMapUtils.parse(file)
        parse.sourcesContent.size() == parse.sources.size()
    }

    def "Check no action"() {
        setup: "prepare map"
        File file = fileFromClasspath("bootstrap.bundle.min.js.map", '/sourcemap/bootstrap.bundle.min.js.map')
        long size = file.length()

        when: "download and embed content"
        SourceMapUtils.includeSources(file, "https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/")

        then: "map updated"
        def parse = SourceMapUtils.parse(file)
        parse.sourcesContent.size() == parse.sources.size()
        file.length() == size
    }
}
