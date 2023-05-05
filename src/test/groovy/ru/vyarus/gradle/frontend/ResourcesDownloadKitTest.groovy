package ru.vyarus.gradle.frontend

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome

/**
 * @author Vyacheslav Rusakov
 * @since 28.01.2023
 */
class ResourcesDownloadKitTest extends AbstractKitTest {

    def "Check resources load from url"() {
        setup:
        build """
            plugins {
                id 'ru.vyarus.prod-frontend'
            }
            
            prodFrontend {
                sourceDir = 'webapp'
                minify.html = false
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
        long idxSize = file('webapp/index.html').length()
        BuildResult result = run('prodFrontend')

        then: "task successful"
        result.task(':prodFrontend').outcome == TaskOutcome.SUCCESS

        file('webapp/js/vue.min.js').exists()
        file('webapp/css/materialdesignicons.min.css').exists()
        file('webapp/index.html').length() != idxSize

        println file('webapp/index.html').text
    }
}
