package ru.vyarus.gradle.frontend.cases

import ru.vyarus.gradle.frontend.core.OptimizationFlow
import ru.vyarus.gradle.frontend.core.info.OptimizationInfo
import spock.lang.Specification
import spock.lang.TempDir

/**
 * @author Vyacheslav Rusakov
 * @since 25.04.2023
 */
abstract class AbstractCoreTest extends Specification {

    @TempDir
    File testDir

    OptimizationInfo run(String dir) {
        def res = OptimizationFlow.create(file(dir))
                .downloadResources()
                .preferMinDownload()
                .downloadSourceMaps()
                .minifyJs()
                .minifyCss()
                .minifyHtml()
                .minifyHtmlCss()
                .minifyHtmlJs()
                .applyAntiCache()
                .applyIntegrity()
                .sourceMaps()
                .gzip()
                .debug()
                .run()
        res.printStats()
        if (!res.htmls.isEmpty()) {
            println "RESULTED HTML:---------------------------------------------------"
            println res.htmls[0].file.text
            println "-----------------------------------------------------------------"
        }
        res
    }

    File file(String path) {
        File res = new File(testDir, path)
        File dir = res.isDirectory() ? res : res.parentFile
        dir.mkdirs()
        res
    }

    File fileFromClasspath(String toFile, String source) {
        File target = file(toFile)
        target.parentFile.mkdirs()
        target.withOutputStream {
            it.write((getClass().getResourceAsStream(source) ?: getClass().classLoader.getResourceAsStream(source)).bytes)
        }
        target
    }

    protected String unifyString(String input) {
        return input
        // cleanup win line break for simpler comparisons
                .replace("\r", '')
    }
}
