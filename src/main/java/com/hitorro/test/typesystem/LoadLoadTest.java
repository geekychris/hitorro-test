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
package com.hitorro.test.typesystem;

import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;


@HTTest(runlevel = RunLevel.Never,
        email = "chris@hitorro.com",
        description = "Load tests")
public class LoadLoadTest extends DumpLoadTest {
    public void setUp() {
        super.setUp();
    }

    public void testLoad() {
        /*  no-op.  deliberately clobber the parent class.  */
    }


    public void testDump() {
        /*  no-op.  deliberately clobber the parent class.  */
    }


    public void testLoadLoad() {
        super.testLoadLoad();

    }


    public void verifyDump() {
        /*  no-op.  deliberately clobber the parent class.  */
    }


    public void verifyLoad() {
        /*  no-op.  deliberately clobber the parent class.  */
    }
}
