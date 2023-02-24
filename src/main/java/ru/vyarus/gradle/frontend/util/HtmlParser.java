package ru.vyarus.gradle.frontend.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Vyacheslav Rusakov
 * @since 09.02.2023
 */
public final class HtmlParser {

    private HtmlParser() {
    }

    public static ParseResult parse(File file) {
        try {
            final Document doc = Jsoup.parse(file);
            final List<Element> css = new ArrayList<>();
            // ignore icon links
            doc.select("link[href]").forEach(element -> {
                if ("stylesheet".equalsIgnoreCase(element.attr("rel"))) {
                    css.add(element);
                }
            });

            final List<Element> js = new ArrayList<>(doc.select("script[src]"));

            return new ParseResult(doc, css, js);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse html: " + file.getAbsolutePath(), e);
        }
    }

    public static class ParseResult {
        private final Document document;
        private final List<Element> css;
        private final List<Element> js;

        public ParseResult(final Document document, final List<Element> css, final List<Element> js) {
            this.document = document;
            this.css = css;
            this.js = js;
        }

        public Document getDocument() {
            return document;
        }

        public List<Element> getCss() {
            return css;
        }

        public List<Element> getJs() {
            return js;
        }
    }
}
