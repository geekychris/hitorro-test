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

import com.hitorro.basedms.StoreUtil;
import com.hitorro.basedms.cache.ContentTypeCache;
import com.hitorro.basedms.contentconstraints.FileNameMatchContentConstraint;
import com.hitorro.basedms.contentconstraints.ResolutionConstraint;
import com.hitorro.basedms.contentconstraints.TagConstraint;
import com.hitorro.basedms.exceptions.CategoryException;
import com.hitorro.basedms.session.DMSSession;
import com.hitorro.basedms.session.DMSSessionFactory;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.fs.file.FileFileSystem;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.Timer;
import com.hitorro.util.core.opers.HTPredicate;
import com.hitorro.util.core.opers.LogicalAndOperator;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;
import com.hitorro.util.typesystem.BaseSession;
import com.hitorro.util.typesystem.BaseSessionFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;


@HTTest(runlevel = RunLevel.Smoke,
        email = "chris@hitorro.com",
        description = "Content related tests",
        dependentServices = {com.hitorro.base.objects.BaseDMSService.class})
public class ContentTest extends EnhancedTestCase {
    public void testNonManagedFileSystem() {
        BaseSession sess = DMSSessionFactory.getFactory().getSession();
        com.hitorro.base.objects.Post p = new com.hitorro.base.objects.Post();
        File testFile = new File(Env.getHome(), "training/photo/adobecs3.html");
        File srcTestFile = getInputFileRelative("multcontent/a.html");
        try {
            FileUtil.copy(srcTestFile, testFile);
        } catch (IOException e) {
            assertAlways(Fmt.S("source test file not found: %e: %s", e, e));
        }
        this.assertFileExistsWithExplanation(testFile);
        com.hitorro.base.objects.Store unmanaged = StoreUtil.getStore("unmanaged_store1");
        assertNotNull(unmanaged);
        com.hitorro.base.objects.ContentType ct = ContentTypeCache.getCache().getTypeFromFileWithDefault(testFile.getAbsolutePath());
        assertNotNull(ct);
        try {
            com.hitorro.base.objects.Content c = p.setContentLinkForUnmanagedFileStore(testFile, ct, unmanaged);
            c.addCategory("docparts", "html");
        } catch (StoreException e) {
            Log.util.error("Exception %s %e", e, e);
            assertAlways("testNonManagedFileSystem...setContentLinkForUnmanagedFileStore blew a gasket (1...store exception)");
        } catch (CategoryException e) {
            Log.util.error("Exception %s %e", e, e);
            assertAlways("testNonManagedFileSystem...setContentLinkForUnmanagedFileStore blew a gasket (2 category exception)");
        }
        sess.persist(p);
        sess.commit();
        // now make sure we can retrieve it and have the file right!
        BaseSession sess2 = DMSSessionFactory.getFactory().getSession();
        com.hitorro.base.objects.Post p1 = (com.hitorro.base.objects.Post) sess2.getObjectFromGuid(p.getGuid());
        assertNotNull(p1);
        com.hitorro.base.objects.Content c1 = p1.getContentByConstraint(new TagConstraint("docparts", "html"), true);
        assertNotNull(c1);

        // Ensure that the back pointer works.
        Set<com.hitorro.base.objects.VersionableObject> sos = c1.getVersionableObjects();
        assertFalse(sos.isEmpty());
        Iterator<com.hitorro.base.objects.VersionableObject> iter = sos.iterator();
        if (iter.hasNext()) {
            com.hitorro.base.objects.VersionableObject so = iter.next();
            String soGuid = so.getGuid();
            String postGuid = p1.getGuid();
            assertEquals(soGuid, postGuid);
        }
        BaseFile compareMe = c1.getContentFile();
        assertFileExistsWithExplanation(compareMe);
        String externalUrl = c1.getExternalURL();
        assertNotNull(externalUrl);
        // cleanup
        DMSSessionFactory.getFactory().rollbackClose(sess);
        DMSSessionFactory.getFactory().rollbackClose(sess2);
    }


