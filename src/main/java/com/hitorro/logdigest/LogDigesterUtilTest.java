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
package com.hitorro.logdigest;

import com.hitorro.logdigest.requestclassifier.ClassifierContext;
import com.hitorro.util.core.ByteManipulationUtil;
import com.hitorro.util.core.UTCDateUtil;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 */
@HTTest(runlevel = RunLevel.Never,
        email = "chris@hitorro.com",
        description = "Baseic log processor tests")
public class LogDigesterUtilTest extends EnhancedTestCase {
    private static final String testDate = "09/Dec/2007 23:10:22 +0000";
    private static SimpleDateFormat LogDateFormat = new SimpleDateFormat("dd/MMM/yyyy HH:mm:ss Z");

    public void testIntByteMerge() {
        int iIn = 5678653;
        byte bIn = 10;
        long l = ByteManipulationUtil.combineIntByte(iIn, bIn);

        byte b = ByteManipulationUtil.intByteBytePart(l);
        assertEquals(bIn, b);
        int i = ByteManipulationUtil.intByteIntPart(l);
        assertEquals(iIn, i);
    }

    public void testDateExtraction() {
        Date d = null;
        try {
            d = LogDateFormat.parse(testDate);
        } catch (ParseException e) {
            assertAlways("should not have a parse exception");
        }
        int i = UTCDateUtil.dateAsIntFromCalendar(d);
        assertEquals(i, 20071209);


    }

    public void testClassifier() {
        ClassifierContext context = new ClassifierContext();
        assertTrue(context.selfTest());
    }


}
