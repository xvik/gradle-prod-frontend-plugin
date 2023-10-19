package ru.vyarus.gradle.frontend.util

import ru.vyarus.gradle.frontend.core.util.UrlUtils
import spock.lang.Specification

import java.nio.file.Files

/**
 * @author Vyacheslav Rusakov
 * @since 27.02.2023
 */
class UrlUtilsTest extends Specification {

    def "Check redirect detection"() {
         expect:
         UrlUtils.followRedirects('https://unpkg.com/vue@2.7.14') == 'https://unpkg.com/vue@2.7.14/dist/vue.js'
         UrlUtils.followRedirects('https://cdn.jsdelivr.net/npm/@mdi/font@2.5.94/css/materialdesignicons.css') == 'https://cdn.jsdelivr.net/npm/@mdi/font@2.5.94/css/materialdesignicons.css'
    }

    def "Check base url resolution"() {

        expect:
        UrlUtils.getBaseUrl('http://some.com/somewhere/file.txt') == 'http://some.com/somewhere/'
        UrlUtils.getBaseUrl('http://some.com:225/somewhere/file.txt') == 'http://some.com:225/somewhere/'
        UrlUtils.getBaseUrl('http:\\\\some.com:225\\somewhere\\file.txt') == 'http:\\\\some.com:225\\somewhere\\'
    }

    def "Check server url resolution"() {

        expect:
        UrlUtils.getServerRoot('http://some.com/somewhere') == 'http://some.com'
        UrlUtils.getServerRoot('https://some.com/somewhere/') == 'https://some.com'
        UrlUtils.getServerRoot('http://some.com:225/somewhere') == 'http://some.com:225'
    }

    def "Check url params cleanup"() {

        expect:
        UrlUtils.clearParams('http://some.com/file.txt') == 'http://some.com/file.txt'
        UrlUtils.clearParams('http://some.com/file.txt?some') == 'http://some.com/file.txt'
        UrlUtils.clearParams('http://some.com/file.txt#some') == 'http://some.com/file.txt'
    }

    def "Check url extension detection"() {

        expect:
        UrlUtils.hasExtension('http://some.com/file.txt')
        UrlUtils.hasExtension('http://some.com/file.txt?some')
        UrlUtils.hasExtension('http://some.com/file.txt#some')
        !UrlUtils.hasExtension('https://some.com/somewhere')
        !UrlUtils.hasExtension('http://some.com:225/file.extension')
    }

    def "Check file name extraction"() {

        expect:
        UrlUtils.getFileName('http://some.com/somewhere/file.txt') == 'file.txt'
        UrlUtils.getFileName('http://some.com/somewhere/file.txt?some') == 'file.txt'
        UrlUtils.getFileName('http://some.com/somewhere/file.txt#some') == 'file.txt'
        UrlUtils.getFileName('http://some.com:225/somewhere/file.txt') == 'file.txt'
        UrlUtils.getFileName('http:\\\\some.com:225\\somewhere\\file.txt') == 'file.txt'
    }

    def "Check filename selection from host"() {

        expect:
        UrlUtils.selectFilename('https://fonts.googleapis.com/css?family=Roboto', 'css') == 'fonts.googleapis.css'
        UrlUtils.selectFilename('https://fonts/css?family=Roboto', 'css') == 'fonts.css'
        UrlUtils.selectFilename('https://fonts.com/style.css?family=Roboto', 'css') == 'style.css'
        UrlUtils.selectFilename('https://fonts.com/style?family=Roboto', 'css') == 'style.css'
    }

    def "Smart download test"() {

        setup:
        File dir = Files.createTempDirectory("load").toFile()
        File target = new File(dir, "vue.js")

        when: "loading file"
        File res = UrlUtils.smartDownload('https://unpkg.com/vue@2.7.14/dist/vue.js', target)

        then: "loaded"
        res == target
        target.length() > 0

        when: "loading same file"
        long len = target.length()
        res = UrlUtils.smartDownload('https://unpkg.com/vue@2.7.14/dist/vue.js', target)

        then: "loaded and removed as duplicate"
        res == target
        len == target.length()

        when: "loading different file with same name"
        res = UrlUtils.smartDownload('https://unpkg.com/vue@2.7.13/dist/vue.js', target)

        then: "file renamed"
        res != target
        res.name == 'vue.1.js'

        cleanup:
        dir.deleteOnExit()
    }
}
