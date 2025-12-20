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
package com.hitorro.basedms.rss;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * User: chris
 * Date: Oct 13, 2006
 * Time: 12:29:42 PM
 */

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import gnu.trove.set.hash.TLongHashSet;
import com.hitorro.util.bagio.Bag2PrimitiveTMapper;
import com.hitorro.util.bagio.BagLogicalOperator2FieldLogicalOperator;
import com.hitorro.util.bagio.InSetLogicalOperator;
import com.hitorro.util.bagio.LongMapCollector;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.opers.HTPredicate;
import com.hitorro.util.core.opers.LogicalNotOperator;
import com.hitorro.util.core.string.StringBuilderUtil;
import com.hitorro.util.html.HTMLParser;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;
import com.hitorro.util.typesystem.Bag;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.*;
import java.util.Iterator;
import java.util.List;

/**
 * It Reads and prints any RSS/Atom feed type.
 * <p/>
 *
 * @author chris
 */
@HTTest(runlevel = RunLevel.Smoke,
        email = "chris@hitorro.com",
        description = "RSS quick test")
public class RssTest extends EnhancedTestCase {
    public static final String EchoDivider = "-----------------------------------";

    private static String[] testSource = {
            "http://www.engadget.com/rss.xml",  // blogsmith - description is full HTML
            "http://www.npr.org/rss/", // NPR (error)
            "http://feeds.feedburner.com/Techcrunch", // techcrunch
            "http://www.techspot.com/backend.xml", // RSS 0.92 - description is clipped first of article
            "http://www.paidcontent.org/pcorg", // (error) RSS 2.0, from wordpress 2.0.2? - description is full HTML
            "http://scobleizer.wordpress.com/feed/",  // RSS 1.0 from wordpress, description is full HTML of articles  - categories
            "http://scobleizer.wordpress.com/comments/feed/",  // RSS 1.0 from wordpress, description is full HTML of comments
            "http://www.wired.com/rss/index.xml", // wired - description is full HTML
            "http://www.cnn.com/services/rss/", // CNN (error)
            "http://today.reuters.com/rss/default.aspx", // Reuters (error)
            "http://feeds.feedburner.com/crunchgear", // crunchgear (description is clipped first of article)  - categories
            "feed://www.nytimes.com/services/xml/rss/nyt/HomePage.xml", // new york times (unknown protocol: feed)
    };

    private static void xtestCrude(int index) {
        com.hitorro.basedms.rss.RssItemStore store = new Storer();
        com.hitorro.basedms.rss.RssRead reader = new Reader0();

        reader.read(testSource[4], null, store);
    }

    /**
     * read an rss feed into an interator of bags. More of a demonstrator than a test.
     *
     * @throws MalformedURLException
     */
    public void testIter() throws MalformedURLException {
        String scob = "https://rss.art19.com/apology-line";
        com.hitorro.basedms.rss.FeedIterator fi = new com.hitorro.basedms.rss.FeedIterator(new URL(scob), "");
        LongMapCollector collector = new LongMapCollector();
        InSetLogicalOperator so = new InSetLogicalOperator(null);
        HTPredicate<Long> setOper = new LogicalNotOperator(so);
        TLongHashSet s = new TLongHashSet();
        s.add(7579135863635726397l);
        so.setSet(s);

        BagLogicalOperator2FieldLogicalOperator loMapper = new BagLogicalOperator2FieldLogicalOperator("id", setOper, true);
        Bag2PrimitiveTMapper tMapper = new Bag2PrimitiveTMapper("id", collector);

        Iterator<Bag> it = fi.map(com.hitorro.basedms.rss.ItemBeanToBagMapper.map).filter(loMapper).map(tMapper);
        while (it.hasNext()) {
            Bag b = it.next();
            Console.println("%s %s %s", b.getValueAsString("title"), b.getValueAsString("id"), b.getValueAsString("srcid"));
        }
        Console.println("%s", fi.getLastModified());
    }

