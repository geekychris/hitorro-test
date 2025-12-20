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

import com.hitorro.basedms.cache.ContentTypeCache;
import com.hitorro.basedms.session.DMSRealmSecurityModel;
import com.hitorro.basedms.session.DMSSession;
import com.hitorro.basedms.session.DMSSessionFactory;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;
import com.hitorro.util.typesystem.BaseSession;
import com.hitorro.util.typesystem.BaseSessionFactory;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;

@HTTest(runlevel = RunLevel.Full,
        email = "chris@hitorro.com",
        description = "Persistence tests",
        dependentServices = {com.hitorro.base.objects.BaseDMSService.class})
public class PersistenceTest extends EnhancedTestCase {
    public void testRealm() {
        BaseSession sess = DMSSessionFactory.getFactory().getSession();
        ((DMSSession) sess).setUser("chris", "chris", "c1", DMSRealmSecurityModel.me);
        com.hitorro.base.objects.Post item = new com.hitorro.base.objects.Post();
        StringBuilder b = new StringBuilder();
        b.append("Chris was ");
        item.setTitle(b.toString());
        item.setBody("Body Text");
        String s = b.toString();
        sess.persist(item);
        String guid = item.getGuid();
        DMSSessionFactory.getFactory().commitAndCloseSession(sess);

        sess = DMSSessionFactory.getFactory().getSession();
        ((DMSSession) sess).setUser("chris", "chris", null, DMSRealmSecurityModel.me);
        item = new com.hitorro.base.objects.Post();
        b.append("Chris was there");
        item.setTitle(b.toString());
        item.setBody("Body Text");
        s = b.toString();
        sess.persist(item);
        String guidWithNull = item.getGuid();

        DMSSessionFactory.getFactory().commitAndCloseSession(sess);

        BaseSession sess2 = DMSSessionFactory.getFactory().getSession();
        ((DMSSession) sess2).setUser("chris", "chris", "c1", DMSRealmSecurityModel.me);
        com.hitorro.base.objects.Post rp = (com.hitorro.base.objects.Post) sess2.getHTSerializableFromGUID(guid);

        com.hitorro.base.objects.Post rpWithNullRealm = (com.hitorro.base.objects.Post) sess2.getHTSerializableFromGUID(guidWithNull);

        // we should of retrieved the document with realm of NULL and the realm that is the current user realm.
        assertNotNull(rp);
        assertNotNull(rpWithNullRealm);

        DMSSessionFactory.getFactory().commitAndCloseSession(sess);

        sess2 = DMSSessionFactory.getFactory().getSession();
        ((DMSSession) sess2).setUser("ryan", "ryan", "r1", DMSRealmSecurityModel.me);
        rp = (com.hitorro.base.objects.Post) sess2.getHTSerializableFromGUID(guid);
        assertNull(rp);
    }

    public void testDetachWithContent() {
        //    Logger.setLogLevels("org.hibernate:debug");
        BaseSession sess = DMSSessionFactory.getFactory().getSession();
        BaseSession sess2 = DMSSessionFactory.getFactory().getSession();
        BaseSession sess3 = DMSSessionFactory.getFactory().getSession();

        try {
            com.hitorro.base.objects.Post item = new com.hitorro.base.objects.Post();
            StringBuilder b = new StringBuilder();
            b.append("Chris was ");
            item.setTitle(b.toString());
            item.setBody("Body Text");
            String s = b.toString();
            sess.persist(item);
            String guid = item.getGuid();

            DMSSessionFactory.getFactory().commitAndCloseSession(sess);

            com.hitorro.base.objects.Post item2 = (com.hitorro.base.objects.Post) sess2.getHTSerializableFromGUID(guid);
            com.hitorro.base.objects.Content c = item2.setBody("SomeBody");
            DMSSessionFactory.getFactory().rollbackCloseSession(sess2);
            sess3.saveOrUpdate(item2);
            com.hitorro.base.objects.User chris = com.hitorro.base.objects.User.getUserForName(sess3, "chris");
        } catch (Exception e) {
            Console.println("Exception %s %e", e, e);
        } finally {
            DMSSessionFactory.getFactory().rollbackCloseSession(sess);
            DMSSessionFactory.getFactory().rollbackCloseSession(sess3);
        }

    }

