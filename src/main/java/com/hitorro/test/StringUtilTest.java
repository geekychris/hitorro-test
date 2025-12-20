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
package com.hitorro.test;

import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.html.HTMLEncoder;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;

/**
 * Tests for tricky parts of stringutil
 */
@HTTest(runlevel = RunLevel.Smoke,
        email = "chris@hitorro.com",
        description = "Tests for StringUtil")
public class StringUtilTest extends EnhancedTestCase {
    public void testEncodeUrl() {
        // simple test
        String test = "simple test x.-*y";
        String output = StringUtil.encodeUrl(test);
        assertEquals("simple+test+x.-*y", output);

        test = "%fancy&?()}characters&";
        output = StringUtil.encodeUrl(test);
        assertEquals("%25fancy%26%3F%28%29%7Dcharacters%26", output);
    }

    public void testDecodeUrl() {
        String test = "simple+test+x.-*y";
        String output = StringUtil.decodeUrl(test);
        assertEquals("simple test x.-*y", output);

        test = "%25fancy%26%3F%28%29%7Dcharacters%26";
        output = StringUtil.decodeUrl(test);
        assertEquals("%fancy&?()}characters&", output);
    }

    public void testEncodeHtml() {
        // simple test
        String test = "this & that";
        String output = StringUtil.encodeForXML(test);
        assertEquals("this &amp; that", output);

        // character code and entities at beginning and end
        String fancyQuote = stringFromUnicode(8217);
        test = StringUtil.strcat("<<stuff and nonsense ", fancyQuote, " yow&");
        output = StringUtil.encodeForXML(test);
        assertEquals("&lt;&lt;stuff and nonsense &#8217; yow&amp;", output);

        // no translation at all
        test = "there are no special characters";
        output = StringUtil.encodeForXML(test);
        assertEquals(test, output);

        // empty string
        test = "";
        output = StringUtil.encodeForXML(test);
        assertEquals(test, output);

        // null
        test = null;
        output = StringUtil.encodeForXML(test);
        assertNull(output);
    }

    public void testDecodeHtml() {
        // simple test
        String test = "this &amp; that";
        String output = HTMLEncoder.decodeHtml(test);
        assertEquals("this & that", output);

        // character code and entities at beginning and end
        test = "&lt;&lt;stuff and nonsense &#8217; yow&amp; more &AElig;less&yuml;";
        output = HTMLEncoder.decodeHtml(test);
        String fancyQuote = stringFromUnicode(8217);
        String aelig = stringFromUnicode(198);
        String yuml = stringFromUnicode(255);
        String result = StringUtil.strcat("<<stuff and nonsense ", fancyQuote, " yow& more ", aelig, "less", yuml);
        assertEquals(result, output);

        // no translation at all
        test = "there are no special characters";
        output = HTMLEncoder.decodeHtml(test);
        assertEquals(test, output);

        // empty string
        test = "";
        output = HTMLEncoder.decodeHtml(test);
        assertEquals(test, output);

        // null
        test = null;
        output = HTMLEncoder.decodeHtml(test);
        assertNull(output);
    }

    private String stringFromUnicode(int code) {
        int[] codepoint = new int[1];
        codepoint[0] = code;
        return new String(codepoint, 0, 1);
    }
}