    public static class Reader0 implements com.hitorro.basedms.rss.RssRead {
        private com.hitorro.basedms.rss.RssItemStore _store;

        public Reader0() {

        }

        public com.hitorro.basedms.rss.FeedFetcherDatum read(String urlString, String lastModified, com.hitorro.basedms.rss.RssItemStore store) {
            _store = store;
            boolean ok = false;
            try {
                URL feedUrl = new URL(urlString);

                SyndFeedInput input = new SyndFeedInput();
                SyndFeed feed = input.build(new XmlReader(feedUrl));

                echoFeed(feed, System.out);

                ok = true;
            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("ERROR: " + ex.getMessage());
            }

            if (!ok) {
                System.out.println("Error reading " + urlString);
            }

            return null;
        }


        private void echoFeed(SyndFeed feed, PrintStream out) {
            String title = feed.getTitle();
            //out.println("----- " + title + "-----");
            //out.println(EchoDivider);
            List entries = feed.getEntries();
            if (entries == null) {
                return;
            }
            int maxEntries = 10;
            int count = 0;
            for (Object oen : entries) {
                SyndEntry entry = (SyndEntry) oen;
                echoEntry(entry);
                if (++count >= maxEntries) {
                    break;
                }
            }
        }

        private void echoEntry(SyndEntry entry) {
            String link = entry.getLink();
            String title = entry.getTitle();
            String description = entry.getDescription().getValue();
            com.hitorro.basedms.rss.ItemBean ib = new com.hitorro.basedms.rss.ItemBean();
            ib.setTitle(title);
            ib.setDescription(description);
            ib.setLink(link);
            _store.store(ib, link);

        }

    }

    private static class Storer implements com.hitorro.basedms.rss.RssItemStore {

        private static final String HTMLContent = "text/html";

        private static HTMLParser parseHtmlFromUrl(URLConnection connection)
                throws IOException {
            InputStream uin = connection.getInputStream();
            StringBuilder htmlString = StringBuilderUtil.readStreamIntoBuilder(uin);

            HTMLParser parser = new HTMLParser();
            parser.setHtmlPage(htmlString.toString());

            return parser;
        }

        public Object store(com.hitorro.basedms.rss.ItemBean ib, String originalUrl) {
            String title = ib.getTitle();
            String description = ib.getDescription();
            String link = ib.getLink();

            PrintStream out = System.out;
            out.println("    " + title);
            out.println("Link: " + link);

            out.println("DESCRIPTION DESCRIPTION");
            out.println(description);
            out.println("-DESCRIPTION DESCRIPTION=");

            try {
                /*
                List es = entry.getLinks();
                System.out.println("links");
                for (Object ex : es) {
                    System.out.println("  type: " + ex.getClassification().getName());
                    System.out.println("   " + ex.toString());

                }
                */
                URI contenturi = new URI(link);
                URL contenturl = contenturi.toURL();
                URLConnection connection = contenturl.openConnection();
                String contentType = connection.getContentType();
                if (contentType != null && contentType.startsWith(HTMLContent)) {
                    // read the content
                    HTMLParser parser = parseHtmlFromUrl(connection);
                    //System.out.println(parser.getBodyText());
                    //System.out.println(" html content...");
                } else {
                    System.out.println("content isn't html");
                }
            } catch (URISyntaxException urise) {
                System.out.println("uri syntax: " + urise);

            } catch (MalformedURLException meu) {
                System.out.println("malformedurl: " + meu);

            } catch (IOException ioe) {
                System.out.println("ioe: " + ioe);
            }

            out.println(EchoDivider);

            return null;
        }
    }

    private static class FeedItemTest implements com.hitorro.basedms.rss.RssItemStore {
        private boolean _gotItems;

        public FeedItemTest() {
            _gotItems = false;
        }

        public Object store(com.hitorro.basedms.rss.ItemBean ib, String originalUrl) {
            _gotItems = true;
            return null;
        }

        public boolean gotItems() {
            return _gotItems;
        }
    }

}