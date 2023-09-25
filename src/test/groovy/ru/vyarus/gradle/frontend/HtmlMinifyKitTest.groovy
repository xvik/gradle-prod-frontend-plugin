package ru.vyarus.gradle.frontend

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome

/**
 * @author Vyacheslav Rusakov
 * @since 28.01.2023
 */
class HtmlMinifyKitTest extends AbstractKitTest {

    def "Check html minification"() {
        setup:
        build """
            plugins {
                id 'ru.vyarus.prod-frontend'
            }
            
            prodFrontend {
                sourceDir = 'webapp'
            }
        """

        file('webapp/index.html') << """<!DOCTYPE html>
<html>
<head>
</head>
<body>
<pre>sample body
string</pre>
</body>
</html>
"""

        when: "run task"
        BuildResult result = run('prodFrontend')

        then: "task successful"
        result.task(':prodFrontend').outcome == TaskOutcome.SUCCESS
        unifyString(file('webapp/index.html').text) == """<!doctype html><html><head><body><pre>sample body
string</pre>"""
    }
}
