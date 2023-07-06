package ru.vyarus.gradle.frontend

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome

/**
 * @author Vyacheslav Rusakov
 * @since 28.01.2023
 */
class ProdFrontendPluginKitTest extends AbstractKitTest {

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
        BuildResult result = run('prodFrontend')

        then: "task successful"
        result.task(':prodFrontend').outcome == TaskOutcome.SUCCESS
        result.output.contains("""
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
        result = run('prodFrontend')

        then: "task executed"
        result.task(':prodFrontend').outcome == TaskOutcome.SUCCESS
        file('web/index.html').lastModified() == modified
        result.output.contains("""
                                                                       original       minified       gzipped        
--------------------------------------------------------------------------------------------------------------------
index.html                                                             503 bytes      503 bytes      397 bytes      
  js/bootstrap.bundle.min.js                                           78 KB                         22 KB          
  css/bootstrap.min.css                                                190 KB                        26 KB          
                                                                       ---------------------------------------------
                                                                       269 KB         269 KB         49 KB""")
        !result.output.contains('Gzip index.html')
    }

    def "Check plugin execution for not existing folder"() {
        setup:
        build """
            plugins {
                id 'ru.vyarus.prod-frontend'
            }
            
            prodFrontend {
                sourceDir = 'web'
            }
        """

        when: "run task"
        BuildResult result = runFailed('prodFrontend')

        then: "task successful"
        result.task(':prodFrontend').outcome == TaskOutcome.FAILED
        result.output.contains("Webapp directory does not exists:")

    }
}