    public void testDetachWithContentCachedSession() {
        //    Logger.setLogLevels("org.hibernate:debug");
        BaseSession sess = DMSSessionFactory.getFactory().getCachedDMSSession();
        try {
            com.hitorro.base.objects.Post item = new com.hitorro.base.objects.Post();
            StringBuilder b = new StringBuilder();
            b.append("Chris was ");
            item.setTitle(b.toString());
            item.setBody("Body Text");
            String s = b.toString();
            sess.persist(item);
            String guid = item.getGuid();
            sess.commit();
            DMSSessionFactory.getFactory().disconnectSession(sess);

            com.hitorro.base.objects.Post item2 = (com.hitorro.base.objects.Post) sess.getObjectFromGuid(guid);
            com.hitorro.base.objects.Content c = item2.setBody("SomeBody");
            item2.setTitle("new title 1");
            DMSSessionFactory.getFactory().disconnectSession(sess);
            item2 = (com.hitorro.base.objects.Post) sess.getObjectFromGuid(guid);
            item2.setTitle("new title 3");
            c = item2.setBody("SomeBody");
            sess.commit();
            com.hitorro.base.objects.User chris = com.hitorro.base.objects.User.getUserForName(sess, "chris");
        } catch (Exception e) {
            Console.println("Exception %s %e", e, e);
        } finally {
            DMSSessionFactory.getFactory().rollbackCloseSession(sess);
        }

    }

    public void testPersistI18NChar() {
        //    Logger.setLogLevels("org.hibernate:debug");
        BaseSession sess = DMSSessionFactory.getFactory().getSession();
        BaseSession sess2 = DMSSessionFactory.getFactory().getSession();

        try {
            com.hitorro.base.objects.Post item = new com.hitorro.base.objects.Post();
            StringBuilder b = new StringBuilder();
            b.append("Chris was ");
            //b.append((char)65533);
            b.append((char) 41120);

            b.append(" with a funny character");
            item.setTitle(b.toString());
            item.setBody("Body Text");
            String s = b.toString();
            sess.persist(item);
            String guid = item.getGuid();

            DMSSessionFactory.getFactory().commitAndCloseSession(sess);
            com.hitorro.base.objects.Post item2 = (com.hitorro.base.objects.Post) sess2.getHTSerializableFromGUID(guid);
            String title = item2.getTitle();
            assertEquals(title, b.toString());
        } finally {
            DMSSessionFactory.getFactory().rollbackCloseSession(sess);
            DMSSessionFactory.getFactory().rollbackCloseSession(sess2);
        }
    }

    public void testConstruct() {
        try {
            ContentTypeCache cache = ContentTypeCache.getCache();
            com.hitorro.base.objects.ContentType ct = cache.getContentTypeByMimeType("application/msword");
            Set<com.hitorro.base.objects.Extension> exts = ct.getExtensions();
            Iterator<com.hitorro.base.objects.Extension> iter = exts.iterator();

            BaseSession session = DMSSessionFactory.getFactory().getSession();
            com.hitorro.base.objects.Content content = new com.hitorro.base.objects.Content();
            content.setOriginalFileName("c:/foo.bla");
            content.setFileName("10/11/12");
            content.setContentType(ct);
            session.persist(content);
            session.commit();

            //createBlog(session);

            DMSSessionFactory.closeSession(session);
        } catch (Exception e) {
            System.out.println(Fmt.S("Exception : %s %e", e, e));
        }
    }

