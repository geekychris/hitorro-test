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

import com.hitorro.basedms.BaseUtil;
import com.hitorro.jsontypesystem.JVS;
import com.hitorro.util.core.ArrayUtil;
import com.hitorro.util.core.Constants;
import com.hitorro.util.core.ListUtil;
import com.hitorro.util.core.classes.ClassUtil;
import com.hitorro.util.core.diff.GenericDiffer;
import com.hitorro.util.core.diff.GenericDifferContainer;
import com.hitorro.util.core.map.MapUtil;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.io.IOUtil;
import com.hitorro.util.io.LengthConstrainedInputStream;
import com.hitorro.util.io.net.InetUtil;
import com.hitorro.util.json.keys.propaccess.PropaccessError;
import com.hitorro.util.osprocessexec.SimpleExec;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;

import java.io.*;
import java.util.*;

/**
 * Test core utility functions.
 *
 * @author chris
 */
@HTTest(runlevel = RunLevel.Smoke,
        email = "chris@hitorro.com",
        description = "Tests of core utility functions",
        module = "util")
public class CoreUtilTest extends EnhancedTestCase {
    public static final String A = "The quick [tag]brown[/tag]fox jumped over the lazy [tag]dogs";
    public static final String AP = "The quick brownfox jumped over the lazy dogs";
    public static final String B = "The quick [tag]brown[/tag]fox jumped over the lazy [tag]dogs[/tag]";
    public static final String BP = "The quick brownfox jumped over the lazy dogs";
    public static final String tok1[] = {"this is", "a test", "case"};
    public static final String tok2[] = {"case"};
    private static final String Redirect = "<META HTTP-EQUIV=\"Refresh\"\n" +
            "      CONTENT=\"5; URL=html-redirect.html\">";
    private static String[] guids = {"3c:99:655123434", "a:120:123", "1FF:1:124353534578"};

    private void p(String p[], String expects[]) {
        if (ArrayUtil.nullOrEmpty(p)) {
            this.assertAlways("expected non empty array");
        }
        assertEquals(p.length, expects.length);
        for (int i = 0; i < p.length; i++) {
            assertEquals(p[i], expects[i]);
        }
    }

    public void testMultiCharTokenizer() {
        p(StringUtil.tokenizeFromMultiChar("this is @@ a test@@case", "@@", true), tok1);
        p(StringUtil.tokenizeFromMultiChar("@@this is @@ a test@@case", "@@", true), tok1);
        p(StringUtil.tokenizeFromMultiChar("@@this is @@ a test@@case@@", "@@", true), tok1);
        p(StringUtil.tokenizeFromMultiChar("case@@", "@@", true), tok2);
    }

    public void testDiffer() {
        GenericDifferContainer<String> cont = new GenericDifferContainer<String>();
        List<String> a = new ArrayList<String>();
        List<String> b = new ArrayList<String>();
        a.add("a");
        a.add("b");
        a.add("c");
        b.add("c");
        b.add("d");
        GenericDiffer<String> differ = new GenericDiffer<String>();
        differ.diff(a.iterator(), b.iterator(), new StringComparitor(), cont);
        assertEquals(cont.getAdded().size(), 1);
        assertEquals(cont.getDeleted().size(), 2);
        assertEquals(cont.getModified().size(), 1);
    }

    public void testTagSubstitute() {
        List<String> extractedTags = new ArrayList<String>();
        String r = StringUtil.removeSpecialTags(A, "[tag]", "[/tag]", extractedTags);
        assertEquals(r, AP);

        List<String> et = new ArrayList<String>();
        String r2 = StringUtil.removeSpecialTags(B, "[tag]", "[/tag]", et);
        assertEquals(r2, BP);
    }

    public void testGuidIDTranslate() {
        for (String guid : guids) {
            long bid = BaseUtil.getLongFromGuid(guid);
            if (bid == -1) {
                this.assertAlways("did not expect -1");
            }
            String newGuid = BaseUtil.getGuidFromLong(bid);
            assertEqualsIgnoreCase(guid, newGuid);
        }

    }


    // -------------------------------------------------------------------------------
    // ClassUtil tests
    // -------------------------------------------------------------------------------

