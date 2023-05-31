package ru.vyarus.gradle.frontend.core.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Range;
import org.jsoup.parser.Parser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * https://jsoup.org/
 *
 * @author Vyacheslav Rusakov
 * @since 09.02.2023
 */
public final class HtmlParser {

    private HtmlParser() {
    }

    public static ParseResult parse(File file) {
        try {
            final List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            final Parser parser = Parser.htmlParser();
            // required to extract exact source location
            parser.setTrackPosition(true);
            final Document doc = Jsoup.parse(file, StandardCharsets.UTF_8.name(), file.getAbsolutePath(), parser);
            final List<SourceElement> css = new ArrayList<>();
            // ignore icon links
            doc.select("link[href]").forEach(element -> {
                if ("stylesheet".equalsIgnoreCase(element.attr("rel"))) {
                    css.add(new SourceElement(element, elementSource(lines, element)));
                }
            });

            final List<SourceElement> js = new ArrayList<>();
            doc.select("script[src]").forEach(element -> js.add(
                    new SourceElement(element, elementSource(lines, element))));

            return new ParseResult(doc, css, js);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse html: " + file.getAbsolutePath(), e);
        }
    }

    private static String elementSource(final List<String> lines, final Element element) {
        final Range.Position start = element.sourceRange().start();
        // it should be endSourceRange() for elements with closing tags (script), but jsoup does not track it
        final Range.Position end = element.sourceRange().end();
        // going from end because it is simpler to cut-off line end first (when both points on the same line)
        int endLine = end.lineNumber() - 1;
        final List<String> res = new ArrayList<>();
        res.add(lines.get(endLine).substring(0, end.columnNumber() - 1));

        while (endLine > start.lineNumber() - 1) {
            // adding lines between start and end (-1 not required!)
            res.add(lines.get(--endLine));
        }

        final String startLine = res.remove(res.size() - 1);
        res.add(startLine.substring(start.columnNumber() - 1));
        Collections.reverse(res);
        return String.join(System.lineSeparator(), res);
    }

    public static class ParseResult {
        private final Document document;
        private final List<SourceElement> css;
        private final List<SourceElement> js;

        public ParseResult(final Document document, final List<SourceElement> css, final List<SourceElement> js) {
            this.document = document;
            this.css = css;
            this.js = js;
        }

        public Document getDocument() {
            return document;
        }

        public List<SourceElement> getCss() {
            return css;
        }

        public List<SourceElement> getJs() {
            return js;
        }
    }

    public static class SourceElement {
        private Element element;
        private String source;

        public SourceElement(Element element, String source) {
            this.element = element;
            this.source = source;
        }

        public Element getElement() {
            return element;
        }

        public String getSource() {
            return source;
        }
    }
}
