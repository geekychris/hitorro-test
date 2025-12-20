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
package com.hitorro.util.urlparser;

import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;

/**
 * ht.util.url.UrlTests Copyright (c) 2003 - present HiTorro All rights reserved. User: chris
 */
@HTTest(runlevel = RunLevel.Smoke,
        email = "chris@hitorro.com",
        description = "Test a variety of URL manipulation routines")
public class UrlTests extends EnhancedTestCase {
    public static String[] UrlsToNorm = {"http://me.staffs.ac.uk/this/is/a/index.html",
            "http://ac.uk/this/is/bogus.html",
            "http://zz.foo"};

    public static String[] UrlDomains = {"uk.ac.staffs",
            "uk.ac",
            "foo.zz"};

    public void testCCTLD() {                               //boolean flipHost, boolean includeHttp, boolean removeWWW
        com.hitorro.util.urlparser.UrlNormalizer norm = new com.hitorro.util.urlparser.UrlNormalizer(true, false, true);
        for (int i = 0; i < UrlsToNorm.length; i++) {
            String normText = norm.normalize(UrlsToNorm[i]);
            String domain = norm.getTLDDomain();
            assertEquals(UrlDomains[i], domain);
            long hash = norm.getTLDDomainHash();
        }
    }

    /**
     * Compute the distance between two url's
     */
    public void testEditDistance() {
        com.hitorro.util.urlparser.UrlEditDistance ued = new com.hitorro.util.urlparser.UrlEditDistance();
        int ord = ued.determineUrlDistance("http://www.cnn.com:8002/world/food/eat.meat.html",
                "http://www.cnn.com:8002/world/health/alergies/hay-fever/thebeesolution.htm");
        assertEquals(8, ord);

        ord = ued.determineUrlDistance("http://www.cnn.com/world/food/eat.meat.html",
                "http://www.cnn.com/world/food/eat.fruit.html");
        assertEquals(2, ord);

        ord = ued.determineUrlDistance("http://www.cnn.com/world/food/eat.meat.html",
                "http://www.cnn.com/world/food/eat.meat.html");
        assertEquals(0, ord);
    }

    public void testUrlMemoryCursor() {
        long h1 = StringUtil.hashStringCaseFree("HeLlO", 0, 5);
        long h2 = StringUtil.hashStringCaseFree("hello");
        // ensure they are the same hash
        assertEquals(h1, h2);

        com.hitorro.util.urlparser.UrlMemoryCursor cur = new com.hitorro.util.urlparser.UrlMemoryCursor();
        // looking for either first part as "feed" or last part of uri as one of n things
        //cur.scan("origin-www.HiTorro.net/parta/partb/wp-rss2.php?category_name=InfoTalk&feed=rss2");
        String u = "www.foobar.net/home/technology/2149/%5C%22http:/media.foobar.net/foy/baseui.gif?p=/aaaa/bbbb/cccc/dddd%5C%22&key=value";
        cur.scan(u);
        assertTrue(cur.resetToFirstPart());

        assertEquals("home", cur.getToken());
        assertEquals(cur.getUrlPartType(), com.hitorro.util.urlparser.UrlCursor.Part.Path);

        assertTrue(cur.resetToLastPart());

        assertEquals("baseui.gif", cur.getToken());
        assertEquals(cur.getUrlPartType(), com.hitorro.util.urlparser.UrlCursor.Part.Path);

        assertTrue(cur.resetToFirstArg());

        assertEquals("p=/aaaa/bbbb/cccc/dddd%5C%22", cur.getToken());
        assertEquals(cur.getUrlPartType(), com.hitorro.util.urlparser.UrlCursor.Part.Argument);

        assertTrue(cur.resetToFirstPart() && cur.nextToken());

        assertEquals("technology", cur.getToken());
        assertEquals(cur.getUrlPartType(), com.hitorro.util.urlparser.UrlCursor.Part.Path);

        assertTrue(cur.resetToFirstArg());

        assertTrue(cur.nextToken());

        assertEquals(cur.getValueHash(), StringUtil.hashStringCaseFree("value"));

        assertTrue(cur.resetToLastPart());

        String s = cur.getToken();
        // should have an extension of gif
        assertTrue(cur.hasExtension());

        // should have the same hash value for "gif"
        assertEquals(cur.getValuePartOfArgToken(), "gif");
        // now the value part is setPhrases to the extension, which should be gif hash
        assertEquals(cur.getValueHash(), StringUtil.hashStringCaseFree("gif"));
    }

    public void testUrlNormalizer() {
        com.hitorro.util.urlparser.UrlNormalizer norm = new com.hitorro.util.urlparser.UrlNormalizer(false, true, false);
        String n = norm.normalize("file:///Volumes/500gb/BOOKSHELF/Cisco_and_Networking/Cisco Press Cisco CCNA Exam 640-607 Certification Guide/content/8-515.pdf");

        assertIt(norm, "http://www.example.com/display?lang=en&article=fred", "http://www.example.com/display?article=fred&lang=en");

        //Converting the scheme and host to lower case. The scheme and host components of the URL are case-insensitive. Most normalizers will convertToPdf them to lowercase. Example:
        assertIt(norm, "HTTP://www.Example.com/", "http://www.example.com/");
        //Adding trailing / Directories are indicated with a trailing slash and should be included in URLs. Example:

        assertIt(norm, "http://www.example.com", "http://www.example.com/");

        //Removing directory Index. Default directory indexes are generally not needed in URLs. Examples:

        assertIt(norm, "http://www.example.com/default.asp", "http://www.example.com/");
        assertIt(norm, "http://www.example.com/a/Index.html", "http://www.example.com/a/");

        //Converting the entire URL to lower case. Some web servers that run on top of case-insensitive file systems allow URLs to be case-insensitive. URLs from a case-insensitive web server may be converted to lowercase to avoid ambiguity. Example:

        assertIt(norm, "http://www.example.com/BAR.html", "http://www.example.com/bar.html");

        //Capitalizing letters in escape sequences. All letters within a percent-encoding triplet (e.g., "%3A") are case-insensitive, and should be capitalized. Example:

        //assertIt("http://www.example.com/a%c2%b1b", "http://www.example.com/a%C2%B1b");
        assertIt(norm, "http://www.example.com/a%c2%b1b", "http://www.example.com/a%c2%b1b");
        //Removing the fragment. The fragment component of a URL is usually removed. Example:

        assertIt(norm, "http://www.example.com/bar.html#section1", "http://www.example.com/bar.html");

        assertIt(norm, "http://www.example.com:80/bar.html", "http://www.example.com/bar.html");


        assertIt(norm, "http://www.example.com/../a/b/../c/./d.html", "http://www.example.com/a/c/d.html");

        assertIt(norm, "http://www.example.com/display?lang=en&article=fred", "http://www.example.com/display?article=fred&lang=en");

        com.hitorro.util.urlparser.UrlNormalizer norm2 = new com.hitorro.util.urlparser.UrlNormalizer(true, false, true);
        assertIt(norm2, "http://www.foo.example.com/display?lang=en&article=fred", "com.example.foo/display?article=fred&lang=en");

    }

    public void assertIt(com.hitorro.util.urlparser.UrlNormalizer norm, String input, String expected) {
        String result = norm.normalize(input);
        assertEquals(expected, result);
    }
}
