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
package com.hitorro.basetext.test;

import com.hitorro.base.objects.Post;
import com.hitorro.basedms.session.DMSSessionFactory;
import com.hitorro.basetext.dfindex.DFIndex;
import com.hitorro.basetext.dfindex.DFIndexSingletonMapper;
import com.hitorro.basetext.indexer.AllTypesAnalyzerCache;
import com.hitorro.basetext.indexer.IndexerFieldAdapter;
import com.hitorro.basetext.inverter.DocumentCollection;
import com.hitorro.basetext.inverter.DocumentInverter;
import com.hitorro.basetext.inverter.SPResult;
import com.hitorro.basetext.inverter.TFIDFTermMeasureFunction;
import com.hitorro.basetext.inverter.TermTuple;
import com.hitorro.basetext.inverter.TermTupleSet;
import com.hitorro.basetext.inverter.TermTupleSetGroup;
import com.hitorro.basetext.inverter.TestVisitor;
import com.hitorro.basetext.indexer.SearcherCache;
import com.hitorro.language.Iso639Table;
import com.hitorro.language.IsoLanguage;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.math.SparseVector;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;
import com.hitorro.util.typesystem.BaseSession;
import com.hitorro.util.typesystem.HTSerializable;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@HTTest(runlevel = RunLevel.Smoke,
        email = "chris@hitorro.com",
        description = "Test the text filters")
public class TestInverter extends EnhancedTestCase {
    public static final String T = "The quick Brown fox Jumped over the lazy Dogs";
    public static final String T2 = "QuIck chris Fox blow a Dog";

    public static final String queryOld = "select s.guid as sysguid, c.guid as contguid from htcms.VersionableObject s, htcms.Content c, htcms.sysobject_contents sc where s.system_id= sc.system_id and c.content_id = sc.content_id and c.content_id in (select system_id from htcms.content_category cc where cc.domain=\"docparts\" and cc.value=\"reflinksnew\")";


    /**
     * Ensure that tokenization works for a simple string. Ensure that given two tokenization lists ordered by hash, we
     * can look for common terms.
     *
     * @throws java.io.IOException
     */
    public void testInverterAndHashMatch() throws IOException {
        for (int i = 0; i < 100; i++) {
            Log.test.info("execution number %s", i);
            ex();
        }
    }

    private void ex() throws IOException {
        IsoLanguage language = Iso639Table.english;
        DocumentInverter inverter = new DocumentInverter("body", null, null, TermTupleSet.s_HashAscend);
        TermTupleSet set = inverter.setText("body", T, language);
        TermTupleSet set2 = inverter.setText("body", T2, language);
        TestVisitor v = new TestVisitor();

        set.matchByHash(v, set2);

        assertEquals(2, v.m_map.size());
        assertMapContains(v.m_map, "quick");
        assertMapContains(v.m_map, "fox");
    }

    /**
     * Test only usefull if there is a df index around.
     *
     * @throws IOException
     */
    public void teestTFIDFDocSimilarity() throws IOException {
        IsoLanguage language = Iso639Table.english;
        DFIndex index = DFIndexSingletonMapper.getSingleton().get();
        assertNotNull(index);
        DocumentInverter inverter = new DocumentInverter("body", null, new TFIDFTermMeasureFunction(), TermTupleSet.s_MeasureDescendComparitor);
        TermTupleSet<TermTuple> set = inverter.setText("body", "Apple ipod iphone jobs", language);

        String indexName = "fs_store1";
        IndexSearcher s = SearcherCache.getCache().get(indexName);
        if (s == null) {
            assertAlways("Could not find index %s", indexName);
        }
        Analyzer analyzers = AllTypesAnalyzerCache.getCache().get(Iso639Table.english);
        if (analyzers == null) {
            assertAlways("Could not get analyzers");
        }
        QueryParser qp = new QueryParser(IndexerFieldAdapter.DEFAULT_SEARCH_FIELD, analyzers);
        BooleanQuery.Builder q = null;
        try {
            q = new BooleanQuery.Builder();

            //Query version = new TermQuery(new Term("indexversion", Long.toString(m_indexVersion)));
            for (TermTuple tt : set.getTuplesList()) {
                //q.put(version, BooleanClause.Occur.MUST_NOT);
                if (tt.isGood()) {
                    q.add(new TermQuery(new Term("contenttext", tt.getTerm())), BooleanClause.Occur.SHOULD);
                }
            }
            s.rewrite(q.build());
            BaseSession session = null;
            try {
                session = DMSSessionFactory.getFactory().getSession();

                TopDocs td = s.search(q.build(), 100000);
                long totalCount = td.totalHits.value;
                ScoreDoc[] sd = td.scoreDocs;
                for (int i = 0; i < totalCount; i++) {
                    Document doc = s.doc(sd[i].doc);
                    float score = sd[i].score;
                    String guid = doc.get("guid");
                    HTSerializable pts = session.getHTSerializableFromGUID(guid);
                    if (pts instanceof Post) {
                        Post item = (Post) pts;
                        Console.println("%s : %s, text: %s", guid, score, item.getContentText());
                        Console.println();
                        Console.println("============================================================");
                        if (score < 0.31) {
                            break;
                        }
                    } else {
                        Console.println("Not a feed item %s", pts.getClass().getCanonicalName());
                    }
                }
            } finally {
                if (session != null) {
                    DMSSessionFactory.getFactory().rollbackCloseSession(session);
                }
            }

        } catch (IOException e) {
            assertAlways("Could not search index %s, %s %e", indexName, e, e);
        }

    }

