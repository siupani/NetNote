package client.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MarkdownUtilTest {

	// Basic test
	@Test
	public void testParseToHtml() {
		String markdownContent = "This is **bold** and *italic* text.";
		String expectedHtml = "<p>This is <strong>bold</strong> and <em>italic</em> text.</p>\n";

		String actualHtml = MarkdownUtil.parseToHtml(markdownContent);

		assertEquals(expectedHtml, actualHtml, "The HTML output should match the expected output.");
	}
	// Test for empty input
	@Test
	public void testParseToHtml_emptyInput() {
		String markdownContent = "";
		String expectedHtml = "";

		String actualHtml = MarkdownUtil.parseToHtml(markdownContent);

		assertEquals(expectedHtml, actualHtml, "The HTML output for empty markdown should be an empty paragraph.");
	}

	// Test for null input
	@Test
	public void testParseToHtml_nullInput() {
		String markdownContent = null;
		assertThrows(NullPointerException.class, () -> MarkdownUtil.parseToHtml(markdownContent),
			"The method should throw a NullPointerException for null input.");
	}

	// Test for markdown with multiple formats
	@Test
	public void testParseToHtml_multipleFormats() {
		String markdownContent = "# Heading 1\nThis is **bold** and *italic* text.\n## Heading 2";
		String expectedHtml = "<h1>Heading 1</h1>\n<p>This is <strong>bold</strong> and <em>italic</em> text.</p>\n<h2>Heading 2</h2>\n";

		String actualHtml = MarkdownUtil.parseToHtml(markdownContent);

		assertEquals(expectedHtml, actualHtml, "The HTML output for mixed markdown formats should match the expected output.");
	}

	// Test for markdown with invalid syntax
	@Test
	public void testParseToHtml_invalidSyntax() {
		String markdownContent = "**bold text";
		String expectedHtml = "<p>**bold text</p>\n";

		String actualHtml = MarkdownUtil.parseToHtml(markdownContent);

		assertEquals(expectedHtml, actualHtml, "The HTML output should handle unclosed markdown syntax gracefully.");
	}
}
