package ru.vyarus.gradle.frontend

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.TempDir

/**
 * Base class for plugin configuration tests.
 *
 * @author Vyacheslav Rusakov
 * @since 28.01.2023
 */
abstract class AbstractTest extends Specification {

    boolean isWin = Os.isFamily(Os.FAMILY_WINDOWS)

    @TempDir File testProjectDir

    Project project(Closure<Project> config = null) {
        projectBuilder(config).build()
    }

    ExtendedProjectBuilder projectBuilder(Closure<Project> root = null) {
        new ExtendedProjectBuilder().root(testProjectDir, root)
    }

    File file(String path) {
        File res = new File(testProjectDir, path)
        File dir = res.isDirectory() ? res : res.parentFile
        dir.mkdirs()
        res
    }

    File fileFromClasspath(String toFile, String source) {
        File target = file(toFile)
        target.parentFile.mkdirs()
        target.withOutputStream {
            def bytes = (getClass().getResourceAsStream(source) ?: getClass().classLoader.getResourceAsStream(source)).bytes
            if (isWin) {
                // remove CR to unify length win linux
                bytes = (bytes as List).findAll { it != 13} as byte[]
            }
            println "writing $bytes.length bytes into $target.name"
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

    static class ExtendedProjectBuilder {
        Project root

        ExtendedProjectBuilder root(File dir, Closure<Project> config = null) {
            assert root == null, "Root project already declared"
            Project project = ProjectBuilder.builder()
                    .withProjectDir(dir).build()
            if (config) {
                project.configure(project, config)
            }
            root = project
            return this
        }

        /**
         * Direct child of parent project
         *
         * @param name child project name
         * @param config optional configuration closure
         * @return builder
         */
        ExtendedProjectBuilder child(String name, Closure<Project> config = null) {
            return childOf(null, name, config)
        }

        /**
         * Direct child of any registered child project
         *
         * @param projectRef name of required parent module (gradle project reference format: `:some:deep:module`)
         * @param name child project name
         * @param config optional configuration closure
         * @return builder
         */
        ExtendedProjectBuilder childOf(String projectRef, String name, Closure<Project> config = null) {
            assert root != null, "Root project not declared"
            Project parent = projectRef == null ? root : root.project(projectRef)
            File folder = parent.file(name)
            if (!folder.exists()) {
                folder.mkdir()
            }
            Project project = ProjectBuilder.builder()
                    .withName(name)
                    .withProjectDir(folder)
                    .withParent(parent)
                    .build()
            if (config) {
                project.configure(project, config)
            }
            return this
        }

        /**
         * Evaluate configuration.
         *
         * @return root project
         */
        Project build() {
            if (root.subprojects) {
                linkSubprojectsEvaluation(root)
            }
            root.evaluate()
            return root
        }

        private void linkSubprojectsEvaluation(Project project) {
            project.evaluationDependsOnChildren()
            project.subprojects.each { linkSubprojectsEvaluation(it) }
        }
    }
}
