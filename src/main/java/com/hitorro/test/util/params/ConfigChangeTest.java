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
package com.hitorro.test.util.params;

import com.hitorro.jsontypesystem.JVS;
import com.hitorro.jsontypesystem.propreaders.JVSChangeContainer;
import com.hitorro.jsontypesystem.propreaders.JVSConfigChangeRegistry;
import com.hitorro.jsontypesystem.propreaders.JVSConfigDiffer;
import com.hitorro.util.core.params.ConfigChange;
import com.hitorro.util.json.keys.propaccess.PropaccessError;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * ht.test.util.params.ConfigChangeTest User: chris
 */
@HTTest(runlevel = RunLevel.Smoke,
        email = "chris@hitorro.com",
        description = "Test configuration changes.")
public class ConfigChangeTest extends EnhancedTestCase {
    public void test() throws PropaccessError {
        JVSConfigChangeRegistry reg = new JVSConfigChangeRegistry();
        JVSChangeContainer ab = new JVSChangeContainer();
        JVSChangeContainer a = new JVSChangeContainer();
        JVSChangeContainer c = new JVSChangeContainer();
        reg.addObserver("a.b", ab);
        reg.addObserver("a", a);
        reg.addObserver("c", c);
        JVS old = new JVS();
        JVS newC = new JVS();

        old.set("a.b.a", "a.b.old");
        old.set("a.b.c", "a.b.c.old");
        old.set("a.c.c", "a.c.c.old");
        old.set("b.b.c", "b.b.c.old");

        newC.set("a.b.a", "a.b.old");
        newC.set("a.b.c", "a.b.c.new");
        newC.set("b.b.c", "b.b.c.old");
        newC.set("c.c", "c.c.new");
        JVSConfigDiffer differ = new JVSConfigDiffer(old, newC);
        Iterator<ConfigChange> iter = differ.getDiffs();
        Map<String, ConfigChange> changes = new HashMap();
        while (iter.hasNext()) {
            ConfigChange cc = iter.next();
            changes.put(cc.getKey(), cc);
        }
        assertTrue(hasChange(changes, ConfigChange.ConfigChangeType.Updated, "a.b.c"));

        assertTrue(hasChange(changes, ConfigChange.ConfigChangeType.Deleted, "a.c.c"));

        assertTrue(hasChange(changes, ConfigChange.ConfigChangeType.Added, "c.c"));
        reg.runDiff(old, newC);
    }

    private boolean hasChange(Map<String, ConfigChange> changes, ConfigChange.ConfigChangeType type, String key) {
        ConfigChange change = changes.get(key);
        if (change == null) {
            return false;
        }
        if (change.getType() != type) {
            return false;
        }
        return change.getKey().equals(key);
    }
}

