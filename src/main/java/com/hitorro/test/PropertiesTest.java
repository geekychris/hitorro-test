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

import com.hitorro.util.core.params.HTProperties;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;

import java.util.List;
import java.util.Properties;

/**
 * Test of the HTProperties class, and properties in general.
 *
 * @author chris
 */
@HTTest(runlevel = RunLevel.Smoke,
        email = "chris@hitorro.com",
        description = "Test of HTProperties")
public class PropertiesTest extends EnhancedTestCase {

    // test properties
    private HTProperties _properties;

    @Override
    protected void setUp()
            throws Exception {

        createTestProperties();
        super.setUp();
    }

    public void testResolve() {
        // during setup we didn't resolve the property value, do it now
        String var = "dinner.main";
        assertTrue(_properties.get(var).equals("we will be having fried ${animal.reptile.tasty}"));
        _properties.resolveVariables();
        assertTrue(_properties.get(var).equals("we will be having fried rattlesnake"));
    }

    public void testChildKeys() {
        List<String> childKeys = _properties.getChildKeys("animal");
        // we should have only the child keys mammal and reptile
        assertTrue(childKeys.size() == 2);
        assertTrue(childKeys.contains("mammal"));
        assertTrue(childKeys.contains("reptile"));

        // listFiles with a null key should be a null listFiles
        assertNull(_properties.getChildKeys(null));
    }

    public void testChildProperties() {
        // we should be able to try to get a child apply with a null key without an explosion
        Properties subp = _properties.getSubProperties(null);
        assertEquals(0, subp.size());

        // the non-null case
        subp = _properties.getSubProperties("server.hibernate");
        // should just have the connection keys
        assertEquals(2, subp.size());
        assertNotNull(subp.get("connection.user"));
        assertNotNull(subp.get("connection.password"));
    }

    /**
     * Create a useful
     *
     * @return
     */
    private void createTestProperties() {
        _properties = new HTProperties();
        _properties.setProperty("animal.mammal.favorite", "zebra");
        _properties.setProperty("animal.mammal.ugly", "kangaroo");
        _properties.setProperty("animal.reptile.favorite", "alligator");
        _properties.setProperty("animal", "starfish");
        _properties.setProperty("animal.reptile.tasty", "rattlesnake");
        _properties.setProperty("server.hibernate.connection.user", "scott");
        _properties.setProperty("server.hibernate.connection.password", "tiger");
        // don't resolve the property yet - let that happen in the test
        _properties.setProperty("dinner.main", "we will be having fried ${animal.reptile.tasty}");
    }

}
