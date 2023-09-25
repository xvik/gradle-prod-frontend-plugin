package ru.vyarus.gradle.frontend.util

import ru.vyarus.gradle.frontend.core.util.HtmlParser
import spock.lang.Specification
import spock.lang.TempDir

/**
 * @author Vyacheslav Rusakov
 * @since 09.02.2023
 */
class HtmlParserTest extends Specification {

    @TempDir File dir

    def "Check links parse"() {

        File index = new File(dir, "index.html")
        index << """<!DOCTYPE html>
<html>
<head>       
    <link rel="icon" src="my.ico">
    <link rel="icon" href="favicon.png" sizes="16x16" type="image/png">
    <link rel="stylesheet" href="1.css">
    <script src="1.js"></script>     
</head>
<body>
<link rel="stylesheet" href="2.css">
<script src="2.js"></script>
</body>
</html>
"""

        when:
        def res = HtmlParser.parse(index)
        then:
        res.document != null
        res.css.size() == 2
        res.js.size() == 2
        res.css.collect { it.element.attr('href')} == ['1.css', '2.css']
        res.js.collect { it.element.attr('src')} == ['1.js', '2.js']
    }

    def "Check links source extraction"() {

        File index = new File(dir, "index.html")
        index << """<!DOCTYPE html>
<html>
<head>       
    <link rel="icon" src="my.ico">
    <link rel="icon" href="favicon.png" sizes="16x16" type="image/png">
    <link rel="stylesheet" 
            href="1.css">
    <script   src="1.js"></script>     
</head>
<body>
</body>
</html>
"""

        when:
        def res = HtmlParser.parse(index)
        then:
        res.document != null
        res.css.size() == 1
        res.js.size() == 1
        unifyString(res.css[0].source) == """<link rel="stylesheet" 
            href="1.css">"""
        unifyString(res.js[0].source) == '<script   src="1.js">' // without </script> !!


        def text = index.text
        and:
        // text would be read as LF, but html parser would parse it as CRLF - test-only issue
        text.contains(unifyString(res.css[0].source))
        text.contains(unifyString(res.js[0].source))
    }

    def "Check jsp support"() {

        File index = new File(dir, "index.jsp")
        index << """<%@ page language="java" contentType="text/html; charset=US-ASCII"
    pageEncoding="US-ASCII"%>
<!DOCTYPE html>
<html>
<head>       
    <link rel="icon" src="my.ico">
    <link rel="icon" href="favicon.png" sizes="16x16" type="image/png">
    <link rel="stylesheet" href="1.css">
    <script src="1.js"></script>     
</head>
<%@ page import="java.util.Date" %>
<body>
<link rel="stylesheet" href="2.css">
<strong>Current time</strong>: <%=new Date() %>
<script src="2.js"></script>
</body>
</html>
"""

        when:
        def res = HtmlParser.parse(index)
        then:
        res.document != null
        res.css.size() == 2
        res.js.size() == 2
        res.css.collect { it.element.attr('href')} == ['1.css', '2.css']
        res.js.collect { it.element.attr('src')} == ['1.js', '2.js']

        def text = index.text
        and:
        text.contains(res.css[0].source)
        text.contains(res.css[1].source)
        text.contains(res.js[0].source)
        text.contains(res.js[1].source)
    }

    def "Check jte support"() {

        File index = new File(dir, "index.jte")
        index << """@import org.example.Page

@param Page page
<!DOCTYPE html>
<html>
<head>
    @if(page.getDescription() != null)
        <meta name="description" content="\${page.getDescription()}">
    @endif       
    <link rel="icon" src="my.ico">
    <link rel="icon" href="favicon.png" sizes="16x16" type="image/png">
    <link rel="stylesheet" href="1.css">
    <script src="1.js"></script>    
    <title>\${page.getTitle()}</title> 
</head>
<body>
<link rel="stylesheet" href="2.css">
<script src="2.js"></script>
</body>
</html>
"""

        when:
        def res = HtmlParser.parse(index)
        then:
        res.document != null
        res.css.size() == 2
        res.js.size() == 2
        res.css.collect { it.element.attr('href')} == ['1.css', '2.css']
        res.js.collect { it.element.attr('src')} == ['1.js', '2.js']

        def text = index.text
        and:
        text.contains(res.css[0].source)
        text.contains(res.css[1].source)
        text.contains(res.js[0].source)
        text.contains(res.js[1].source)

    }

    protected String unifyString(String input) {
        return input
        // cleanup win line break for simpler comparisons
                .replace("\r", '')
                .replace('\\', '/')
    }
}
