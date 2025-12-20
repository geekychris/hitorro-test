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

import com.rometools.rome.feed.synd.*;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@HTTest(runlevel = RunLevel.Smoke,
        email = "chris@hitorro.com",
        description = "Content related tests")
public class FeedRetrievalTest extends EnhancedTestCase {
    public static String tstUrl = "http://tapestryjava.blogspot.com/feeds/posts/default?alt=rss";
    com.hitorro.basedms.rss.FeedRetrieval feedRetrieval = new com.hitorro.basedms.rss.FeedRetrieval();

    /**
     * Case: testSetRequestHeaders<BR> 1. Call method setRequestHeaders<BR> 2. Verify the connection has a user-agent
     * value of "HiTorro Crawler"<BR> 3. Verify the connection has a accept-encoding value of "gzip"<BR>
     */
    public void testSetRequestHeaders() {
        String error = "Unable to create valid connection";
        String expectedResult = "HiTorro Crawler";
        URLConnection connection = null;
        String lastModified = null;
        try {
            connection = new URL(tstUrl).openConnection();
        } catch (IOException e) {
            fail(error);
        }
        //call method
        feedRetrieval.setRequestHeaders(connection, lastModified);
        error = "Invalid user-agent";
        assertEquals(error, expectedResult, connection.getRequestProperty("user-agent"));
        error = "Invalid accept-encoding value";
        expectedResult = "gzip";
        assertEquals(error, expectedResult, connection.getRequestProperty("accept-encoding"));
    }

    /**
     * Case: testGetStream<BR> 1. URL: http://feeds.feedburner.com/MicroPersuasion<BR> 2. Call getStream<BR> 3. Verify
     * returned syndFeed object has at least 1 syndEntry object
     */
    public void testGetStream() {
        String error = "Unable to create valid connection";
        URLConnection connection = null;
        try {
            connection = new URL(tstUrl).openConnection();
        } catch (IOException e) {
            fail(error);
        }
        //call method
        error = "Invalid number of syndEntry objects found";
        SyndFeed syndFeed = feedRetrieval.getStream(connection);
        if (syndFeed.getEntries().size() < 1) {
            fail(error);
        }
    }

    /**
     * Case: testGetStreamGZIP<BR> 1. URL: http://feeds.feedburner.com/MicroPersuasion, accept-encoding: gzip<BR> 2.
     * Call getStream<BR> 3. Verify returned syndFeed object has at least 1 syndEntry object
     */
    public void testGetStreamGZIP() {
        String error = "Unable to create valid connection";
        URLConnection connection = null;
        try {
            connection = new URL(tstUrl).openConnection();
            connection.setRequestProperty("accept-encoding", "gzip");
        } catch (IOException e) {
            fail(error);
        }
        //call method
        error = "Invalid number of syndEntry objects found";
        SyndFeed syndFeed = feedRetrieval.getStream(connection);
        if (syndFeed.getEntries().size() < 1) {
            fail(error);
        }
    }

    /**
     * Case: testGetStreamNonRSS<BR> 1. URL: http://www.yahoo.com<BR> 2. Call getStream<BR> 3. Verify returned syndFeed
     * object is null<BR>
     */
    public void testGetStreamNonRSS() {
        String error = "Unable to create valid connection";
        String uRL = "http://www.yahoo.com";
        URLConnection connection = null;
        try {
            connection = new URL(uRL).openConnection();
        } catch (IOException e) {
            fail(error);
        }
        //call method
        error = "Invalid number of syndEntry objects found";
        SyndFeed syndFeed = feedRetrieval.getStream(connection);
        assertNull(error, syndFeed);
    }

