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

import com.hitorro.util.core.Env;
import com.hitorro.util.core.Platform;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;

import java.io.File;

@HTTest(runlevel = RunLevel.Smoke,
        email = "chris@hitorro.com",
        description = "Test FileUtil tests")
public class BasicFileUtilTest extends EnhancedTestCase {
    /**
     * Ensure we get a hexdec path for the id.
     */
    public void testHexId() {
        String s = FileUtil.idToHexPath(992392343);
        assertEquals(s, "/00/00/00/00/3b/26/b4/97");
    }

    /**
     * Test file linking
     *
     * @throws Exception
     */
    public void testLink() throws Exception {
        File openResourceDir = Env.getOpenResourceDir();
        File testDir = new File(openResourceDir, "linktest");
        testDir.mkdirs();
        File testFile = new File(testDir, "sourceFile.txt");
        long time = System.currentTimeMillis();
        FileUtil.writeLongValToFile(testFile, time);
        File linkFile = new File(testDir, "linkFile.txt");
        linkFile.delete();
        Platform.getPlatform().softLink(testFile, linkFile);
        this.assertFileExistsWithExplanation(linkFile);
        long val = FileUtil.readLongValToFile(linkFile);
        assertEquals(time, val);
    }
}
