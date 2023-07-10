package ru.vyarus.gradle.frontend.cases

import ru.vyarus.gradle.frontend.core.info.resources.HtmlInfo

/**
 * Same as CssFontsCoreTest but links inside css are not relative, but absolute urls.
 *
 * @author Vyacheslav Rusakov
 * @since 04.07.2023
 */
class HttpCssSubLinksCoreTest extends AbstractCoreTest {

    def "Check css fonts support"() {

        fileFromClasspath('webapp/index.html', '/cases/httpSubResource/httpSubResource.html')
        fileFromClasspath('webapp/materialdesignicons.min.css', '/cases/httpSubResource/materialdesignicons.min.css')

        when: "processing fonts"
        def res = run('webapp')

        then: "optimization done"
        res.getHtmls().size() == 1
        HtmlInfo html = res.getHtmls()[0]
        html.changes == ['changed links', 'minified']
        html.js.size() == 0
        html.css.size() == 1
        with(html.css[0]) {
            changes.size() == 2
            changes.containsAll(['integrity token applied'])
            element.attr('integrity') != null
            gzip != null
            file != null
            // one duplicate resource, but with different source url
            getSubResources().size() == 6
            getSubResources()[0].target.startsWith('resources/materialdesignicons-webfont.eot?')
            getSubResources()[0].gzip != null

            file('webapp/resources/materialdesignicons-webfont.eot').exists()
            file('webapp/materialdesignicons.min.css').text.contains('url("resources/materialdesignicons-webfont.eot?')
        }

        when: "running on already processed"
        String htmlContent = file('webapp/index.html').text
        res = run('webapp')

        then: "no actions performed"
        res.getHtmls().size() == 1
        HtmlInfo html2 = res.getHtmls()[0]
        html2.changes.isEmpty()
        html2.getJs().size() == 0
        html2.css.size() == 1
        with(html2.css[0]) {
            !remote
            changes.size() == 0
            gzip != null
            file != null
        }

        and: "index file not changed"
        htmlContent == file('webapp/index.html').text
    }
}