    /**
     * Case: testGetFeed<BR> 1. URL: http://feeds.feedburner.com/MicroPersuasion<BR> 2. Call getStream<BR> 3. Verify
     * returned feedBean object has at least 1 feedItem object
     */
    public void testGetFeed() {
        String error = "Unable to create valid connection";
        com.hitorro.basedms.rss.FeedFetcherDatum feedBean = new com.hitorro.basedms.rss.FeedFetcherDatum();
        try {
            feedBean.setURL(new URL(tstUrl));
        } catch (MalformedURLException e) {
            fail(error);
        }
        //call method
        error = "Invalid number of itemBean objects found";
        com.hitorro.basedms.rss.FeedFetcherDatum feedBeanResult = feedRetrieval.getFeed(feedBean);
        if (feedBeanResult.getItemBeans().size() < 1) {
            fail(error);
        }
    }

    /**
     * Case: testGetFeedContentEncoded<BR>
     * 1. URL: http://www.web-strategist.com/blog/feed<BR>
     * 2. Call getStream<BR>
     * 3. Verify returned feedBean object has at least 1 feedItem object<BR>
     * 4. Verify returned itemBean object has a valid value for content<BR>
     */
    public void testGetFeedContentEncoded() {
        String error = "Unable to create valid connection";
        com.hitorro.basedms.rss.FeedFetcherDatum feedBean = new com.hitorro.basedms.rss.FeedFetcherDatum();
        try {
            feedBean.setURL(new URL(tstUrl));
        } catch (MalformedURLException e) {
            fail(error);
        }
        //call method
        error = "Invalid number of itemBean objects found";
        com.hitorro.basedms.rss.FeedFetcherDatum feedBeanResult = feedRetrieval.getFeed(feedBean);
        if (feedBeanResult.getItemBeans().size() < 1) {
            fail(error);
        }
        error = "Unable to find content:encoded value";
        for (Iterator<com.hitorro.basedms.rss.ItemBean> it = feedBeanResult.getItemBeans().iterator(); it.hasNext(); ) {
            com.hitorro.basedms.rss.ItemBean itemBean = it.next();
            assertNotNull(error, itemBean.getContent());
            break;
        }
    }


    /**
     * Case: testGetFeed<BR> 1. URL: http://feeds.feedburner.com/MicroPersuasion<BR> 2. Call getFeed<BR> 3. Verify
     * returned feedBean object has at least 1 feedItem object<BR> 4. Verify returned feedBean object has a value for
     * lastModified<BR> 5. Call getFeed again with the lastModified value populated<BR> 6. Verify return feedBean object
     * has no feedItem objects<BR> 7. Verify return feedBean object has a responseCode value of 304<BR>
     * <p/>
     * This seems to generally not work with many rss feeds.  Many dont even return a "last modified"
     */
    public void norunGetFeedLastModified() {
        String error = "Unable to create valid connection";
        //String s = "http://feeds.feedburner.com/MicroPersuasion";
        String s = "http://www.google.com/reader/public/atom/user/03845521535364536530/state/com.google/broadcast";
        com.hitorro.basedms.rss.FeedFetcherDatum feedBean = new com.hitorro.basedms.rss.FeedFetcherDatum();
        try {
            feedBean.setURL(new URL(s));
        } catch (MalformedURLException e) {
            fail(error);
        }
        //call method
        error = "Invalid number of itemBean objects found";
        com.hitorro.basedms.rss.FeedFetcherDatum feedBeanResult = feedRetrieval.getFeed(feedBean);
        List<com.hitorro.basedms.rss.ItemBean> l = feedBeanResult.getItemBeans();
        if (l.size() < 1) {
            fail(error);
        }
        error = "LastModified should not be null";
        String lm = feedBeanResult.getLastModified();
        assertNotNull(error, lm);
        //again
        feedBean = new com.hitorro.basedms.rss.FeedFetcherDatum();
        try {
            feedBean.setURL(new URL(s));
        } catch (MalformedURLException e) {
            fail(error);
        }
        String lm2 = feedBeanResult.getLastModified();
        feedBean.setLastModified(lm2);
        error = "No items should be found";
        feedBeanResult = feedRetrieval.getFeed(feedBean);
        l = feedBeanResult.getItemBeans();
        assertNull(error, l);
        error = "responseCode should be 304";
        assertEquals(error, 304, feedBeanResult.getResponseCode());

    }