    public void testCosine() throws IOException {
        DocumentCollection dc = new DocumentCollection("STANDARD, CASE");
        IsoLanguage lang = Iso639Table.english;
        TermTupleSetGroup g1 = dc.addDocument("1", true, "a", "this 1234 at&t is a. document title", "b", "the quick.  1234 brown fox jumped over the lazy dogs", "body", lang);
        TermTupleSetGroup g2 = dc.addDocument("2", true, "a", "some other title", "b", "dogs are a mans best friend, especially when they jump", "body", lang);
        TermTupleSetGroup g3 = dc.addDocument("3", true, "a", "the good the bad and the ugly", "b", "the mans title president fox is not one of the ugly men according to the document", "body", lang);
        DFIndex index = dc.getDFIndexFrom("all", 100.00);
        TFIDFTermMeasureFunction func = new TFIDFTermMeasureFunction();
        func.setDFIndex(index);

        SparseVector<TermTupleSet> v[] = dc.getSparseVectors("all", func, true);
        int vMax = v.length;
        List<SPResult> list = new ArrayList();
        SPResult sp = new SPResult(v[1], v[2]);
        SPResult sp1 = new SPResult(v[1], v[2]);
        SPResult sp2 = new SPResult(v[1], v[2]);
        for (int i = 0; i < vMax; i++) {
            for (int j = i + 1; j < vMax; j++) {
                list.add(new SPResult(v[i], v[j]));
            }
        }
        Collections.sort(list);
    }

    public void tdfest() throws IOException {
        DocumentCollection dc = new DocumentCollection(null);
        IsoLanguage lang = Iso639Table.english;
        TermTupleSetGroup g1 = dc.addDocument("1", true, "a", "this is a document title", "b", "the quick brown fox jumped over the lazy dogs", "body", lang);
        TermTupleSetGroup g2 = dc.addDocument("2", true, "a", "some other title", "b", "dogs are a mans best friend, especially when they jump", "body", lang);
        TermTupleSetGroup g3 = dc.addDocument("3", true, "a", "the good the bad and the ugly", "b", "the mans title president fox is not one of the ugly men according to the document", "body", lang);
        DFIndex index = dc.getDFIndexFrom("all", 100.00);
        TFIDFTermMeasureFunction func = new TFIDFTermMeasureFunction();
        func.setDFIndex(index);

        dc.computeTermMeasureIfNotAlready("all", func);

        long v[] = index.getHashArray(0, -1, true);
        String terms[] = new String[v.length];
        List<SparseVector> vecs = dc.getSPForTuples("all", v, terms);
        for (int i = 0; i < v.length; i++) {
            Console.println("Term: %s, hash: %s", terms[i], v[i]);
        }

        for (SparseVector sv : vecs) {
            print(sv, v, terms);
        }
        Console.println();
    }

    private void print(SparseVector sv, long v[], String terms[]) {
        int ec = sv.getElementCount();
        Console.println("Vector: %s, count: %s, size %s", sv.getReferrer(), ec, sv.getSize());
        for (int i = 0; i < ec; i++) {
            long pos = sv.getElementPos(i);
            Console.println("pos: %s, term: %s, value: %s", pos, terms[(int) pos], sv.getValue(i));
        }
    }
}

