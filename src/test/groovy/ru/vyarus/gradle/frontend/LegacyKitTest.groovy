package ru.vyarus.gradle.frontend

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.IgnoreIf

/**
 * @author Vyacheslav Rusakov
 * @since 26.06.2023
 */
@IgnoreIf({ !jvm.java11 })
class LegacyKitTest extends AbstractKitTest {

    String GRADLE_VERSION = '6.2'

    def "Check plugin execution"() {
        setup:
        build """
            plugins {
                id 'ru.vyarus.prod-frontend'
            }
            
            prodFrontend {
                sourceDir = 'web'
            }
        """
        fileFromClasspath('web/index.html', '/cases/bootstrap.html')

        when: "run task"
        BuildResult result = runVer(GRADLE_VERSION, 'prodFrontend')

        then: "task successful"
        result.task(':prodFrontend').outcome == TaskOutcome.SUCCESS
        unifyString(result.output).contains("""
                                                                       original       minified       gzipped        
--------------------------------------------------------------------------------------------------------------------
index.html                                                             651 bytes      503 bytes      397 bytes      
  js/bootstrap.bundle.min.js                                           78 KB                         22 KB          
  css/bootstrap.min.css                                                190 KB                        26 KB          
                                                                       ---------------------------------------------
                                                                       269 KB         269 KB         49 KB""")

        when: "run task again"
        File index = file('web/index.html')
        long modified = index.lastModified()
        result = runVer(GRADLE_VERSION, 'prodFrontend')

        then: "task executed"
        result.task(':prodFrontend').outcome == TaskOutcome.SUCCESS
        file('web/index.html').lastModified() == modified
        unifyString(result.output).contains("""
                                                                       original       minified       gzipped        
--------------------------------------------------------------------------------------------------------------------
index.html                                                             503 bytes      503 bytes      397 bytes      
  js/bootstrap.bundle.min.js                                           78 KB                         22 KB          
  css/bootstrap.min.css                                                190 KB                        26 KB          
                                                                       ---------------------------------------------
                                                                       269 KB         269 KB         49 KB""")
        !result.output.contains('Gzip index.html')
    }
}
