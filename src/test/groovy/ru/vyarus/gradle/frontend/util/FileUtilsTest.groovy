package ru.vyarus.gradle.frontend.util

import ru.vyarus.gradle.frontend.AbstractTest
import ru.vyarus.gradle.frontend.core.util.FileUtils

/**
 * @author Vyacheslav Rusakov
 * @since 12.06.2023
 */
class FileUtilsTest extends AbstractTest {

    def "Check files search"() {

        setup: "create files"
        file('sample.html') << ''
        file('sample.jsp') << ''
        file('sub/sample.html') << ''
        file('sub/sample.jsp') << ''

        when: "searching for html"
        List<File> res = FileUtils.findHtmls(testProjectDir, ['html'])

        then: "found"
        res.size() == 2
        res[0].name == 'sample.html'
        res[1].name == 'sample.html'
        res[1].parentFile.name == 'sub' || res[0].parentFile.name == 'sub'

        when: "searching for jsp"
        res = FileUtils.findHtmls(testProjectDir, ['jsp'])

        then: "found"
        res.size() == 2
        res[0].name == 'sample.jsp'
        res[1].name == 'sample.jsp'
        res[1].parentFile.name == 'sub' || res[0].parentFile.name == 'sub'
    }

    def "Check select not existing"() {
        setup: "create files"
        file('sample.html') << ''

        when: "selecting not existing file"
        File file = FileUtils.selectNotExistingFile(testProjectDir, 'sample.html')

        then: "selected"
        !file.exists()
        file.name == 'sample.1.html'

        when: "selecting not existing file again"
        file.createNewFile()
        file = FileUtils.selectNotExistingFile(testProjectDir, 'sample.html')

        then: "selected"
        !file.exists()
        file.name == 'sample.2.html'
    }

    def "Check append before extension"() {

        expect:
        FileUtils.appendBeforeExtension("sample.js", "-tt") == 'sample-tt.js'
        FileUtils.appendBeforeExtension("sample.js", ".js") == 'sample.js.js'
    }

    def "Check min name construction"() {

        expect:
        FileUtils.getMinName('sample.js') == 'sample.min.js'
        FileUtils.getMinName('sample.min.js') == 'sample.min.js'
        FileUtils.getMinName('sample.min.js') == 'sample.min.js'
    }

    def "Check relative path computation"() {

        expect:
        FileUtils.relative(file('index.html'), file('js.js')) == 'js.js'
        FileUtils.relative(testProjectDir, file('js.js')) == 'js.js'
        FileUtils.relative(file('index.html'), file('sub/js.js')) == 'sub/js.js'
        FileUtils.relative(file('sub/index.html'), file('js.js')) == '../js.js'
    }

    def "Check file read-write"() {

        when: "writing file"
        FileUtils.writeFile(file('sample.txt'), 'sample content')

        then: "ok"
        FileUtils.readFile(file('sample.txt')) == 'sample content'
    }

    def "Check md5 computation"() {

        when: "compute md5 of file"
        File file = file('sample.txt')
        FileUtils.writeFile(file, 'sample')
        String md1 = FileUtils.computeMd5(file)

        then: "ok"
        md1 != null
        md1 == FileUtils.computeMd5(file)

        when: "different content"
        FileUtils.writeFile(file, 'other sample')
        String md2 = FileUtils.computeMd5(file)

        then: "different"
        md1 != md2
    }

    def "Check gzip"() {

        File file = file('sample.txt')
        file << "sample content"

        when: "gzip"
        File gzip = FileUtils.gzip(file, testProjectDir)
        long last = gzip.lastModified()

        then:
        gzip.name == 'sample.txt.gz'
        gzip.size() > 0

        when: "compute again"
        gzip = FileUtils.gzip(file, testProjectDir)

        then: "not changed"
        gzip.lastModified() == last
    }

    def "Check read last line"() {

        file('sample.txt') << """
one
two

three


"""
        expect:
        FileUtils.readLastLine(file('sample.txt')) == 'three'
    }
}
