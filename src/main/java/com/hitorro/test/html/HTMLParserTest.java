/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.hitorro.test.html;

import com.hitorro.util.core.Console;
import com.hitorro.util.core.ListUtil;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringBuilderUtil;
import com.hitorro.util.html.constraint.TypeLinkConstraint;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Oct 15, 2006 Time: 5:42:53 PM
 */
@HTTest(runlevel = RunLevel.Smoke,
        email = "chris@hitorro.com",
        description = "Test HTML parser")
public class HTMLParserTest extends EnhancedTestCase {
    public static final String D = "Robert Scoble zooomr photowalking zone how zone job zone more youtube so";
    private static final String Redirect = "<META HTTP-EQUIV=\"Refresh\"\n" +
            "      CONTENT=\"5; URL=html-redirect.html\">";

    private String getTestBuffer(String file) throws IOException {
        File f = this.getInputFileRelative(file);

        StringBuilder sb = new StringBuilder();
        String s = null;
        StringBuilderUtil.readFileIntoBuilder(sb, f);
        return sb.toString();
    }

    /**
     * Test the basics of the TagIterator.  Its job is to allow fast scanning of elements of xml that are represented in
     * a string no strings are harmed in the process thus making it somewhat faster for simple uses. The second
     * advantage is the ability todo case incensative matches.
     * <p/>
     * This test ensures that we can find entities by a specific name and attributes.  Attributes must support cases
     * where they have values (content ="") and ones when they dont, such as <!DOCTYPE html ....
     */
    public void testTagIterator() {
        File f = this.getInputFileRelative("test1.html");

        StringBuilder sb = new StringBuilder();
        String s = null;
        try {
            StringBuilderUtil.readFileIntoBuilder(sb, f);
            s = sb.toString();

        } catch (IOException e) {
            this.assertAlways("Unable to read html file %s %e", e, e);
        }
        com.hitorro.util.html.TagFinder tf = new com.hitorro.util.html.TagFinder();
        tf.set(s, 3000);

        /*
        Test that we can get non key=value pairs from the doctype
         */
        boolean firstMeta = true;
        boolean foundHtmlAttr = false;
        while (tf.next()) {
            if (tf.isTagEqualIgnoreCase("!DoCtYpE")) {
                if (tf.findAttribute("html")) {
                    foundHtmlAttr = true;
                }


            }
            if (tf.isTagEqualIgnoreCase("MeTa")) {
                if (firstMeta) {
                    firstMeta = false;
                    if (tf.findAttribute("ConTenT")) {
                        String content = tf.getAttributeValue();
                        assertNotNull(content);
                        assertEquals(content, "text/html; charset=UTF-8");
                    }
                }

            }
        }

        assertTrue(foundHtmlAttr);

    }

    /**
     * Test basic testing of sniffing content types.
     */
    public void testSniffing() throws IOException {
        String rssBuff = getTestBuffer("rss.htm");
        String htmlBuff = getTestBuffer("html.htm");
        String atomBuff = getTestBuffer("atom.htm");
        String styleBuff = getTestBuffer("style.htm");

        assertEquals(com.hitorro.util.html.contentsniffers.RssSniffer.MIME, com.hitorro.util.html.contentsniffers.ContentSniffers.getSniffers().getMimeType(rssBuff, null));
        assertEquals(com.hitorro.util.html.contentsniffers.HtmlSniffer.MIME, com.hitorro.util.html.contentsniffers.ContentSniffers.getSniffers().getMimeType(htmlBuff, null));
        assertEquals(com.hitorro.util.html.contentsniffers.AtomSniffer.MIME, com.hitorro.util.html.contentsniffers.ContentSniffers.getSniffers().getMimeType(atomBuff, null));
        assertEquals(com.hitorro.util.html.contentsniffers.StyleSheetSniffer.MIME, com.hitorro.util.html.contentsniffers.ContentSniffers.getSniffers().getMimeType(styleBuff, null));
    }

