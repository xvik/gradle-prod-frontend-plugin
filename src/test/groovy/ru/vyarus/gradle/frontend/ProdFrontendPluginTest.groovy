package ru.vyarus.gradle.frontend

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import ru.vyarus.gradle.frontend.task.OptimizeFrontendTask

/**
 * @author Vyacheslav Rusakov
 * @since 28.01.2023
 */
class ProdFrontendPluginTest extends AbstractTest {

    def "Check extension registration"() {

        when: "plugin applied"
        Project project = ProjectBuilder.builder().build()
        project.plugins.apply "ru.vyarus.prod-frontend"

        then: "extension registered"
        project.extensions.findByType(ProdFrontendExtension)
        
        and: "task applied"
        project.tasks.getByName('prodFrontend') != null

    }

    def "Check default extension values"() {

        when: "plugin configured"
        Project project = project {
            apply plugin: "ru.vyarus.prod-frontend"
        }

        then: "extension defaults valid"
        ProdFrontendExtension prodFrontend = project.extensions.prodFrontend
        prodFrontend.debug == false
        prodFrontend.sourceDir == 'build/webapp'
        prodFrontend.jsDir == 'js'
        prodFrontend.cssDir == 'css'
        prodFrontend.download.enabled == true
        prodFrontend.download.preferMin == true
        prodFrontend.download.sourceMaps == true
        prodFrontend.minify.html == true
        prodFrontend.minify.htmlJs == true
        prodFrontend.minify.htmlCss == true
        prodFrontend.minify.js == true
        prodFrontend.minify.css == true
        prodFrontend.minify.generateSourceMaps == true
        prodFrontend.applyAntiCache == true
        prodFrontend.applyIntegrity == true
        prodFrontend.gzip == true

        and: "task configured accordingly"
        OptimizeFrontendTask task = project.tasks.getByName('prodFrontend')
        task.debug.get() == false
        unifyString(task.sourceDir.get().toString()).endsWith('build/webapp')
        task.jsDir.get() == 'js'
        task.cssDir.get() == 'css'
        task.downloadResources.get() == true
        task.preferMinDownload.get() == true
        task.downloadSourceMaps.get() == true
        task.minifyHtml.get() == true
        task.minifyHtmlJs.get() == true
        task.minifyHtmlCss.get() == true
        task.minifyJs.get() == true
        task.minifyCss.get() == true
        task.generateSourceMaps.get() == true
        task.applyAntiCache.get() == true
        task.applyIntegrity.get() == true
        task.gzip.get() == true
    }

    def "Check task configuration"() {

        when: "plugin configured"
        Project project = project {
            apply plugin: "ru.vyarus.prod-frontend"

            prodFrontend {
                debug  = true
                sourceDir = 'web'
                jsDir = 'jss'
                cssDir = 'csss'
                htmlExtensions = ['jsp', 'jtl']
                ignore = ['**/*.js']

                download {
                    enabled = false
                    preferMin = false
                    sourceMaps = false
                    ignore = ['.*somedomain\\.com.*']
                }

                minify {
                    html = false
                    htmlJs = false
                    htmlCss = false
                    js = false
                    css = false
                    generateSourceMaps = false
                    ignore = ['**/*.css']
                }

                applyAntiCache = false
                applyIntegrity = false
                gzip = false
            }
        }

        then: "extension configured"
        ProdFrontendExtension prodFrontend = project.extensions.prodFrontend
        prodFrontend.debug == true
        prodFrontend.sourceDir == 'web'
        prodFrontend.jsDir == 'jss'
        prodFrontend.cssDir == 'csss'
        prodFrontend.htmlExtensions == ['jsp', 'jtl']
        prodFrontend.ignore == ['**/*.js']
        prodFrontend.download.enabled == false
        prodFrontend.download.preferMin == false
        prodFrontend.download.sourceMaps == false
        prodFrontend.download.ignore == ['.*somedomain\\.com.*']
        prodFrontend.minify.html == false
        prodFrontend.minify.htmlJs == false
        prodFrontend.minify.htmlCss == false
        prodFrontend.minify.js == false
        prodFrontend.minify.css == false
        prodFrontend.minify.generateSourceMaps == false
        prodFrontend.minify.ignore == ['**/*.css']
        prodFrontend.applyAntiCache == false
        prodFrontend.applyIntegrity == false
        prodFrontend.gzip == false

        and: "task configured accordingly"
        OptimizeFrontendTask task = project.tasks.getByName('prodFrontend')
        task.debug.get() == true
        unifyString(task.sourceDir.get().toString()).endsWith('/web')
        task.jsDir.get() == 'jss'
        task.cssDir.get() == 'csss'
        task.htmlExtensions.get() == ['jsp', 'jtl']
        task.ignore.get() == ['**/*.js']
        task.downloadResources.get() == false
        task.preferMinDownload.get() == false
        task.downloadSourceMaps.get() == false
        task.downloadIgnore.get() == ['.*somedomain\\.com.*']
        task.minifyHtml.get() == false
        task.minifyHtmlJs.get() == false
        task.minifyHtmlCss.get() == false
        task.minifyJs.get() == false
        task.minifyCss.get() == false
        task.generateSourceMaps.get() == false
        task.minifyIgnore.get() == ['**/*.css']
        task.applyAntiCache.get() == false
        task.applyIntegrity.get() == false
        task.gzip.get() == false
    }
}