    public void xtestMultiPartsMultiSessions() {
        String HText = "Permalink Body text";
        BaseSession sess = DMSSessionFactory.getFactory().getSession();
        com.hitorro.base.objects.Post p = new com.hitorro.base.objects.Post();
        p.setTitle("testMultiPartsMultiSessions Title");
        p.setBody("BodyTest");
        sess.persist(p);
        sess.commit();
        String guid = p.getGuid();
        BaseSession sess2 = DMSSessionFactory.getFactory().getSession();
        com.hitorro.base.objects.Post p2 = (com.hitorro.base.objects.Post) sess2.getHTSerializableFromGUID(guid);
        p2.setBodyFromPermaLink(HText);
        sess2.commit();
        BaseSession sess3 = DMSSessionFactory.getFactory().getSession();
        com.hitorro.base.objects.Post p3 = (com.hitorro.base.objects.Post) sess3.getHTSerializableFromGUID(guid);
        String bodyText = p3.getBodyText();
        assertEquals(HText, bodyText);

    }


    public void testURLLink() {
        String AribaUrl = "http://www.ariba.com";
        BaseSession session = DMSSessionFactory.getFactory().getSession();
        com.hitorro.base.objects.Document doc = new com.hitorro.base.objects.Document();
        doc.setTitle("link test");
        ContentTypeCache cache = ContentTypeCache.getCache();
        com.hitorro.base.objects.ContentType ct = cache.getContentTypeByMimeType("text/html");

        com.hitorro.base.objects.Content c = doc.setContentLink(AribaUrl, ct);
        String url = c.getExternalURL();
        assertEquals(url, AribaUrl);

    }


    public void testURLLinkInDocument() {
        String url = "http://www.HiTorro.net";
        BaseSessionFactory sessionFactory = DMSSessionFactory.getFactory();
        BaseSession saveSession = sessionFactory.getCachedDMSSession();
        com.hitorro.base.objects.Document doc = new com.hitorro.base.objects.Document();
        doc.setTitle("test external link");
        ContentTypeCache cache = ContentTypeCache.getCache();
        com.hitorro.base.objects.ContentType ct = cache.getContentTypeByMimeType("text/html");
        com.hitorro.base.objects.Content cnt = doc.setContentLink(url, ct);
        try {
            cnt.addCategory("docparts", "thumbnail");
        } catch (CategoryException exc) {
            assertTrue(false);
        }
        saveSession.persist(doc);
        saveSession.commit();
        long docid = doc.getId();
        sessionFactory.commitAndClose(saveSession);

        BaseSession loadSession = sessionFactory.getCachedDMSSession();
        com.hitorro.base.objects.Document doc2 = (com.hitorro.base.objects.Document) loadSession.getObject(com.hitorro.base.objects.Document.class, "where id= :a", docid);
        com.hitorro.base.objects.Content contRetrieved = doc2.getContentByConstraint(new TagConstraint("docparts", "thumbnail"), true);
        String savedURL = null;
        if (contRetrieved != null) {
            savedURL = contRetrieved.getExternalURL();
        }

        loadSession.delete(doc2);

        assertEquals(url, savedURL);
        sessionFactory.commitAndClose(loadSession);
    }


    public void testDefaultStoreContent() {
        contentStoreRetrieve(null, "tarabwdefault.jpg");
    }


    public void testBlobStoreContent() {
        contentStoreRetrieve(StoreUtil.getBlobStore(), "tarabwblob.jpg");
    }


    public void testStoreContentLink() {
        contentStoreOpenLink(null, "tarabwblob.jpg");
    }