    public void testParsing() {
        File f = this.getInputFileRelative("test1.html");
        File canon = this.getInputFileRelative("test1.canon");
        File out = this.getOutputFileRelative("test1.out");
        this.assertFileExistsWithExplanation(f);
        com.hitorro.util.html.HTMLParser parser = new com.hitorro.util.html.HTMLParser();
        StringBuilder sb = new StringBuilder();
        List<com.hitorro.util.html.Link> links = null;
        try {
            StringBuilderUtil.readFileIntoBuilder(sb, f);
            String s = sb.toString();
            parser.setHtmlPage(s);
            links = parser.getLinks(com.hitorro.util.html.HTMLParser.PAGE_HTML, new URL("http://www.hibernate.com"));
        } catch (IOException e) {
            this.assertAlways("Unable to read html file %s %e", e, e);
        }
        sb.setLength(0);
        Console.bprintln(sb, "Body Text");
        StringBuilderUtil.appendIfNotNull(sb, parser.getBodyText());
        Console.bprintln(sb, "Title Text");
        StringBuilderUtil.appendIfNotNull(sb, parser.getTitleText());
        Console.bprintln(sb, "Meta Description");
        Console.bprintln(sb, parser.getMetaDescription());
        Console.bprintln(sb, "Meta Keywords");
        Console.bprintln(sb, parser.getMetaKeywords());
        try {
            FileUtil.writeToFileUsingPrintWriter(out, sb, "UTF-8");
        } catch (IOException e) {
            this.assertAlways("Unable to write test1 output file", e, e);
        }
        this.assertLinesOfFilesEqual("Canon files not same", out, canon, "UTF-8");
    }

    public void testRedirect() throws IOException {
        com.hitorro.util.html.HTMLParser parser = new com.hitorro.util.html.HTMLParser();
        parser.setHtmlPage(Redirect);
        String redirectedUrl = parser.getRedirectURL("http://foo");
        assertEquals("http://foo/html-redirect.html", redirectedUrl);
    }

    public void testFetchHTML() throws IOException {
        com.hitorro.util.html.HTMLPageFetcher fetcher = new com.hitorro.util.html.HTMLPageFetcher();
        fetcher.setHttpTimeout(10000);
        com.hitorro.util.html.HTMLPage page = fetcher.fetchPage("https://www.cnn.com/");

        assertNotNull(page);
        assertNotNull(page.getSource());
        String ct = page.getContentTypeFromContent();
        // make sure we sniffed it as html
        assertEquals(com.hitorro.util.html.contentsniffers.HtmlSniffer.MIME, ct);
        List<com.hitorro.util.html.Link> links = page.getLinks();
        String u = page.getDestinationUrl();
        // XXX TODO this is fickel  every time cnn changes their url we are messed up
        assertEquals("https://www.cnn.com/", u);
    }

    public void testPingback() throws IOException {
        com.hitorro.util.html.HTMLPageFetcher fetcher = new com.hitorro.util.html.HTMLPageFetcher();
        fetcher.setHttpTimeout(10000);
        com.hitorro.util.html.HTMLPage page;
        List<com.hitorro.util.html.Link> links;
        page = fetcher.fetchPage("http://billstclair.com/html-redirect.html");
        assertNotNull(Fmt.S("HTMLPageFetcher return code: %s", fetcher.getCode()), page);
        assertNotNull(page.getSource());
        links = page.getLinks();
        assertEquals("http://billstclair.com/html-redirect.html", page.getDestinationUrl());
        String pingbackurl = page.getPingBack(true);
        assertNull(pingbackurl);
    }

    public void testAlternateURL() throws IOException {
        com.hitorro.util.html.HTMLPageFetcher fetcher = new com.hitorro.util.html.HTMLPageFetcher();
        fetcher.setHttpTimeout(10000);
        com.hitorro.util.html.HTMLPage page;
        List<com.hitorro.util.html.Link> links;
        String u;
        String pingbackurl;
        page = fetcher.fetchPage("https://www.cnn.com/");
        assertNotNull(page);
        assertNotNull(page.getSource());
        links = page.getLinkWithConstraint(new TypeLinkConstraint(com.hitorro.util.html.Link.LinkType.Alternate));
        if (ListUtil.nullOrEmpty(links)) {
            assertAlways("We expect to get at least one alternate address assuming we fetched the page");
        }
        u = page.getDestinationUrl();

        assertStartsWith("Should start with an techcrunch string", u, "https://www.cnn.com");
        pingbackurl = page.getPingBack(true);
    }
}