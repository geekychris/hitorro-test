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

import com.hitorro.basedms.NamedLong;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;

/**
 * Tests the creation, the deletion and the re-use of a named long. Note that we also make sure we get the correct
 * numbers out of the long
 *
 * @author chris
 */
@HTTest(runlevel = RunLevel.Smoke,
        email = "chris@hitorro.com",
        description = "Test named longs")
public class NamedLongTest extends EnhancedTestCase {
    /**
     *
     */
    public void testName() {
        NamedLong nl = NamedLong.registerNamedLong("test", 1, 100, "Test named long");
        nl.delete();
        for (int i = 1; i < 1002; i++) {
            long id = nl.getNextValue();
            assertEquals(i, id);
        }

        NamedLong nl2 = NamedLong.registerNamedLong("test", 1, 100, "Test named long");
        for (int i = 1101; i < 2000; i++) {
            long id = nl2.getNextValue();
            assertEquals(i, id);
        }
    }
}