    public void contentStoreRetrieve(com.hitorro.base.objects.Store st, String testFile) {
        BaseFile inputFile = getInputFileRelativeBaseFile("tarabw.jpg");
        this.assertFileExistsWithExplanation(inputFile);
        BaseFile outputFile = this.getOutputFileRelativeBaseFile(testFile);
        BaseSession session = DMSSessionFactory.getFactory().getSession();
        long id = createContent(null, inputFile, session);
        if (!outputFile.mkParentDir()) {
            assertAlways("Unable to write to directory %s", outputFile);
        }

        try {
            com.hitorro.base.objects.Content c = ((DMSSession) session).retrieveContentById(id);
            c.getContent(outputFile);
            this.assertBinaryFilesEqual("images should be the same", inputFile, outputFile);
        } catch (FileNotFoundException e) {
            assertAlways("File not found %s", outputFile);
        } catch (IOException e) {
            assertAlways("File not found %s %s %e", outputFile, e, e);
        } catch (StoreException e) {
            assertAlways("Store exception %s %s %e", outputFile, e, e);
        } finally {
            DMSSessionFactory.getFactory().commitAndCloseSession(session);
        }
    }


    /**
     * Ensure we can link to the "OpenResource" directory where we can allow apache to peer into our disk.
     *
     * @param st
     * @param testFile
     */
    public void contentStoreOpenLink(com.hitorro.base.objects.Store st, String testFile) {
        BaseFile inputFile = getInputFileRelativeBaseFile("tarabw.jpg");
        this.assertFileExistsWithExplanation(inputFile);
        BaseFile outputFile = this.getOutputFileRelativeBaseFile(testFile);
        BaseSession session = DMSSessionFactory.getFactory().getSession();
        long id = createContent(null, inputFile, session);
        if (!outputFile.mkParentDir()) {
            assertAlways("Unable to write to directory %s", outputFile);
        }

        try {
            com.hitorro.base.objects.Content c = ((DMSSession) session).retrieveContentById(id);
            // must be linkable
            assertTrue(c.canBeLinked());
            File l = c.getLinkFile();
            BaseFile lbf = FileFileSystem.Root.getFile(l.getAbsolutePath());
            this.assertFileDoesNotExistWithExplanation(l);
            assertTrue(c.createLink());
            this.assertFileExistsWithExplanation(l);

            c.getContent(outputFile);
            this.assertBinaryFilesEqual("images should be the same", inputFile, lbf);
            assertTrue(c.removeLink());
        } catch (FileNotFoundException e) {
            assertAlways("File not found %s", outputFile);
        } catch (IOException e) {
            assertAlways("File not found %s %s %e", outputFile, e, e);
        } catch (StoreException e) {
            assertAlways("Store exception %s %s %e", outputFile, e, e);
        } finally {
            DMSSessionFactory.getFactory().commitAndCloseSession(session);
        }
    }


    private long createContent(com.hitorro.base.objects.Store st, BaseFile inputFile, BaseSession session) {
        try {
            ContentTypeCache cache = ContentTypeCache.getCache();
            com.hitorro.base.objects.ContentType ct = cache.getContentTypeByMimeType("application/msword");

            com.hitorro.base.objects.Content c = new com.hitorro.base.objects.Content();
            if (st != null) {
                c.setStore(st);
            }
            c.setContent("MyOriginalFileName.foo", inputFile, ct);

            session.saveOrUpdate(c);
            session.commit();
            return c.getId();
        } catch (Exception e) {
            session.rollback();
            this.assertAlways("Failed to create default store content %s", e.getMessage());
        }
        return 0;
    }


