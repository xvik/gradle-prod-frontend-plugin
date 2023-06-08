package ru.vyarus.gradle.frontend.util

import ru.vyarus.gradle.frontend.AbstractTest
import ru.vyarus.gradle.frontend.core.util.SourceMapUtils

/**
 * @author Vyacheslav Rusakov
 * @since 02.03.2023
 */
class SourceMapUtilsTest extends AbstractTest {

    def "Check source map extraction"() {

        expect:
        SourceMapUtils.getSourceMapReference("/*# sourceMappingURL=materialdesignicons.min.css.map */") == 'materialdesignicons.min.css.map'
        // jsdelivr
        SourceMapUtils.getSourceMapReference("//# sourceMappingURL=/sm/9365766bce1527e45586988d0bb7e9064acca1c1d547544ad774220eaebf0c8b.map") == '/sm/9365766bce1527e45586988d0bb7e9064acca1c1d547544ad774220eaebf0c8b.map'
    }

    def "Check sources inclusion"() {

        setup: "prepare map"
        File file = fileFromClasspath("materialdesignicons.min.css.map", '/sourcemap/materialdesignicons.min.css.map')

        when: "download and embed content"
        SourceMapUtils.includeRemoteSources(file, "https://cdn.jsdelivr.net/npm/@mdi/font@2.5.94/css/")

        then: "map updated"
        def parse = SourceMapUtils.parse(file)
        parse.sourcesContent.size() == parse.sources.size()
    }

    def "Check no action"() {
        setup: "prepare map"
        File file = fileFromClasspath("bootstrap.bundle.min.js.map", '/sourcemap/bootstrap.bundle.min.js.map')
        long size = file.length()

        when: "detect sources already embedded"
        SourceMapUtils.includeRemoteSources(file, "https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/")

        then: "map not updated"
        def parse = SourceMapUtils.parse(file)
        parse.sourcesContent.size() == parse.sources.size()
        file.length() == size
    }
}
