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
        res.css.collect { it.attr('href')} == ['1.css', '2.css']
        res.js.collect { it.attr('src')} == ['1.js', '2.js']
    }
}