    /**
     * Create an object in one session Modify it in session 2 modify something else in session 3 open in session 4 and
     * verify that we have both changes.
     * <p/>
     * Ensure that transactions work
     */
    public void testTransactionBoundary() {
        try {
            // create forum & post
            BaseSession session = DMSSessionFactory.getFactory().getSession();
            com.hitorro.base.objects.Forum blog = createForum(session, "blog name");
            com.hitorro.base.objects.Post be = createPost(session, blog, "contenta", "titlea");
            DMSSessionFactory.getFactory().commitAndCloseSession(session);

            // refetch the post, saveOrUpdate, and change it
            BaseSession session1 = DMSSessionFactory.getFactory().getSession();
            com.hitorro.base.objects.Document be1 = (com.hitorro.base.objects.Document) session1.getObjectFromGuid(be.getGuid());
            session1.saveOrUpdate(be1);
            be1.setStringContent("docparts", "elink", "file1", "test");
            be1.setContent("contentb");
            DMSSessionFactory.getFactory().commitAndCloseSession(session1);

            // change the post, then saveOrUpdate
            BaseSession sessionX = DMSSessionFactory.getFactory().getSession();
            be1.setStringContent("docparts", "elink", "file2", "test");
            be1.setContent("contentb");
            sessionX.saveOrUpdate(be1);
            Env.sleepNSeconds(1);
            DMSSessionFactory.getFactory().commitAndCloseSession(sessionX);
            // fetch & change it again
            BaseSession session2 = DMSSessionFactory.getFactory().getSession();
            com.hitorro.base.objects.Document be2 = ((DMSSession) session2).retrieveDocumentById(be.getId());
            be2.setTitle("titleb");
            Env.sleepNSeconds(1);
            session2.saveOrUpdate(be2);

            DMSSessionFactory.getFactory().commitAndCloseSession(session2);

            BaseSession sessionAlpha = DMSSessionFactory.getFactory().getSession();
            com.hitorro.base.objects.Document o = (com.hitorro.base.objects.Document) sessionAlpha.getHTSerializableFromGUID(be2.getGuid());
            if (o == null) {
                assertAlways("Expected session.getHTSerializableFromGUID to return document");
            }
            Date d1 = o.getCreationDate();
            Date d2 = o.getModifiedDate();
            String d1S = d1.toString();
            String d2S = d2.toString();
            o.setNote("note");
            sessionAlpha.commit();
            Env.sleepNSeconds(5);
            assertEquals(o.getId(), be2.getId());

            BaseSession session3 = DMSSessionFactory.getFactory().getSession();
            com.hitorro.base.objects.Document be3 = ((DMSSession) session3).retrieveDocumentById(be.getId());
            Date d1t = be3.getCreationDate();
            Date d2t = be3.getModifiedDate();
            String d1St = d1t.toString();
            String d2St = d2t.toString();
            String content = be3.getContent();
            String title = be3.getTitle();
            DMSSessionFactory.getFactory().rollbackCloseSession(session3);
            assertEquals(content, "contentb");
            assertEquals(title, "titleb");
        } catch (Exception e) {
            this.assertAlways("exception occured %s %e;", e, e);
        }
    }

    /**
     * Ensure we can have a long conversation of one session with multiple transactions.
     */
    public void testMultipleTransactionsInOneSession() {
        try {
            BaseSession session = DMSSessionFactory.getFactory().getSession();
            com.hitorro.base.objects.Forum blog = createForum(session, "multi transaction one session test");
            com.hitorro.base.objects.Post be = createPost(session, blog, "content1", "titlea");
            session.commit();

            com.hitorro.base.objects.Document be1 = ((DMSSession) session).retrieveDocumentById(be.getId());
            be1.setContent("contentb");
            session.saveOrUpdate(be1);
            DMSSessionFactory.getFactory().commitAndCloseSession(session);

            String content = be1.getContent();
            String title = be1.getTitle();
            assertEquals(content, "contentb");
            assertEquals(title, "titlea");
        } catch (Exception e) {
            System.out.println(Fmt.S("Exception : %s %e", e, e));
            this.assertAlways("exception occured %s %e;", e, e);
        }
    }

    private com.hitorro.base.objects.VersionableObject createVersionableObject(BaseSession sess, String name) {
        com.hitorro.base.objects.VersionableObject blog = new com.hitorro.base.objects.VersionableObject();
        blog.setNote(name);
        sess.persist(blog);
        return blog;
    }


