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
package com.hitorro.test.cglib;

import com.hitorro.language.Iso639Table;
import com.hitorro.obj.core.GenericAnalyzer;
import com.hitorro.util.core.Console;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.objectweb.asm.*;

import java.io.IOException;
import java.io.StringReader;


/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * User: chris Date: Nov 21, 2006 Time: 8:13:00 PM
 */

@HTTest(runlevel = RunLevel.Smoke,
        email = "chris@hitorro.com",
        description = "CGLib tests")
public class TestCGLib extends EnhancedTestCase {
    public void testJsqlParser() throws IOException {
        String tst = "this isnt all that it seems running a blaa blaa";
        GenericAnalyzer ga = new GenericAnalyzer("standard, snowballstemmer ", Iso639Table.english, GenericAnalyzer.Mode.Index);
        StringReader sr = new StringReader(tst);
        TokenStream ts = ga.tokenStream("tst", sr);
        CharTermAttribute termAttribute = ts.getAttribute(CharTermAttribute.class);
        while (ts.incrementToken()) {
            String t = termAttribute.toString();
            Console.println("token: %s", t);
        }
        ts.close();

    }
}

class MyTraceMethodVisitor extends MethodVisitor {
    public MyTraceMethodVisitor(MethodVisitor mv) {
        super(Opcodes.ASM4, mv);
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
    }
}

class MyClassVisitor extends ClassVisitor {
    public MyClassVisitor(ClassVisitor cv) {
        super(Opcodes.ASM6, cv);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc,
                                   String signature, Object value) {
        return null;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
                                     String signature, String[] exceptions) {

        if (name.equals("get777")) {
            return new MyTraceMethodVisitor(super.visitMethod(access, name, desc, signature, exceptions));
        }

        return null;
    }
}
