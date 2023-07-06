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
        ResourceLoader.LoadResult res = ResourceLoader.download('https://cdn.jsdelivr.net/npm/@mdi/font@2.5.94/css/materialdesignicons.css', false, false, tmp)

        then: "loaded"
        res.file != null
        res.file.name == 'materialdesignicons.css'
        res.sourceMap == null

        when: "load min resource"
        res.file.delete()
        res = ResourceLoader.download('https://cdn.jsdelivr.net/npm/@mdi/font@2.5.94/css/materialdesignicons.css', true, false, tmp)

        then: "loaded"
        res.file != null
        res.file.name == 'materialdesignicons.min.css'
        res.sourceMap == null

        when: "load min resource with source map"
        res.file.delete()
        res = ResourceLoader.download('https://cdn.jsdelivr.net/npm/@mdi/font@2.5.94/css/materialdesignicons.css', true, true, tmp)

        then: "loaded"
        res.file != null
        res.file.name == 'materialdesignicons.min.css'
        res.sourceMap != null
        res.sourceMap.name == 'materialdesignicons.min.css.map'
        res.sourceMap.exists()

        and: "sources embedded"
        !SourceMapUtils.parse(res.sourceMap).getSourcesContent().isEmpty()
    }

    def "Check source map not exists"() {

        when: "load min resource with source map"
        ResourceLoader.LoadResult res = ResourceLoader.download('https://unpkg.com/vue@2.7.14/dist/vue.js', true, true, tmp)

        then: "loaded, but source map not found"
        res.file != null
        res.file.name == 'vue.min.js'
        res.sourceMap == null
        File source = new File(tmp, 'vue.min.js.map')
        !source.exists()

    }
}