    public void testCacheSession() {
        String blogName = "testMultipleSessions:" + System.currentTimeMillis();
        String originalContent = "contentA(cache)";
        String changedContent = "zzContentB(cache)";
        String changedContent2 = "at last we are done (cache)";

        BaseSessionFactory sessionFactory = DMSSessionFactory.getFactory();
        BaseSession sCreate = sessionFactory.getCachedDMSSession();
        com.hitorro.base.objects.VersionableObject so = createVersionableObject(sCreate, blogName);
        so.setNote(originalContent);
        sCreate.commit();
        sessionFactory.rollbackClose(sCreate);
        com.hitorro.base.objects.VersionableObject soFromCache = (com.hitorro.base.objects.VersionableObject) sCreate.getObjectFromGuid(so.getGuid());
        assertNotNull(so);
        assertEquals(so, soFromCache);
        assertFalse(so == soFromCache);
        soFromCache.setNote(changedContent2);
        sCreate.commit();

        BaseSession sCreate2 = sessionFactory.getCachedDMSSession();
        com.hitorro.base.objects.VersionableObject soFreshFromDB = (com.hitorro.base.objects.VersionableObject) sCreate2.getObjectFromGuid(so.getGuid());
        assertNotNull(soFreshFromDB);
        assertEquals(soFreshFromDB.getNote(), soFromCache.getNote());
        DMSSessionFactory.closeSession(sCreate2);
        DMSSessionFactory.closeSession(sCreate);
    }

    public void testMultipleSessionsSysobject() {
        String blogName = "testMultipleSessions:" + System.currentTimeMillis();
        String originalContent = "contentA";
        String changedContent = "zzContentB";
        String changedContent2 = "at last we are done";

        try {
            // create a blog entry and save to the database
            BaseSession sCreate = DMSSessionFactory.getFactory().getSession();
            com.hitorro.base.objects.VersionableObject so = createVersionableObject(sCreate, blogName);
            sCreate.persist(so);
            so.setNote(originalContent);
            DMSSessionFactory.getFactory().commitAndCloseSession(sCreate);

            // fetch that entry back from the database and modify it in one session
            BaseSession sQuery = DMSSessionFactory.getFactory().getSession();
            com.hitorro.base.objects.VersionableObject so2 = (com.hitorro.base.objects.VersionableObject) ((DMSSession) sQuery).getSingleObjectById(com.hitorro.base.objects.VersionableObject.class, so.getId());
            assertNotNull(so2);

            so2.setNote(changedContent);
            // no session commit!  We want the change committed in another session
            DMSSessionFactory.getFactory().rollbackCloseSession(sQuery);

            // open another session and attach the blog entry to the new session
            // make another change and save it
            BaseSession sReopen = DMSSessionFactory.getFactory().getSession();

            assertEquals(changedContent, so2.getNote());
            so2.setNote(changedContent2);
            sReopen.saveOrUpdate(so2);
            //sReopen.update(so2);
            DMSSessionFactory.getFactory().commitAndCloseSession(sReopen);

            // open a last session, requery the object and make sure it has the change
            BaseSession sCheck = DMSSessionFactory.getFactory().getSession();
            com.hitorro.base.objects.VersionableObject so3 = (com.hitorro.base.objects.VersionableObject) ((DMSSession) sCheck).getSingleObjectById(com.hitorro.base.objects.VersionableObject.class, so.getId());
            String oldContent = so3.getNote();
            assertEquals(changedContent2, oldContent);

            // delete the test blog & blogentry objects
            sCheck.delete(so3);
            DMSSessionFactory.getFactory().commitAndCloseSession(sCheck);
        } catch (Exception exc) {
            System.out.println(Fmt.S("Exception in testSessionUpdate: %s %e", exc, exc));
            this.assertAlways("exception occurred %s %e", exc, exc);
        }
    }

    private com.hitorro.base.objects.Forum createForum(BaseSession session, String name) {
        com.hitorro.base.objects.Forum blog = new com.hitorro.base.objects.Forum();
        blog.setName(name);
        session.persist(blog);

        return blog;
    }

    private com.hitorro.base.objects.Post createPost(BaseSession session, com.hitorro.base.objects.Forum blog, String content, String title) {
        com.hitorro.base.objects.Post be = new com.hitorro.base.objects.Post();
        be.setEntryOrdinal(0);
        be.setBody(content);
        be.addContainer(blog);
        be.setOwningContainer(blog);

        be.setTitle(title);
        be.setMoreLink("http://www.yahoo.com");
        session.persist(be);

        return be;
    }
}
