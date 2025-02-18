package client.utils;

import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class MarkdownUtil {

    /**
     * Constructor
     */
    public MarkdownUtil() {
    }

    /**
     * Method which parses the content typed in the Markdown
     * renderer into HTML
     * This is taken from the -commonmark-java- library in the product backlog
     *
     * @param markdownContent - string to be processed.
     * @return - markdown content in HTML format
     */
    public static String parseToHtml(String markdownContent) {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(markdownContent);
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        return renderer.render(document);  // "<p>This is <em>Markdown</em></p>\n"
    }

    /**
     * Method to render the HTML without a CSS file
     *
     * @param markdownContent - string to be processed
     * @param webView         - webView to be used
     */
    public void renderMarkdown(String markdownContent, WebView webView) {
        String htmlContent = parseToHtml(markdownContent);
        WebEngine webEngine = webView.getEngine();
        webEngine.loadContent(htmlContent);
    }

    /**
     * Method to render the HTML using a CSS file
     *
     * @param markdownContent - string to be processed
     * @param webView         - webView to be used
     */
    public void renderMarkdownInWebView(String markdownContent, WebView webView) {

        String htmlContent = parseToHtml(markdownContent);

        WebEngine webEngine = webView.getEngine();
        webEngine.loadContent(htmlContent);

        URL cssFileUrl = MarkdownUtil.class.getResource("/css/markdown-style.css");
        if (cssFileUrl != null) {
            // Add the CSS link to the HTML content
            String cssLink = "<link rel='stylesheet' type='text/css' href='" + cssFileUrl + "' />";

            // Load the CSS into the WebView with the HTML
            webEngine.loadContent(cssLink + htmlContent);
        } else {
            System.err.println("CSS file not found.");
        }
    }

    /**
     * Method to generate a temporary HTML file on disk
     *
     * @param webView - webView to be used
     */
    public void generateTemporaryHTML(WebView webView) throws IOException {
        WebEngine webEngine = webView.getEngine();
        String markdownContent = (String) webEngine.executeScript("document.documentElement.outerHTML");
        String htmlContent = parseToHtml(markdownContent);

        Path tempFile = Files.createTempFile("markdown_renderer", ".html");

        try (BufferedWriter writer = Files.newBufferedWriter(tempFile)) {
            writer.write(htmlContent);
        }

        tempFile.toFile().deleteOnExit();
    }
}