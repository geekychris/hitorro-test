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

import com.hitorro.analysis.linkengine.LinkEngine;
import com.hitorro.analysis.linkengine.LinkQueryContext;
import com.hitorro.analysis.linkengine.LinkSegment;
import com.hitorro.analysis.linkengine.LinkSegmentWriter;
import gnu.trove.map.hash.TLongObjectHashMap;
import com.hitorro.analysis.linkengine.tuple.FilterLinkTupleReader;
import com.hitorro.analysis.linkengine.tuple.LinkTupleReader;
import com.hitorro.analysis.linkengine.tuple.filters.TypeFilter;
import com.hitorro.util.core.ArrayUtil;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.hash.FPHash64;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.html.Link;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Copyright (c) 2003 - 2009 HiTorro All rights reserved. User: chris Date: Mar 9, 2009 Time: 3:38:46 PM
 * <p/>
 * using a model graph in text form that we can create a simple link index and query for back references
 * <p/>
 * It creates 2 segments with different link sets in them.  As two segments we verify that we can perform a query, once
 * that is complete we release the query compContext so that we can optimize
 */
@HTTest(runlevel = RunLevel.Never,
        email = "chris@hitorro.com",
        description = "Link engine full test")

public class LinkEngineFullTest extends EnhancedTestCase {
    public static boolean assertForReal = true;
    public static boolean printData = true;

    public void test() throws IOException {
        File graphInput = this.getInputFileRelative("testgraph.txt");
        this.assertFileExistsWithExplanation(graphInput);

        File graphInput2 = this.getInputFileRelative("testgraph2.txt");
        this.assertFileExistsWithExplanation(graphInput2);


        List<Entry> entries = new ArrayList();
        List<Entry> entries2 = new ArrayList();
        TLongObjectHashMap<Entry> map = new TLongObjectHashMap();
        getEntitiesFromFile(graphInput, map, entries);
        getEntitiesFromFile(graphInput2, map, entries2);
        File leDir = this.getOutputFileRelative("smallgraphtest");
        FileUtil.deleteAndRecreateDir(leDir);
        LinkEngine le = new LinkEngine(Env.getBaseFile(leDir));
        LinkSegmentWriter lsw = le.getWriter();
        LinkSegmentWriter lsw2 = le.getWriter();
        List<Link> links = new ArrayList();
        List<Link> links2 = new ArrayList();
        translateEntryToLink(entries, links);

        translateEntryToLink(entries2, links2);
        LinkSegment ls = lsw.indexFromLinkIterator(links.iterator());

        le.commitWriter(lsw, ls);

        LinkSegment ls2 = lsw2.indexFromLinkIterator(links2.iterator());

        le.commitWriter(lsw2, ls2);
        LinkQueryContext lqc = le.getQueryContext();
        testValues(lqc, map);
        le.release(lqc);
        le.optimize();
        LinkQueryContext lqc2 = le.getQueryContext();
        testValues(lqc2, map);
        le.release(lqc2);

    }

    private void testValues(final LinkQueryContext lqc, final TLongObjectHashMap<Entry> map) throws IOException {
        getAndDump("http://b/a/b/d.txt", lqc, map);
        getAndDump("http://e/a/b/d.txt", lqc, map);
        getAndDump("http://zb/a/b/d.txt", lqc, map);
        getAndDump("http://ze/a/b/d.txt", lqc, map);
    }

    private void translateEntryToLink(final List<Entry> entries, final List<Link> links) throws MalformedURLException {
        for (Entry e : entries) {
            URL u = new URL(e.name);
            for (int i : e.pointsTo) {
                Entry target = entries.get(i);
                Link l = new Link(target.name, Link.LinkType.Anchor, "", "", u, e.name);
                links.add(l);

            }
        }
    }

    private void getEntitiesFromFile(final File graphInput, final TLongObjectHashMap<Entry> map, final List<Entry> entries) throws FileNotFoundException {
        Iterator<String> lines = FileUtil.getLineReaderIteratorFromFile(graphInput);
        while (lines.hasNext()) {
            String row = lines.next();
            String parts[] = StringUtil.tokenizeFromSingleChar(row, "|");
            if (parts.length > 0) {
                Entry ent = new Entry();
                ent.index = Integer.parseInt(parts[0]);
                ent.name = parts[1];
                ent.hash = FPHash64.getFP(ent.name);
                map.put(ent.hash, ent);
                if (parts.length > 2) {
                    String indexes[] = StringUtil.tokenizeFromSingleChar(parts[2], ",");
                    ent.pointsTo = ArrayUtil.getIntArrayFromStringArray(indexes);
                } else {
                    ent.pointsTo = new int[0];
                }
                entries.add(ent);
            }
        }
    }

    private void getAndDump(String url, LinkQueryContext lqc, TLongObjectHashMap<Entry> map) throws IOException {
        long hash = FPHash64.getFP(url);
        LinkTupleReader ltr = lqc.getReaderForDocId(hash);

        FilterLinkTupleReader reverseFilterReader =
                new FilterLinkTupleReader(new TypeFilter((byte) (LinkTupleReader.TypeBit_Inbound | LinkTupleReader.TypeHRef)));

        reverseFilterReader.setReader(ltr);
        assertNotNull(reverseFilterReader);
        dumpOrAssertBackRefs(url, reverseFilterReader, map);
    }

    public final void dumpOrAssertBackRefs(String url, LinkTupleReader reader,
                                           TLongObjectHashMap<Entry> map) throws IOException {
        if (printData) {
            Console.println(url);
            Console.println();
        }
        Entry source = map.get(FPHash64.getFP(url));


        while (reader.readNext()) {
            if (LinkTupleReader.hasMetric(reader.m_typeCode)) {
                if (printData) {
                    Console.println("DocID: %s, type: %s, metric: %s , date: %s", reader.getDocumentId(),
                            reader.m_typeCode,
                            reader.getMetric(),
                            reader.getDate());
                }
            } else {
                Entry e = map.get(reader.getDocumentId());
                String name = "<unknown>";
                int intPos = -1;
                if (e != null) {
                    name = e.name;
                    intPos = e.index;

                    boolean b = ArrayUtil.containsInIntArray(e.pointsTo, source.index);
                    if (assertForReal) {
                        assertTrue(b);
                    }
                }
                if (printData) {
                    Console.println("DocID: %s, name: %s, intPos: %s, type: %s , date: %s", reader.getDocumentId(), name, intPos, reader.m_typeCode, reader.getDate());
                }
            }
        }
    }
}

class Entry {
    String name;
    int index;
    long hash;
    int pointsTo[];
}
