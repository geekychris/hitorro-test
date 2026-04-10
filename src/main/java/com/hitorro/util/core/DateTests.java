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
package com.hitorro.util.core;

import com.hitorro.util.core.date.DateRange;
import com.hitorro.util.core.date.DateRangeSnapper;
import com.hitorro.util.core.date.DateResolution;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;
import junit.framework.TestCase;

import java.text.ParseException;
import java.util.Date;


@HTTest(runlevel = RunLevel.Smoke,
        email = "chris@hitorro.com",
        description = "Test date oriented util methods")
public class DateTests extends EnhancedTestCase {
    public void testDateRange() throws ParseException {
        String oneWk = "091206-091213";
        DateRange dr = new DateRange(oneWk);

        TestCase.assertEquals(oneWk, dr.getDateRangeString());
        long durationMillis = dr.getDuration();
        DateResolution range = dr.getDateRange();
        TestCase.assertEquals(range, DateResolution.Week);
    }

    public void testDayOfWeekSnapping() throws ParseException {
        String endOfDay = "0912122359";

        String startOfDay = "0912130001";
        Date startDay = DateResolution.Minute.parse(endOfDay);
        startDay = com.hitorro.util.core.UTCDateUtil.snapToWeek(startDay);
        String s = DateResolution.Minute.getFormatted(startDay);
        TestCase.assertEquals(s, "0912060000");
        Date endDay = DateResolution.Minute.parse(startOfDay);
        endDay = com.hitorro.util.core.UTCDateUtil.snapToWeek(endDay);
        String e = DateResolution.Minute.getFormatted(endDay);
        TestCase.assertEquals(e, "0912130000");

        // now try milliseconds
        DateRange dr = new DateRange(startDay.getTime() + 1007, DateResolution.Day, DateResolution.Week);
        String drS = dr.getDateRangeString();
        TestCase.assertEquals(drS, "091206-091213");

        DateRange next = dr.getNextInSeries();
        String nextS = next.getDateRangeString();
        TestCase.assertEquals(nextS, "091213-091220");

        DateRange dayRange = new DateRange(startDay.getTime() + 1007, DateResolution.Hour, DateResolution.Day);
        String dayRangeS = dayRange.getDateRangeString();
        TestCase.assertEquals(dayRangeS, "09120600-09120700");


    }


    public void testShardKey() throws ParseException {
        String oneWk = "091206-091213-foo";
        DateRange dr = new DateRange("091206-091213");
        com.hitorro.util.core.ShardKey key = new com.hitorro.util.core.ShardKey(oneWk);

        TestCase.assertEquals("foo", key.getSubSegment());
        DateRange resultDR = key.getDateRange();
        TestCase.assertEquals(dr, resultDR);
        String fn = key.getFullName();
    }

    public void testDataSnapper() {
        DateRangeSnapper snapper = new DateRangeSnapper(DateResolution.Week);
        Date now = new Date();
        long nowM = now.getTime();
        long snap1 = snapper.getSnappedTime(nowM);

        String s = new Date(snap1).toString();
        long end = snap1 + com.hitorro.util.core.Constants.MillisInYear + com.hitorro.util.core.Constants.MillisInWeek;
        com.hitorro.util.core.Timer t = new com.hitorro.util.core.Timer();
        t.reset();
        int counter = 0;

        counter = 0;
        for (int i = 0; i < 5; i++) {
            snapper = new DateRangeSnapper(DateResolution.Week);
            snap1 = snapper.getSnappedTime(nowM);
            t.reset();
            while (snap1 < end) {
                long l = snapper.getSnappedTime(snap1);
                snap1 += com.hitorro.util.core.Constants.MillisInSecond;
                counter++;
            }
            t.stop();
            com.hitorro.util.core.Console.println("%s incs %sms count:%s", snapper.getNewCount(), t.getTime(), counter);
            TestCase.assertEquals(54, snapper.getNewCount());
        }


    }
}

