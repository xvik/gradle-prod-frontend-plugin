package ru.vyarus.gradle.frontend

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome

/**
 * @author Vyacheslav Rusakov
 * @since 28.01.2023
 */
class ResourcesDownloadKitTest extends AbstractKitTest {

    def "Check plugin execution"() {
        setup:
        build """
            plugins {
                id 'ru.vyarus.prod-frontend'
            }
            
            prodFrontend {
                source = 'webapp'
            }
        """

        file('webapp/index.html') << """<!DOCTYPE html>
<html>
<head>
<script src="https://unpkg.com/vue@2.7.14/dist/vue.js"></script>
<link rel="stylesheet" href="https://cdn.materialdesignicons.com/2.5.94/css/materialdesignicons.min.css">
</head>
<body>
</body>
</html>
"""

        when: "run task"
        BuildResult result = run('prodFrontend')

        then: "task successful"
        result.task(':prodFrontend').outcome == TaskOutcome.SUCCESS

        file('webapp/js/vue.js').exists()
        file('webapp/css/materialdesignicons.min.css').exists()
        file('webapp/index.html').text == """<!doctype html>
<html>
 <head>
  <script src="js/vue.js"></script>
  <link rel="stylesheet" href="css/materialdesignicons.min.css">
 </head>
 <body>
 </body>
</html>"""
    }
}
