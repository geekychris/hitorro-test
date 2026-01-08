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

import com.hitorro.base.objects.Container;
import com.hitorro.base.objects.Document;
import com.hitorro.base.objects.VersionableObject;
import com.hitorro.basedms.session.DMSSessionFactory;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;
import com.hitorro.util.typesystem.BaseSession;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * to Containers of objects.
 */
@HTTest(runlevel = RunLevel.Smoke,
        email = "chris@hitorro.com",
        description = "Virtual container tests.")
public class ContainerTest extends EnhancedTestCase {
    /**
     * Basic container test, ensure that a sysobject can belong to more than one container and that we get all the right
     * objects back for a given container.
     */
    public void testBasicContainer() {
        BaseSession sess = DMSSessionFactory.getFactory().getCachedDMSSession();
        Container containerA = new Container(Document.class, "id");
        Container containerB = new Container(Document.class, "id");
        containerA.setDescription("Test Container A");
        sess.persist(containerA);
        containerB.setDescription("Test Container B");
        sess.persist(containerB);
        HashMap<String, String> mapA = new HashMap<String, String>();
        HashMap<String, String> mapB = new HashMap<String, String>();
        createDoc(sess, mapA, containerA, null);
        createDoc(sess, mapB, containerB, null);
        createDoc(sess, mapB, containerB, null);
        createDoc(sess, mapA, containerA, null);
        createDoc(sess, mapB, containerB, null);
        createDoc(sess, mapA, containerA, null);
        // not sure whats going on here.  hql shows the correct amount of results
        // dms.hql from ht.base.objects.Document s join s.containers c where c.id = 431
        Document both = createDoc(sess, mapA, containerA, containerB);
        mapB.put(both.getGuid(), both.getGuid());
        DMSSessionFactory.getFactory().commitAndCloseSession(sess);
        // ensure that the container has both docs
        assertContainment(mapA, containerA);
        assertContainment(mapB, containerB);
    }

    private void assertContainment(HashMap<String, String> map, Container container) {
        BaseSession sessContainer = DMSSessionFactory.getFactory().getSession();
        container.setSession(sessContainer);
        Iterator<Object[]> soIter = container.getIterator();
        int i = 0;
		HashSet<String> found = new HashSet<String>();
        while (soIter.hasNext()) {
            Object a = soIter.next();
            VersionableObject so = (VersionableObject) a;
			found.add(so.getGuid());
            assertNotNull(map.get(so.getGuid()));
            i++;
        }
        assertEquals(map.size(), i);
        //DMSSessionFactory.getFactory().rollbackCloseSession(sessContainer);
    }

    private Document createDoc(BaseSession sess, HashMap<String, String> map, Container container, Container secondContainer) {
        Document d = new Document();
        d.setContent("ContentString");
        d.setNote("Document Note");
        d.setTitle("This is my document title");
        d.getContainers().add(container);
        if (secondContainer != null) {
            d.getContainers().add(secondContainer);
        }
        sess.persist(d);
        map.put(d.getGuid(), d.getGuid());
        return d;
    }
}