    /**
     * Case: testGetFeedInvalidURL<BR> 1. URL: http://feeds.feedburner.ccc/MicroPersuasion<BR> 2. Call getFeed<BR> 3.
     * Verify return feedBean object has no feedItem objects<BR> 4. Verify return feedBean object has a responseCode
     * value of 304<BR>
     */
    public void testGetFeedInvalidURL() {
        String error = "Unable to create valid connection";
        String s = "http://feeds.feedburner.com/MicroPersuasionx";
        com.hitorro.basedms.rss.FeedFetcherDatum feedBean = new com.hitorro.basedms.rss.FeedFetcherDatum();
        try {
            feedBean.setURL(new URL(s));
        } catch (MalformedURLException e) {
            fail(error);
        }
        //call method
        error = "feedItems object should be null";
        com.hitorro.basedms.rss.FeedFetcherDatum feedBeanResult = feedRetrieval.getFeed(feedBean);
        assertNull(error, feedBeanResult.getItemBeans());
        error = "responseCode should be 404";
        assertEquals(error, 404, feedBeanResult.getResponseCode());
    }

    /**
     * Case: testGetFeedNullURL<BR> 1. URL: null<BR> 2. Call getFeed<BR> 3. Verify returned FeedFetcherDatum object is
     * null
     */
    public void testGetFeedNullURL() {
        String error = "Unable to create valid connection";
        com.hitorro.basedms.rss.FeedFetcherDatum feedBean = new com.hitorro.basedms.rss.FeedFetcherDatum();
        //call method
        error = "feedBean object should be null";
        com.hitorro.basedms.rss.FeedFetcherDatum feedBeanResult = feedRetrieval.getFeed(feedBean);
        assertNull(error, feedBeanResult);
    }


    /**
     * Case: testSetFeed<BR> 1. Create valid syndFeed object<BR> 2. Call setFeed method<BR> 3. Verify the returned
     * object<BR>
     */
    public void testSetFeed() {
        List<SyndPerson> authors = new ArrayList();
        SyndPersonImpl spi = new SyndPersonImpl();
        spi.setName("author1");
        authors.add(spi);
        SyndCategory syndCategory = new SyndCategoryImpl();
        syndCategory.setName("category1");
        List categories = new ArrayList();
        categories.add(syndCategory);
        String copyright = "Copyright 2006";
        String link = "http://www.junit.org";
        Date pubDate = new Date();
        String title = "a title";
        String imageURL = "http://www.junit.org/images/junitlogo.gif";
        SyndImage syndImage = new SyndImageImpl();
        syndImage.setUrl(imageURL);

        SyndFeed syndFeed = new SyndFeedImpl();
        syndFeed.setAuthors(authors);
        syndFeed.setCategories(categories);
        syndFeed.setCopyright(copyright);
        syndFeed.setLink(link);
        syndFeed.setPublishedDate(pubDate);
        syndFeed.setTitle(title);
        syndFeed.setImage(syndImage);

        String error = "Found invalid ";
        //call
        com.hitorro.basedms.rss.FeedFetcherDatum feedBean = feedRetrieval.setFeed(syndFeed);
        //author
        assertEquals(error + "author", authors.get(0), feedBean.getAuthors().get(0));
        //category
        assertEquals(error + "category", ((SyndCategory) categories.get(0)).getName(),
                feedBean.getCategories().get(0));
        //copyright
        assertEquals(error + "copyright", copyright, feedBean.getCopyright());
        //link
        assertEquals(error + "link", link, feedBean.getLink());
        //pubDate
        assertEquals(error + "pubDate", pubDate, feedBean.getPubDate());
        //title
        assertEquals(error + "title", title, feedBean.getTitle());
        //image
        assertEquals(error + "imageURL", imageURL, feedBean.getImageURL());
    }

