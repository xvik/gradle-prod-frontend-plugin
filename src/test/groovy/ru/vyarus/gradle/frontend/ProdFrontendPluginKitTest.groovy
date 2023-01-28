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

            productionFrontend {
                foo '1'
                bar '2'
            }

            task printFoo() {
                doLast {
                    println "fooo: \$productionFrontend.foo"
                }
            }

        """

        when: "run task"
        BuildResult result = run('printFoo')

        then: "task successful"
        result.task(':printFoo').outcome == TaskOutcome.SUCCESS
        result.output.contains('fooo: 1')
    }
}