    public void testMultiRenditionDocument() {
        BaseSession sess = DMSSessionFactory.getFactory().getSession();
        try {
            BaseFile a = getInputFileRelativeBaseFile("multcontent/a.html");
            BaseFile b = getInputFileRelativeBaseFile("multcontent/b.html");
            BaseFile c = getInputFileRelativeBaseFile("multcontent/c.html");
            BaseFile d = getInputFileRelativeBaseFile("multcontent/d.html");

            BaseFile aOut = this.getOutputFileRelativeBaseFile("mulcontent/a.html");
            BaseFile bOut = this.getOutputFileRelativeBaseFile("mulcontent/b.html");
            BaseFile cOut = this.getOutputFileRelativeBaseFile("mulcontent/c.html");
            BaseFile dOut = this.getOutputFileRelativeBaseFile("mulcontent/d.html");

            aOut.mkParentDir();
            com.hitorro.base.objects.Container container = new com.hitorro.base.objects.Container(com.hitorro.base.objects.Post.class, "id");
            container.setDescription("Description");
            sess.persist(container);
            com.hitorro.base.objects.Post doc = new com.hitorro.base.objects.Post();
            doc.getContainers().add(container);
            doc.setStringContent("docparts", "elink", "fileLink", "This is a test string");
            doc.setContent("ContentString");
            doc.setNote("Document Note");
            doc.setTitle("This is my document title");
            com.hitorro.base.objects.ContentType ct = ContentTypeCache.getCache().getContentTypeByMimeType("application/msword");
            doc.setContent("a.html", ct, a);
            com.hitorro.base.objects.Content cont = doc.setContent("b.html", ct, b);
            cont.addCategory("testcat", "HTHello");
            doc.setContent("c.html", ct, c).setContentRendition(sess, ct, d, "320x240");
            doc.setContent("d.html", ct, d);
            sess.persist(doc);
            DMSSessionFactory.getFactory().commitAndCloseSession(sess);

            Timer t = new Timer();
            t.reset();
            for (int i = 0; i < 10000; i++) {
                BaseSession sess2 = DMSSessionFactory.getFactory().getSession();

                String guid = doc.getGuid();
                com.hitorro.base.objects.Document doc2 = (com.hitorro.base.objects.Document) sess2.getObjectFromGuid(guid);
                DMSSessionFactory.getFactory().rollbackCloseSession(sess2);
            }
            double time = t.stop();
            Log.util.error("Took %s %s", time, time / (double) 10000);
            BaseSession sess2 = DMSSessionFactory.getFactory().getSession();

            String guid = doc.getGuid();
            com.hitorro.base.objects.Document doc2 = (com.hitorro.base.objects.Document) sess2.getObjectFromGuid(guid);

            assertNotNull(doc2);

            // check for usage of tag
            com.hitorro.base.objects.Content contRetrieved = doc2.getContentByConstraint(new TagConstraint("testcat", "HTHello"), true);
            assertNotNull(contRetrieved);

            // ensure we can retrieve a rendition
            HTPredicate<com.hitorro.base.objects.Content> c1 = new ResolutionConstraint("320x240");
            HTPredicate<com.hitorro.base.objects.Content> c2 = new FileNameMatchContentConstraint("c.html", true);
            HTPredicate<com.hitorro.base.objects.Content> cons =
                    new LogicalAndOperator(c1, c2);
            com.hitorro.base.objects.Content rendition = doc2.getContentByConstraint(cons, true);
            assertNotNull(cons);

            com.hitorro.base.objects.Content ca = doc2.getContentByFileName("a.html", true);
            assertNotNull(ca);
            com.hitorro.base.objects.Store s = StoreUtil.getStore(ca.getStore().getName());
            com.hitorro.base.objects.StoreType type = s.getStoreTypeType();
            ca.getContent(aOut);
            this.assertBinaryFilesEqual("", a, aOut);

            ca = doc.getContentByFileName("b.html", true);
            assertNotNull(ca);
            ca.getContent(bOut);
            this.assertBinaryFilesEqual("", b, bOut);

            ca = doc.getContentByFileName("d.html", true);
            assertNotNull(ca);
            ca.getContent(dOut);
            this.assertBinaryFilesEqual("", d, dOut);

            ca = doc.getContentByFileName("c.html", true);
            assertNotNull(ca);
            ca.getContent(cOut);
            this.assertBinaryFilesEqual("", c, cOut);

            Set<com.hitorro.base.objects.Content> renditions = ca.getRenditions();
            Object content[] = renditions.toArray();
            assertNotNull(content);
            assertNotNull(content[0]);
            ca = (com.hitorro.base.objects.Content) content[0];

            ca.getContent(dOut);
            assertBinaryFilesEqual("", d, dOut);
            DMSSessionFactory.getFactory().rollbackCloseSession(sess2);


        } catch (IOException e) {
            assertAlways("Got IOException");
        } catch (StoreException e) {
            assertAlways("Got StoreException");
        } catch (Exception e) {
            assertAlways("Exception");
        }
    }

}
