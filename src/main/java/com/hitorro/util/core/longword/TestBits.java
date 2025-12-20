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
package com.hitorro.util.core.longword;

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.fs.file.FileFileSystem;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.longword.opers.BitSetOperator;
import com.hitorro.util.core.longword.opers.BitsEqual;
import com.hitorro.util.core.longword.opers.LogicalAndOperator;
import com.hitorro.util.core.longword.opers.NotOperator;
import com.hitorro.util.core.map.FPHashLongMap;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.json.keys.propaccess.PropaccessError;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;

/**
 * Test of the Named bits
 */
@HTTest(runlevel = RunLevel.Smoke,
        email = "chris@hitorro.com",
        description = "Long word bit manipulation")
public class TestBits extends EnhancedTestCase {
    public void testNamedBits() {
        long l = 0;
        com.hitorro.util.core.longword.NamedBitsOfLong a = new com.hitorro.util.core.longword.NamedBitsOfLong(null, "a", 0, 1);
        com.hitorro.util.core.longword.NamedBitsOfLong b = new com.hitorro.util.core.longword.NamedBitsOfLong(null, "b", a.getBitOffset() + 1, 1);
        com.hitorro.util.core.longword.NamedBitsOfLong c = new com.hitorro.util.core.longword.NamedBitsOfLong(null, "c", b.getBitOffset() + 1, 1);
        com.hitorro.util.core.longword.NamedBitsOfLong d = new com.hitorro.util.core.longword.NamedBitsOfLong(null, "d", c.getBitOffset() + 1, 1);
        com.hitorro.util.core.longword.NamedBitsOfLong e = new com.hitorro.util.core.longword.NamedBitsOfLong(null, "e", c.getBitOffset() + 1, 3);
        l = a.set(l);
        l = b.clear(l);
        l = c.clear(l);
        l = d.set(l);
        l = e.set(l, 7);
        l = e.set(l, 5);
        Console.println("%s", l);
        assertTrue("Bits a and d should be setPhrases", a.isSet(l) || d.isSet(l));

        assertTrue("bits b and c should not be setPhrases", !b.isSet(l) || !d.isSet(l));
        long v = e.get(l);
        assertEquals("v should be 5", v, 5);
    }

    public void testWordBits() {
        com.hitorro.util.core.longword.WordBits bits = new com.hitorro.util.core.longword.WordBits();
        bits.add("a", 1);
        bits.add("b", 1);
        bits.add("c", 4);
        bits.add("d", 5);
        bits.add("e", 1);
        long l = 0;
        String stringOfBits = "a=1 b=0 c=5 d=19 e=1";
        try {
            l = bits.setFromKeyValueString(stringOfBits, l);
        } catch (ParseException | PropaccessError e1) {
            assertAlways("Parsing exception converting key value pairs to longword bits");
        }
        StringBuilder builder = new StringBuilder();
        bits.dumpLongToString(builder, l);
        assertEquals(stringOfBits, builder.toString());
    }

    public void testConstraint() throws PropaccessError {
        //assertAlways("Hao broke it 10 million times!!");
        com.hitorro.util.core.longword.WordBits bits = new com.hitorro.util.core.longword.WordBits();
        com.hitorro.util.core.longword.BiDirectionalKeyLongMap mlm = new com.hitorro.util.core.longword.BiDirectionalKeyLongMap(true);
        mlm.add("M", 1);
        mlm.add("F", 2);
        mlm.add("U", 0);
        bits.add("a", 1);
        bits.add("b", 1);
        bits.add("c", 4);
        bits.add("d", 5);
        bits.add("e", 1);
        bits.add("sex", 2, mlm);
        long l = 0;
        LogicalAndOperator op = new LogicalAndOperator(new BitSetOperator(bits, "a"),
                new NotOperator(new BitSetOperator(bits, "b")),
                new BitsEqual(bits, "d", 18, BitsEqual.Oper.GreaterThanEqual));
        try {
            l = bits.setFromKeyValueString("a=1 b=0 c=5 d=19 e=1", l);
        } catch (ParseException e1) {
            assertAlways("Parsing exception converting key value pairs to longword bits");
        }

        assertTrue(op.match(l));
        try {
            File in = this.getInputFileRelative("maps");
            File out = this.getOutputFileRelative("maps");
            this.assertFileExistsWithExplanation(in);
            FileUtil.deleteDirectoryContent(out, true);
            FileUtil.ensureDirectoryExists(out);
            FileUtil.copyDirectory(in, out);
            BaseFile bOut = FileFileSystem.Root.getFile(out.getAbsolutePath());
            FPHashLongMap map = FPHashLongMap.loadFromDirectory(bits, bOut, 10, true, true);
            File hashFile = new File(out, "hash");
            assertFileExistsWithExplanation(hashFile);
            long val = map.get("chris collins", 0);
            StringBuilder builder = new StringBuilder();
            bits.dumpLongToString(builder, val);
            Console.println("%s", builder.toString());
        } catch (IOException e) {
            assertAlways("Expected it not to blow up");
        } catch (ParseException e) {
            assertAlways("Expected it not to blow up");
        }
    }
}
