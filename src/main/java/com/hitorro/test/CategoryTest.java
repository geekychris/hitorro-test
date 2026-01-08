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

import com.hitorro.base.objects.Category;
import com.hitorro.base.objects.Document;
import com.hitorro.basedms.SystemVersionException;
import com.hitorro.basedms.cache.DomainValueCache;
import com.hitorro.basedms.exceptions.CategoryException;
import com.hitorro.basedms.session.DMSSession;
import com.hitorro.basedms.session.DMSSessionFactory;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.valuemap.ValueMap;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;
import com.hitorro.util.typesystem.BaseSession;
import org.hibernate.query.Query;

import java.util.List;

/**
 * User: chris Date: Nov 3, 2006 Time: 6:36:12 PM
 * <p/>
 * test categories, caches, and lookup.
 * <p/>
 * todo: label tests with multiple versions of versionedobject - setting label already exists. hierarchical lookup tests.
 * simple lookup tests. multi dv tests lookup against wildcard tests? attempting to put a domain that does not exist.
 * <p/>
 * Domain lookup defaulting.
 */
@HTTest(runlevel = RunLevel.Full,
        email = "chris@hitorro.com",
        description = "Test the category lookup cache mechanisms.")
public class CategoryTest extends EnhancedTestCase {
    public String m_guid;
    public String m_guidM3;

    public void testDomainValueCache() {
        ValueMap<Category> cats = DomainValueCache.getCache().get("testcat");
        ValueMap<Category> cats2 = DomainValueCache.getCache().get("testcat");
        // should be same instance
        assertTrue(cats == cats2);
    }

    private Document createFourPartDoc(BaseSession sess) {
        Document doc = new Document();
        doc.setContent("category versioning test content");
        doc.setNote("category versioning test");
        doc.setTitle("This is my document title for category versioning test");
        sess.persist(doc);
        sess.commit();
        return doc;
    }

    public void testVersioning() {
        BaseSession session = DMSSessionFactory.getFactory().getSession();
        Document doc = createFourPartDoc(session);

        try {
            doc.addCategory("label", "release");
        } catch (CategoryException e) {
            assertAlways("Unable to setPhrases label for doc");
        }
        assertNotNull(doc);
        try {
            Document major = (Document) doc.createMajorVersion();
            try {
                major.addCategory("label", "release");
            } catch (CategoryException e) {
                assertAlways("Unable to setPhrases label for major");
            }
            session.persist(major);

            Document m2 = (Document) major.createMajorVersion();
            // break a's file at this point
            try {
                m2.addCategory("testcat", "release");
                m2.addCategory("label", "release");
                m2.addCategory("label", "foo");
            } catch (CategoryException e) {
                assertAlways("Unable to setPhrases label for m2");
            }
            session.persist(m2);

            Document m3 = (Document) m2.createMinorVersion();
            try {
                m3.addCategory("label", "release");
                m3.addCategory("testcat", "release");
                m_guidM3 = m3.getGuid();

            } catch (CategoryException e) {
                assertAlways("Unable to setPhrases label for m3");
            }

            session.persist(m3);

            Document m4 = (Document) m3.createMinorVersion();
            session.persist(m4);

            session.commit();
            m_guid = doc.getGuid();
            assertEquals(m3.getCategoryValueExists("label", "release"), true);
            assertEquals(doc.getCategoryValueExists("label", "release"), false);
            assertEquals(m2.getCategoryValueExists("label", "release"), false);
            assertEquals(m4.getCategoryValueExists("label", "release"), false);

            assertNotNull(m_guid);
            // ensure 1 label,release
            DMSSessionFactory.getFactory().rollbackCloseSession(session);
            BaseSession s2 = DMSSessionFactory.getFactory().getSession();
            //
            String query = Fmt.S("from %s a join a.categories b where canonicalGuid='%s' and b.domain='label' and b.value='release'", Document.class.getCanonicalName(), m_guid);
            Query q = ((DMSSession) s2).createQuery(query);
            List<Document> list = q.list();
            assertEquals(list.size(), 1);
            Document taggedDoc = list.get(0);
            assertEquals(m_guidM3, taggedDoc.getGuid());
            //ensure testcat, release has 2 docs as thats how many we tagged
            DMSSessionFactory.getFactory().rollbackCloseSession(s2);
            BaseSession s3 = DMSSessionFactory.getFactory().getSession();
            String query1 = Fmt.S("from %s a join a.categories b where canonicalGuid='%s' and b.domain='testcat' and b.value='release'", Document.class.getCanonicalName(), m_guid);
            Query q1 = ((DMSSession) s3).createQuery(query1);

            List<Document> newList = q1.list();
            assertEquals(newList.size(), 2);
            DMSSessionFactory.getFactory().rollbackCloseSession(s3);

        } catch (SystemVersionException e) {
            assertAlways("Unable to create major version");
        }
    }
}
