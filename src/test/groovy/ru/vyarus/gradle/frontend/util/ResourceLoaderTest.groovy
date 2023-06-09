package ru.vyarus.gradle.frontend.util

import ru.vyarus.gradle.frontend.AbstractTest
import ru.vyarus.gradle.frontend.core.util.ResourceLoader
import ru.vyarus.gradle.frontend.core.util.SourceMapUtils
import spock.lang.TempDir

/**
 * @author Vyacheslav Rusakov
 * @since 09.06.2023
 */
class ResourceLoaderTest extends AbstractTest {

    @TempDir
    File tmp

    def "Check resource load"() {

        when: "load resource as-is"
        File res = ResourceLoader.download('https://cdn.jsdelivr.net/npm/@mdi/font@2.5.94/css/materialdesignicons.css', false, false, tmp)

        then: "loaded"
        res != null
        res.name == 'materialdesignicons.css'

        when: "load min resource"
        res.delete()
        res = ResourceLoader.download('https://cdn.jsdelivr.net/npm/@mdi/font@2.5.94/css/materialdesignicons.css', true, false, tmp)

        then: "loaded"
        res != null
        res.name == 'materialdesignicons.min.css'
        !new File(tmp, 'vue.min.js.map').exists()

        when: "load min resource with source map"
        res.delete()
        res = ResourceLoader.download('https://cdn.jsdelivr.net/npm/@mdi/font@2.5.94/css/materialdesignicons.css', true, true, tmp)

        then: "loaded"
        res != null
        res.name == 'materialdesignicons.min.css'
        File source = new File(tmp, 'materialdesignicons.min.css.map')
        source.exists()

        and: "sources embedded"
        !SourceMapUtils.parse(source).getSourcesContent().isEmpty()
    }

    def "Check source map not exists"() {

        when: "load min resource with source map"
        File res = ResourceLoader.download('https://unpkg.com/vue@2.7.14/dist/vue.js', true, true, tmp)

        then: "loaded, but source map not found"
        res != null
        res.name == 'vue.min.js'
        File source = new File(tmp, 'vue.min.js.map')
        !source.exists()

    }
}
