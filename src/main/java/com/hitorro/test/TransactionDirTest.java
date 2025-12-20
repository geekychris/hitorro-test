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

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.fs.file.FileFileSystem;
import com.hitorro.util.basefile.tools.transactiondir.TransactingDirectory;
import com.hitorro.util.basefile.tools.transactiondir.TransactionException;
import com.hitorro.util.core.Env;
import com.hitorro.util.testframework.EnhancedTestCase;

import java.io.IOException;

/**
 * Test transaction files
 */
/*@HTTest(runlevel = RunLevel.Smoke,
        email = "chris@hitorro.com",
        description = "Series of resource cache tests")*/
public class TransactionDirTest extends EnhancedTestCase {
    public void testTransactionDir() throws IOException, TransactionException {
        BaseFile root = FileFileSystem.Root.getFile(Env.getHome().getAbsolutePath());
        BaseFile tdir = root.getChild("transdir");
        tdir.mkdir();
        TransactingDirectory td = new TransactingDirectory(tdir);
        td.start();
        td.getTransactionFile("trans1.txt").writeString("one");
        td.commit();
        td.start();
        td.getTransactionFile("trans2.txt").writeString("two");
        td.getTransactionFile("trans1.txt").writeString("1-1");
        td.commit();
        td.start();
        BaseFile bf = td.getTransactionFile("trans3.txt");
        td.getTransactionFile("trans3.txt").writeString("three");
        td.getTransactionFile("trans1.txt").writeString("1-2");
        td.commit();
        BaseFile bf2 = td.getCommittedFile("trans1.txt");
        assertEquals(bf2.readString(), "1-2");
        assertEquals(td.getCommittedFile("trans2.txt").readString(), "two");
        assertEquals(td.getCommittedFile("trans3.txt").readString(), "three");

    }

}