    public void testDeDupeList() {
        List<String> l = new ArrayList<String>();
        l.add("cat");
        l.add("aardvark");
        l.add("cat");
        l.add("aardvark");
        l.add("pussy");
        l.add("dog");
        l.add("cat");
        l.add("cat");
        Comparator c = new TestComparator();
        ListUtil.removeDuplicates(l, c);
        assertEquals(4, l.size());
    }

    public void testClassForName() {
        String badclassName = "x.y.z";
        String vectorclassName = Vector.class.getName();
        Class vectorClass = Vector.class;
        String treemapclassName = TreeMap.class.getName();
        Class treemapClass = TreeMap.class;
        Class mapclass = Map.class;

        // if we ask for a bad class we should get nothing
        Class test = ClassUtil.getClassForName(badclassName, null, false);
        assertNull(test);

        // if we ask for a good class with no constraint, we should get the right one
        test = ClassUtil.getClassForName(vectorclassName, null, false);
        assertEquals(test, vectorClass);

        // if we ask for a good class with a constraint that it doesn't meet, it should fail
        test = ClassUtil.getClassForName(vectorclassName, mapclass, false);
        assertNull(test);

        // if we ask for a good class with a constraint that it meets, we should get it
        test = ClassUtil.getClassForName(treemapclassName, mapclass, false);
        assertEquals(test, treemapClass);
    }

    public void testBareClassName() {
        Class c1 = ClassUtil.getClassForName("java.util.TreeMap", null);
        assertEquals("TreeMap", ClassUtil.getBareName(c1));
        assertEquals("", ClassUtil.getBareName(null));
        boolean[] xx = new boolean[3];
        Class c2 = xx.getClass();
        assertEquals("[Z", ClassUtil.getBareName(c2));
    }


    // -------------------------------------------------------------------------------
    // MapUtil tests
    // -------------------------------------------------------------------------------

    public void testSubclassOf() {
        // null check
        assertFalse(ClassUtil.isSubClass(null, TreeMap.class));
        // subclass of class
        assertTrue(ClassUtil.isSubClass(TreeMap.class, AbstractMap.class));
        // interface implementation
        assertTrue(ClassUtil.isSubClass(TreeMap.class, Map.class));
        // failure
        assertFalse(ClassUtil.isSubClass(TreeMap.class, Random.class));
        assertFalse(ClassUtil.isSubClass(TreeMap.class, Observer.class));
    }

    public void testMapSubmap() {
        // construct a apply with some hierarchy
        // the value of each element is the name of the leaf node plus !
        TreeMap<String, String> testmap = new TreeMap<String, String>();
        testmap.put("a", "a!");
        testmap.put("c", "c!");
        testmap.put("b", "b!");
        testmap.put("c.dairy.cream", "cream!");
        testmap.put("c.dairy.milk", "milk!");
        testmap.put("c.foo", "foo!");
        testmap.put("d.dairy.cheese", "cheese!");
        testmap.put("c.dairy.z", "z!");

        // if we ask for a non-existent subtree, we should get back an empty apply
        TreeMap<String, String> subtree = MapUtil.getSubMap(testmap, "x");
        assertNotNull(subtree);
        assertEquals(0, subtree.size());

        // if we ask for double-dotted tree, it should work
        subtree = MapUtil.getSubMap(testmap, "c.dairy");
        checkSubmap(subtree, "z", "cream", "milk");

        // if we ask for single-dotted tree we should get back more
        subtree = MapUtil.getSubMap(testmap, "c");
        checkSubmap(subtree, "dairy.cream", "dairy.milk", "foo", "dairy.z");
        // and we should be able to subtree that subtree
        subtree = MapUtil.getSubMap(subtree, "dairy");
        checkSubmap(subtree, "cream", "z", "milk");

        // asking for a tree with a single element should work
        subtree = MapUtil.getSubMap(testmap, "d.dairy");
        checkSubmap(subtree, "cheese");

        // asking for a tree on a key with no subelements should be empty
        subtree = MapUtil.getSubMap(testmap, "b");
        assertNotNull(subtree);
        assertEquals(0, subtree.size());

        // we should be able to do similar things with a Properties object
        Properties props = MapUtil.getPropertySubProperties(testmap, "c.dairy");
        checkSubmap(props, "milk", "cream", "z");
    }

