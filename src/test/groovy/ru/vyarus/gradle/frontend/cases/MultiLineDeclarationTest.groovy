package ru.vyarus.gradle.frontend.cases

import ru.vyarus.gradle.frontend.core.info.resources.HtmlInfo

/**
 * @author Vyacheslav Rusakov
 * @since 25.09.2023
 */
class MultiLineDeclarationTest extends AbstractCoreTest {

    def "Check multiline declarations processing"() {

        fileFromClasspath('webapp/index.html', '/cases/multiLineDeclaration.html')

        when: "processing bootstrap application"
        def res = run('webapp')

        then: "optimization done"
        res.getHtmls().size() == 1
        HtmlInfo html = res.getHtmls()[0]
        html.changes == ['changed links', 'minified']
        html.js.size() == 1
        html.pureHtml
        html.parsedDocument != null
        with(html.js[0]) {
            remote
            changes.containsAll(['crossorigin removed', 'integrity token applied'])
            target.startsWith("js/bootstrap.bundle.min.js?")
        }
        html.css.size() == 1
        with(html.css[0]) {
            remote
            changes.containsAll(['crossorigin removed', 'integrity token applied'])
            target.startsWith("css/bootstrap.min.css?")
        }
        !html.file.text.contains('https://cdn.jsdelivr.net')
    }

    def "Check multiline declarations processing with forced linux format"() {

        fileFromClasspathLF('webapp/index.html', '/cases/multiLineDeclaration.html')

        when: "processing bootstrap application"
        def res = run('webapp')

        then: "optimization done"
        res.getHtmls().size() == 1
        HtmlInfo html = res.getHtmls()[0]
        html.changes == ['changed links', 'minified']
        html.js.size() == 1
        html.css.size() == 1
        !html.file.text.contains('https://cdn.jsdelivr.net')
    }
}
