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
package com.hitorro.analysis.linkengine;

import com.hitorro.analysis.linkengine.tuple.Count;
import com.hitorro.analysis.linkengine.tuple.CountDistinctId;
import com.hitorro.analysis.linkengine.tuple.FilterLinkTupleReader;
import com.hitorro.analysis.linkengine.tuple.LinkTupleReader;
import com.hitorro.analysis.linkengine.tuple.RLEISLinkTupleReader;
import com.hitorro.analysis.linkengine.tuple.RLELinkTupleWriter;
import com.hitorro.analysis.linkengine.tuple.TupleORSelectTreeReader;
import com.hitorro.analysis.linkengine.tuple.filters.ANDTupleReaderFilter;
import com.hitorro.analysis.linkengine.tuple.filters.DateRangeFilter;
import com.hitorro.analysis.linkengine.tuple.filters.TypeFilter;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.UTCDateUtil;
import com.hitorro.util.core.hash.FPHash64;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.io.largedata.compressedstreams.COutputStream;
import com.hitorro.util.io.largedata.compressedstreams.RAMFile;
import com.hitorro.util.io.largedata.compressedstreams.RAMInputStream;
import com.hitorro.util.io.largedata.compressedstreams.RAMOutputStream;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;

import java.io.IOException;
import java.util.Date;

/**
 * ht.analysis.linkengine.RLELinkEnginePrimitivesTest
 * <p/>
 * <p/>
 */
@HTTest(runlevel = RunLevel.Full,
        email = "chris@hitorro.com",
        description = "Basic RLE primitives used by the link engine")
public class RLELinkEnginePrimitivesTest extends EnhancedTestCase {
    public static final String RowA = "1,href,127,1000|5,href,0,1000|5,similar,127,1000|9,href,0,1000|288,href,0,1007";
    public static final String RowB = "2,href,0,1000|5,similar,127,1000|7,href,127,1000|7,similar,127,1000|9,href,55,1000|289,href,0,1007";

    public static final String RowC = "1,href,127,1000|2,href,0,1000|5,href,0,1000|5,similar,127,1000|7,href,127,1000|7,similar,127,1000|9,href,0,1000|288,href,0,1007|289,href,0,1007";
    public static final String RowD = "1,href,127,1000|2,href,0,1000|5,href,0,1000|7,href,127,1000|9,href,0,1000";


    public static final String RowWithNegVals = "-100,href,-98,1000|2,href,0,1000|5,href,0,1000|7,href,127,1000|9,href,0,1000";

    public static final String BasicAreaToPostQuery = " from User as user, RssFeedIn as feed, Forum as forum, SubjectArea as area, Post as post \n" +
            "where user.rssFeedIn = feed and " +
            "feed.forum = forum and " +
            "post.containers.id = forum.id and " +
            "forum.area = area and area.name= :a";

    public static final String BasicAreaToPostQuery_Count = "select count(*)" + BasicAreaToPostQuery;

    public static final String BasicAreaToPostQuery_PostHash_Forum = "select post.identityHash, post.moreLink, forum.name" + BasicAreaToPostQuery;

    /**
     * @param row
     * @param os
     * @return
     * @throws java.io.IOException
     */
    public boolean write(String row, COutputStream os) throws IOException {
        RLELinkTupleWriter writer = new RLELinkTupleWriter();
        writer.setOutputStream(os);
        String tokens[] = StringUtil.tokenizeFromSingleChar(row, "|");
        for (String r : tokens) {
            String parts[] = StringUtil.tokenizeFromSingleChar(r, ",");
            if (parts.length != 4) {
                return false;
            }
            writer.writeTuple(Long.parseLong(parts[0]), LinkTupleReader.getTypeByteFromName(parts[1]), Byte.parseByte(parts[2]), Short.parseShort(parts[3]));

        }
        writer.close();
        return true;
    }

    public boolean writeWithHash(String row, COutputStream os) throws IOException {
        RLELinkTupleWriter writer = new RLELinkTupleWriter();
        writer.setOutputStream(os);
        String tokens[] = StringUtil.tokenizeFromSingleChar(row, "|");
        for (String r : tokens) {
            String parts[] = StringUtil.tokenizeFromSingleChar(r, ",");
            if (parts.length != 4) {
                return false;
            }
            String name = parts[0];
            long hash = FPHash64.getFP(name);
            Console.println("hash:%s", hash);
            writer.writeTuple(hash, LinkTupleReader.getTypeByteFromName(parts[1]), Byte.parseByte(parts[2]), Short.parseShort(parts[3]));

        }
        writer.close();
        return true;
    }

