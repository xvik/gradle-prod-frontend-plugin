package ru.vyarus.gradle.frontend.cases

import org.apache.tools.ant.taskdefs.condition.Os
import ru.vyarus.gradle.frontend.core.OptimizationFlow
import ru.vyarus.gradle.frontend.core.info.OptimizationInfo
import spock.lang.Specification
import spock.lang.TempDir

/**
 * @author Vyacheslav Rusakov
 * @since 25.04.2023
 */
abstract class AbstractCoreTest extends Specification {

    boolean isWin = Os.isFamily(Os.FAMILY_WINDOWS)

    @TempDir
    File testDir

    OptimizationFlow.Builder builder(String dir) {
        return OptimizationFlow.create(file(dir))
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
                .generateSourceMaps()
                .gzip()
    }

    OptimizationInfo run(String dir) {
        run(builder(dir))
    }

    OptimizationInfo run(OptimizationFlow.Builder builder) {
        def res = builder
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
        // on windows it would use \r\n, on linux \n
        target.withOutputStream {
            it.write((getClass().getResourceAsStream(source) ?: getClass().classLoader.getResourceAsStream(source)).bytes)
        }
        target
    }

    File fileFromClasspathLF(String toFile, String source) {
        File target = file(toFile)
        target.parentFile.mkdirs()
        target.withOutputStream {
            def bytes = (getClass().getResourceAsStream(source) ?: getClass().classLoader.getResourceAsStream(source)).bytes
            if (isWin) {
                // remove CR to unify length win linux
                bytes = (bytes as List).findAll { it != 13} as byte[]
            }
            it.write(bytes)
        }
        target
    }

    protected String unifyString(String input) {
        return input
        // cleanup win line break for simpler comparisons
                .replace("\r", '')
                .replace('\\', '/')
    }
}
