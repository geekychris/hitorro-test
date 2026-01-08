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
package com.hitorro.test.dms;

import com.hitorro.basedms.SystemVersionException;
import com.hitorro.basedms.cache.ContentTypeCache;
import com.hitorro.basedms.session.DMSSessionFactory;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.Log;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;
import com.hitorro.util.typesystem.BaseSession;
import com.hitorro.util.typesystem.HTSerializable;
import com.hitorro.util.versioning.VersioningUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * object versioning.
 */
@HTTest(runlevel = RunLevel.Smoke,
        email = "chris@hitorro.com",
        description = "Content related tests")
public class VersioningTest extends EnhancedTestCase {
    /**
     * Test major, minor and branch label names.
     */
    public void testDefaultStoreContent() {
        assertEquals(VersioningUtil.getMajorVersion("1.0"), "2.0");
        assertEquals(VersioningUtil.getMinorVersion("2.0"), "2.1");
        assertEquals(VersioningUtil.getMajorVersion("2.1"), "3.0");
        assertEquals(VersioningUtil.getMinorVersion("3.1"), "3.2");
        assertEquals(VersioningUtil.getBranch("3.1"), "3.1.1.0");
        assertEquals(VersioningUtil.getMajorVersion("3.1.1.0"), "3.1.2.0");
        assertEquals(VersioningUtil.getMinorVersion("3.1.2.0"), "3.1.2.1");
        assertEquals(VersioningUtil.getMajorVersion("3.1.2.1"), "3.1.3.0");
        assertEquals(VersioningUtil.getBranch("3.1.2.1"), "3.1.2.1.1.0");
    }

    /**
     * Verify that we can retrieve a subclass albeit using some stupid helper function that has to dereference any proxies if they
     * exist.
     */
    public void testInheritence() {
        BaseSession session = DMSSessionFactory.getFactory().getSession();
        com.hitorro.base.objects.Container c = new com.hitorro.base.objects.Container(com.hitorro.base.objects.Document.class, "id");
        session.persist(c);
        com.hitorro.base.objects.Post doc = createFourPartPost(session);
        assertNotNull(doc);
        String chris = "http://chriswashere2.com";
        doc.setSiteLink(chris);
        doc.getContainers().add(c);

        //session.persist(c);
        session.commit();
        String guid = c.getGuid();

        DMSSessionFactory.getFactory().commitAndCloseSession(session);

        BaseSession s2 = DMSSessionFactory.getFactory().getSession();
        c.setSession(s2);

        Iterator<Object[]> soIter = c.getIterator();
        while (soIter.hasNext()) {
            Object a = soIter.next();
            com.hitorro.base.objects.VersionableObject so = (com.hitorro.base.objects.VersionableObject) session.getNonProxyObject(((HTSerializable) a));
            com.hitorro.base.objects.Post p = (com.hitorro.base.objects.Post) so;
            assertEquals(p.getSiteLink(), chris);
        }
    }