    public void testNegativeRow() throws IOException {
        RAMOutputStream os = new RAMOutputStream();
        long rowAPtr = 0;
        assertTrue(write(RowWithNegVals, os));

        RAMFile file = os.getFlushedRamFile();

        RAMInputStream is = new RAMInputStream(file);

        RLEISLinkTupleReader reader = new RLEISLinkTupleReader();
        reader.setInputStream(is, rowAPtr);
        reader.reset();
        while (reader.readNext()) {
            Console.println("DocId: %s", reader.getDocumentId());
        }


    }

    public void testRLE() throws IOException {
        RAMOutputStream os = new RAMOutputStream();
        long rowAPtr = 0;
        assertTrue(write(RowA, os));
        long rowBPtr = os.getFilePointer();
        assertTrue(write(RowB, os));
        long rowCPtr = os.getFilePointer();
        assertTrue(write(RowC, os));
        long rowDPtr = os.getFilePointer();
        assertTrue(write(RowD, os));
        long endPoint = os.getFilePointer();


        RAMFile file = os.getFlushedRamFile();

        RAMInputStream is = new RAMInputStream(file);

        RLEISLinkTupleReader reader = new RLEISLinkTupleReader();
        reader.setInputStream(is, rowAPtr);
        //dumpReader("A", reader);
        RAMInputStream is2 = new RAMInputStream(file);
        RLEISLinkTupleReader reader2 = new RLEISLinkTupleReader();
        reader2.setInputStream(is2, rowBPtr);

        //dumpReader("b", reader2);

        TupleORSelectTreeReader orReader = new TupleORSelectTreeReader();
        orReader.set(reader, reader2);

        orReader.reset();

        RLEISLinkTupleReader rowCReader = new RLEISLinkTupleReader();
        RAMInputStream rowCIS = new RAMInputStream(file);
        rowCReader.setInputStream(rowCIS, rowCPtr);

        // ensure that the merged streams are the same as the complete stream.
        assertReadersSame(orReader, rowCReader);

        //dumpReader("or", orReader);
        orReader.reset();
        Count c = new Count();
        c.set(orReader);
        assertEquals(9, c.getInt());
        //Console.println("Count: %s", c.getInt());
        CountDistinctId cd = new CountDistinctId();
        cd.set(orReader);
        orReader.reset();
        assertEquals(7, cd.getInt());
        //Console.println("Count Distinct: %s", cd.getInt());
        FilterLinkTupleReader filter =
                new FilterLinkTupleReader(new ANDTupleReaderFilter(new DateRangeFilter((short) 1000, (short) 1000),
                        new TypeFilter(LinkTupleReader.TypeHRef)));
        filter.setReader(orReader);
        filter.reset();
        RLEISLinkTupleReader rowDReader = new RLEISLinkTupleReader();
        RAMInputStream rowDIS = new RAMInputStream(file);
        rowDReader.setInputStream(rowDIS, rowDPtr);
        assertReadersSame(filter, rowDReader);

        //dumpReader("HTPredicate", filter);

    }

    public void assertReadersSame(LinkTupleReader a, LinkTupleReader b) throws IOException {
        a.reset();
        b.reset();
        while (a.readNext()) {
            if (!b.readNext()) {
                assertAlways("readers not same length");
            }
            assertReaderTuples(a, b);
        }
    }

    public void assertReaderTuples(LinkTupleReader a, LinkTupleReader b) {
        assertEquals(a.m_readerDocumentId, b.m_readerDocumentId);
        assertEquals(a.m_typeCode, b.m_typeCode);
        if (LinkTupleReader.hasMetric(a.m_typeCode)) {
            assertEquals(a.m_metric, b.m_metric);
        }
        assertEquals(a.m_date, b.m_date);
    }

    public void testDates() {
        Date now = new Date();
        short shortDate = UTCDateUtil.dateToShortDate(now);
        Date returned = UTCDateUtil.shortDateToDate(shortDate);
        short returnedDate = UTCDateUtil.dateToShortDate(returned);
        assertEquals(shortDate, returnedDate);
    }
}
