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
package com.hitorro.basedms.nanny;

import com.hitorro.util.cmdline.CommandLine;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.Platform;
import com.hitorro.util.io.RotatingBufferedOutputStream;
import com.hitorro.util.osprocessexec.ExecContext;
import com.hitorro.util.osprocessexec.JavaExecWrapper;
import com.hitorro.util.osprocessexec.TerminationKey;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;
import com.hitorro.util.versioning.StringVersionNode;
import com.hitorro.util.versioning.VersionTree;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Dec 2, 2006 Time: 5:09:46 PM
 */
@HTTest(runlevel = RunLevel.Never,
        email = "chris@hitorro.com",
        description = "Tests related to nanny process")
public class NannyTest extends EnhancedTestCase implements TerminationKey {
    private boolean m_done = false;
    private int m_exitCode;

    public void testVersion() {
        VersionTree<StringVersionNode> vt = new VersionTree<StringVersionNode>();
        vt.addVersion(new StringVersionNode("1.2.3.444", 1, 2, 3, 44));
        vt.addVersion(new StringVersionNode("1.2.3.555", 1, 2, 3, 555));
        vt.addVersion(new StringVersionNode("2.1.3.666", 2, 1, 3, 666));

        vt.addVersion(new StringVersionNode("2.1.4.111", 2, 1, 4, 111));
        vt.addVersion(new StringVersionNode("3.1.4.111", 3, 1, 4, 111));

        StringVersionNode svn = vt.getNode("*");
        assertEquals(svn.getString(), "3.1.4.111");

        svn = vt.getNode("2.1.*");
        assertEquals(svn.getString(), "2.1.4.111");

        svn = vt.getNode("2.1+");
        assertEquals(svn.getString(), "2.1.4.111");

        svn = vt.getNode("1.2.3.*");
        assertEquals(svn.getString(), "1.2.3.555");
    }

    public void testBuffer() throws IOException {
        RotatingBufferedOutputStream b = new RotatingBufferedOutputStream(10);
        byte[] buff = new String("The quick brown fox jumped over the lazy dogs").getBytes();
        b.write(buff);
        byte buffNew[] = b.getWindow();
        String newString = new String(buffNew);
        assertEquals(" lazy dogs", newString);

    }

    public void testRunHelloWorld() throws Exception {
        JavaExecWrapper cc = getJavaContext();

        StringBuilder b = new StringBuilder();
        StringBuilder e = new StringBuilder();
        int exitCode = cc.simpleExec(b, e);
        assertEquals(e.length(), 0);
        assertEquals(exitCode, 12);
    }

    public void testRunHelloWorldAsyncExec() throws Exception {
        JavaExecWrapper cc = getJavaContext();

        ExecContext ec = new ExecContext(this);
        ec.setOutputFile(this.getOutputFileRelative("out.txt"));
        ec.setErrorFile(this.getOutputFileRelative("err.txt"));
        ec.setProgram(cc.getExecString());
        if (ec.exec()) {
            int counter = 100;
            while (m_done == false || counter > 0) {
                Env.sleepNSeconds(1);
                counter--;
            }
            assertEquals(m_done, true);
            assertEquals(m_exitCode, 12);
        } else {
            assertAlways("Did not exec");
        }
    }

    /**
     * Test using async callback mechanism for execing a process
     *
     * @return
     */
    private JavaExecWrapper getJavaContext() {
        Map<String, String> args = new HashMap<String, String>();

        args.put("keepalive", "10");
        args.put("command", "com.hitorro.util.service.HTServer");
        args.put("servertype", "hello");


        JavaExecWrapper cc = new JavaExecWrapper(Env.getBin(),
                Env.getHome(),
                Platform.getPlatform().getJavaRoot(),
                CommandLine.class.getCanonicalName(),
                new ArrayList<String>(),
                args);
        return cc;
    }

    public void complete(int exitCode) {
        m_done = true;
        m_exitCode = exitCode;
    }
}