    /**
     * Verify we can make some major and minor versions and that their version labels are correct.
     */
    public void testVersioning() throws IOException, StoreException {
        BaseSession session = DMSSessionFactory.getFactory().getSession();
        com.hitorro.base.objects.Document doc = createFourPartDoc(session);
        assertNotNull(doc);
        List<String> expectedVersions = new ArrayList<String>();
        expectedVersions.add("1.0");
        try {
            com.hitorro.base.objects.Document major = (com.hitorro.base.objects.Document) doc.createMajorVersion();
            session.persist(major);
            expectedVersions.add("2.0");

            com.hitorro.base.objects.Document m2 = (com.hitorro.base.objects.Document) major.createMajorVersion();
            // break a's file at this point
            BaseFile aFile = getInputFileRelativeBaseFile("multcontent/a.html");
            com.hitorro.base.objects.ContentType ct = ContentTypeCache.getCache().getContentTypeByMimeType("application/msword");
            m2.setContent("a.html", ct, aFile);
            expectedVersions.add("3.0");
            session.persist(m2);

            com.hitorro.base.objects.Document m3 = (com.hitorro.base.objects.Document) m2.createMinorVersion();
            expectedVersions.add("3.1");
            // break a's file at this point
            BaseFile bFile = getInputFileRelativeBaseFile("multcontent/b.html");
            m3.setContent("b.html", ct, bFile);
            session.persist(m3);

            com.hitorro.base.objects.Document m4 = (com.hitorro.base.objects.Document) m3.createMinorVersion();
            expectedVersions.add("3.2");
            session.persist(m4);

            DMSSessionFactory.getFactory().commitAndCloseSession(session);

        } catch (SystemVersionException e) {
            assertAlways("Unable to create major version");
        }

        // now verify we have just the right amount of versions and their version labels.
        com.hitorro.base.objects.Document curr = doc;
        for (String v : expectedVersions) {
            assertNotNull(curr);
            assertEquals(curr.getVersionLabel(), v);
            curr = (com.hitorro.base.objects.Document) curr.getNextVersion();
        }
        // we dont expect any more babies!
        assertNull(curr);
    }

    private void dumpVersionInfo(com.hitorro.base.objects.Document doc) {
        Console.println("This %s, canonical %s, label %s", doc.getGuid(), doc.getCanonicalGuid(), doc.getVersionLabel());
    }

    private com.hitorro.base.objects.Document createFourPartDoc(BaseSession sess) {
        BaseFile a = getInputFileRelativeBaseFile("multcontent/a.html");
        BaseFile b = getInputFileRelativeBaseFile("multcontent/b.html");
        BaseFile c = getInputFileRelativeBaseFile("multcontent/c.html");
        BaseFile d = getInputFileRelativeBaseFile("multcontent/d.html");

        com.hitorro.base.objects.Document doc = new com.hitorro.base.objects.Document();
        doc.setContent("ContentString");
        doc.setNote("Document Note");
        doc.setTitle("This is my document title");
        com.hitorro.base.objects.ContentType ct = ContentTypeCache.getCache().getContentTypeByMimeType("application/msword");
        try {
            doc.setContent("a.html", ct, a);
            doc.setContent("b.html", ct, b);
            doc.setContent("c.html", ct, c).setContentRendition(sess, ct, d, "320x240");
            doc.setContent("d.html", ct, d);
        } catch (IOException e) {
            assertAlways("Got IOException");
        } catch (StoreException e) {
            assertAlways("Got StoreException");
        } catch (Exception e) {
            Log.util.error("%s %e", e, e);
        }
        sess.persist(doc);
        sess.commit();
        return doc;
    }

    private com.hitorro.base.objects.Post createFourPartPost(BaseSession sess) {
        BaseFile a = getInputFileRelativeBaseFile("multcontent/a.html");
        BaseFile b = getInputFileRelativeBaseFile("multcontent/b.html");
        BaseFile c = getInputFileRelativeBaseFile("multcontent/c.html");
        BaseFile d = getInputFileRelativeBaseFile("multcontent/d.html");

        com.hitorro.base.objects.Post doc = new com.hitorro.base.objects.Post();
        doc.setContent("ContentString");
        doc.setNote("Document Note");
        doc.setTitle("This is my document title");
        com.hitorro.base.objects.ContentType ct = ContentTypeCache.getCache().getContentTypeByMimeType("application/msword");
        try {
            doc.setContent("a.html", ct, a);
            doc.setContent("b.html", ct, b);
            doc.setContent("c.html", ct, c).setContentRendition(sess, ct, d, "320x240");
            doc.setContent("d.html", ct, d);
        } catch (IOException e) {
            assertAlways("Got IOException");
        } catch (StoreException e) {
            assertAlways("Got StoreException");
        } catch (Exception e) {
            Log.util.error("%s %e", e, e);
        }
        sess.persist(doc);
        return doc;
    }


}
