package ru.vyarus.gradle.frontend.cases

import ru.vyarus.gradle.frontend.core.info.HtmlInfo

/**
 * @author Vyacheslav Rusakov
 * @since 11.05.2023
 */
class NonHtmlTemplateCoreTest extends AbstractCoreTest {

    def "Check jte processing"() {

        file('webapp/index.jsp') << """@import org.example.Page

@param Page page
<!DOCTYPE html>
<html>
<head>
    @if(page.getDescription() != null)
        <meta name="description" content="\${page.getDescription()}">
        <link rel="stylesheet" href="1.css">
    @endif       
    <link rel="icon" src="my.ico">
    <link rel="icon" href="favicon.png" sizes="16x16" type="image/png">    
    <script src="1.js"></script>    
    <title>\${page.getTitle()}</title> 
</head>
<body>
<link rel="stylesheet" href="2.css">
<script src="2.js"></script>
</body>
</html>
"""
        file('webapp/1.js') << "// empty"
        file('webapp/1.css') << "/* empty */"

        // should not be detected
        file('webapp/index.html') << 'empty'

        when: "processing"
        def res = run(builder('webapp')
                .htmlExtensions('jsp')
        .minifyHtml(false))

        then: "optimization done"
        res.getHtmls().size() == 1
        HtmlInfo html = res.getHtmls()[0]
        html.file.name == 'index.jsp'
        html.changes == ['changed links']
        html.js.size() == 2
        html.css.size() == 2
    }

    def "Check jte processing with minification"() {

        file('webapp/index.jsp') << """@import org.example.Page

@param Page page
<!DOCTYPE html>
<html>
<head>
    @if(page.getDescription() != null)
        <meta name="description" content="\${page.getDescription()}">
        <link rel="stylesheet" href="1.css">
    @endif       
    <link rel="icon" src="my.ico">
    <link rel="icon" href="favicon.png" sizes="16x16" type="image/png">    
    <script src="1.js"></script>    
    <title>\${page.getTitle()}</title> 
</head>
<body>
<link rel="stylesheet" href="2.css">
<script src="2.js"></script>
</body>
</html>
"""
        file('webapp/1.js') << "// empty"
        file('webapp/1.css') << "/* empty */"

        // should not be detected
        file('webapp/index.html') << 'empty'

        when: "processing"
        def res = run(builder('webapp')
                .htmlExtensions('jsp')
                .minifyHtml(true))

        then: "optimization done"
        res.getHtmls().size() == 1
        HtmlInfo html = res.getHtmls()[0]
        html.file.name == 'index.jsp'
        html.changes == ['changed links', 'minified']
        html.js.size() == 2
        html.css.size() == 2
    }
}
