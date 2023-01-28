package ru.vyarus.gradle.frontend

import org.gradle.api.Project
import org.gradle.api.ProjectConfigurationException
import org.gradle.testfixtures.ProjectBuilder

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

    }

    def "Check extension validation"() {

        when: "plugin configured"
        Project project = project {
            apply plugin: "ru.vyarus.prod-frontend"

            productionFrontend {
                foo '1'
                bar '2'
            }
        }

        then: "validation pass"
        def productionFrontend = project.extensions.productionFrontend;
        productionFrontend.foo == '1'
        productionFrontend.bar == '2'
    }


    def "Check extension validation failure"() {

        when: "plugin configured"
        Project project = project {
            apply plugin: "ru.vyarus.prod-frontend"

            productionFrontend {
                foo '1'
            }
        }

        then: "validation failed"
        def ex = thrown(ProjectConfigurationException)
        ex.cause.message == 'productionFrontend.bar configuration required'
    }

}