    /**
     * Case: testSetFeedNoImageURL<BR> 1. Create valid syndFeed object, with no imageURL<BR> 2. Call setFeed method<BR>
     * 3. Verify the returned object<BR>
     */
    public void testSetFeedNoImageURL() {
        SyndImage syndImage = null;

        SyndFeed syndFeed = new SyndFeedImpl();
        syndFeed.setImage(syndImage);

        String error = "Found invalid ";
        //call
        com.hitorro.basedms.rss.FeedFetcherDatum feedBean = feedRetrieval.setFeed(syndFeed);
        //image
        assertNull(error + "imageURL", feedBean.getImageURL());
    }


    /**
     * Case: testBuildCategories<BR> 1. Create valid SyndCategory objects<BR> 2. Call buildCategories<BR> 3. Verify the
     * object returned is valid
     */
    public void testBuildCategories() {
        String cat1 = "category1";
        String cat2 = "category2";
        List categories = new ArrayList();
        SyndCategory syndCategory = new SyndCategoryImpl();
        syndCategory.setName(cat1);
        categories.add(syndCategory);
        syndCategory = new SyndCategoryImpl();
        syndCategory.setName(cat2);
        categories.add(syndCategory);

        String error = "Invalid category found for ";
        //call
        List categoryList = feedRetrieval.buildCategories(categories);
        assertEquals(error + "cat1", cat1, categoryList.get(0));
        assertEquals(error + "cat2", cat2, categoryList.get(1));
    }

    /**
     * Case: testSetItems<BR> 1. Create valid syndEntries<BR> 2. Call setItems method<BR> 3. Verify the returned object
     * is valid
     */
    public void testSetItems() {
        List items = new ArrayList();
        List<SyndPerson> authors = new ArrayList();
        SyndPerson author = new SyndPersonImpl();
        author.setName("author1");
        authors.add(author);
        SyndCategory syndCategory = new SyndCategoryImpl();
        syndCategory.setName("category1");
        List categories = new ArrayList();
        categories.add(syndCategory);
        String description = "a description";
        SyndContent syndContent = new SyndContentImpl();
        syndContent.setValue(description);
        String link = "http://www.junit.org";
        Date pubDate = new Date();
        String title = "a title";

        SyndEntry syndEntry = new SyndEntryImpl();
        syndEntry.setAuthors(authors);
        syndEntry.setCategories(categories);
        syndEntry.setDescription(syndContent);
        syndEntry.setLink(link);
        syndEntry.setPublishedDate(pubDate);
        syndEntry.setTitle(title);
        items.add(syndEntry);

        String error = "Found invalid";
        List itemBeans = feedRetrieval.setItems(items, "http://mysrc.com");

        for (Iterator it = itemBeans.iterator(); it.hasNext(); ) {
            com.hitorro.basedms.rss.ItemBean itemBean = (com.hitorro.basedms.rss.ItemBean) it.next();
            //author
            assertEquals(error + "author", authors.get(0).getName(), itemBean.getAuthors().get(0));
            //category
            assertEquals(error + "category", ((SyndCategory) categories.get(0)).getName(),
                    itemBean.getCategories().get(0));
            //description
            assertEquals(error + "description", description, itemBean.getDescription());
            //link
            assertEquals(error + "link", link, itemBean.getLink());
            //pubDate
            assertEquals(error + "pubDate", pubDate, itemBean.getPubDate());
            //title
            assertEquals(error + "title", title, itemBean.getTitle());
            break;
        }
    }


    /**
     * Case: testSetItemsNullDescription<BR> 1. Create valid syndEntry with a null Description<BR> 2. Call setItems
     * method<BR> 3. Verify the returned object is valid
     */
    public void testSetItemsNullDescription() {
        List items = new ArrayList();

        SyndEntry syndEntry = new SyndEntryImpl();
        syndEntry.setDescription(null);
        items.add(syndEntry);

        String error = "Found invalid";
        List itemBeans = feedRetrieval.setItems(items, "http://mysrc.com");

        for (Iterator it = itemBeans.iterator(); it.hasNext(); ) {
            com.hitorro.basedms.rss.ItemBean itemBean = (com.hitorro.basedms.rss.ItemBean) it.next();
            //description
            assertNull(error + "description", itemBean.getDescription());
            break;
        }
    }

}