    public void testFmtP() throws PropaccessError {
        JVS top = new JVS();
        top.set("foo", "fudge");
        String a = Fmt.P("${os_bin_dir}java");
        String b = Fmt.PO("${os_bin_dir}java${foo}", top.getJsonNode());
        String ab = Fmt.S("%sfudge", a);
        assertEquals(b, ab);
    }

    public void testSubstringCaseInsensativeMatch() {
        String buffer = "";
        assertTrue(StringUtil.subStringEqualsIgnoreCase(Redirect, 1, 4, "MeTa"));
        assertTrue(StringUtil.subStringEqualsIgnoreCase(Redirect, 1, 4, "META"));
        assertFalse(StringUtil.subStringEqualsIgnoreCase(Redirect, 1, 4, "M3Ta"));
    }


    public void testSimpleExec() {
        StringBuilder result = new StringBuilder();
        try {
            String execThis = Fmt.P("${os_bin_dir}date");
            SimpleExec.exec(execThis, result, true);
        } catch (Exception e) {
            assertAlways("Blew up testing simple exec");
        }
        assertNotNull(result);
    }


    /**
     * Helper routine for testMapSubmap. Checks that the expected key-value pairs exist, and that no others exist. We
     * check that the values of the keys are the last node of the key name plus !
     *
     * @param map    submap to be tested
     * @param checks The keys we expect
     */
    private void checkSubmap(Map map, String... checks) {
        // the apply should exist
        assertNotNull(map);

        // the apply should have the same number of keys in it as we have checks
        assertEquals(map.size(), checks.length);

        // every key should be there and have the same value
        for (String key : checks) {
            String value = (String) map.get(key);
            // figure out the expected value
            int dotIndex = key.lastIndexOf('.');
            String expectedValue;
            if (dotIndex < 0) {
                expectedValue = StringUtil.strcat(key, "!");
            } else {
                expectedValue = StringUtil.strcat(key.substring(dotIndex + 1), "!");
            }
            assertEquals(expectedValue, value);
        }
    }


    /**
     * Make sure the IOUtil.copy(is,os, length) and LengthConstrainedInputStream count properly
     *
     * @throws IOException on error
     */
    public void testConstrainedInputStream() throws IOException {
        int size = 1024 * 1024;
        int size2 = 1024 * 1000;
        int size3 = 1024 * 900;
        byte[] array = new byte[size];
        byte[] array2 = new byte[size];
        for (int i = 0; i < size; i++) {
            array[i] = (byte) i;
        }
        InputStream bis = new ByteArrayInputStream(array);
        File f = this.getOutputFileRelative("constrainedis.ser");
        OutputStream fos = FileUtil.getBufferedFileOutputStream(f);
        IOUtil.copyStream(new LengthConstrainedInputStream(bis, size2), fos);
        fos.flush();
        fos.close();
        // make sure we wrote out the amount of bytes we expect.
        assertEquals(f.length(), size2);
        InputStream newIS = new LengthConstrainedInputStream(FileUtil.getBufferedFileInputStream(f), size3);

        int read = newIS.read(array2);
        assertEquals(read, size3);
        for (int i = 0; i < size3; i++) {
            if (array[i] != array2[i]) {
                assertAlways("bytes not equal");
            }
        }
    }


    public void testInetConversions() {
        String[] addresses = {"66.155.7.20", "1.0.0.0", "1.1.1.1", "255.255.255.0", "172.16.20.57", "66.151.128.144", "192.168.1.40", "0.0.0.0", "255.255.255.255", "/255.255.255.255"};


        for (String address : addresses) {
            int intAddress = InetUtil.inetAddressToInt(address);
            String sourceAddress = address.replaceAll("[^0-9.]", Constants.EmptyString);
            String targetAddress = InetUtil.intToInetAddressString(intAddress);

            assertEquals("conversion of iNet address to int to iNet address", sourceAddress, targetAddress);


        }
    }
}


class TestComparator implements Comparator<String> {

    public int compare(String s, String s1) {
        return s.compareTo(s1);
    }
}

class StringComparitor implements Comparator<String> {

    public int compare(final String s, final String s1) {
        return s.compareTo(s1);
    }
}