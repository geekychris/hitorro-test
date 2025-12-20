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
package com.hitorro.analysis.text;

import com.hitorro.basedms.Constants;
import com.hitorro.basetext.dfindex.FPHashDict;
import com.hitorro.jsontypesystem.JVS;
import com.hitorro.language.Iso639Table;
import com.hitorro.obj.core.FilterContext;
import com.hitorro.obj.core.GenericAnalyzer;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.tools.BaseFileUtil;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.CollectionIterator;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.io.ResetableStringReader;
import com.hitorro.util.io.csv.CSV2MapIterator;
import com.hitorro.util.io.csv.CSVIteratorImpl;
import com.hitorro.util.io.csv.CSVSink;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@HTTest(runlevel = RunLevel.Never,
        email = "chris@hitorro.com",
        description = "Lucene analyzer tests")
public class TextAnalysisTests extends EnhancedTestCase {

    /**
     * Verify that the htstandard analyzer keeps special tags such as #tag or cost+ together.  Further verifying that
     * the solr common grams marries some stop words with its neighbors.
     *
     * @throws IOException
     */
    public void testCommonStopHtstandard() throws IOException {
        GenericAnalyzer analyzer = new GenericAnalyzer("HTSTANDARD,CASE,COMMONGRAM,STOP", Iso639Table.english, GenericAnalyzer.Mode.Index);
        File t = this.getInputFileRelative("testCommonStopHtstandard.txt");
        List<String> tList = FileUtil.getLinesFromFile(t);
        ResetableStringReader reader = new ResetableStringReader(null);
        String text = "@worryknot Yep, without listening. 11.5 12 http://hello.com porno.net #mytag also+ love that one, One of my favs? wtf";
        reader.set(text);
        TokenStream ts = analyzer.tokenStream("body", reader);
        CharTermAttribute termAttLocal = ts.addAttribute(CharTermAttribute.class);

        int pos = 0;
        while (ts.incrementToken()) {
            String tok = termAttLocal.toString();
            this.assertEqualsIgnoreCase(tok, tList.get(pos++));
        }
        ts.close();
    }

    /**
     * Identify phrases against a dictionary of terms that have associated them word bits that can be used to classify
     * dictionary terms under different categories of words (eg emotional or parts of speech).
     * <p/>
     * Leverages mappers to read and write csv representations of a hit row.
     *
     * @throws IOException
     */
    public void testPhraseEmitter() throws IOException {
        JVS jvs = new JVS();
        HashBits hb = new HashBits();
        BaseFile hashDir = this.getOutputDirViaInputDirCopyBaseFile("smallphrase");
        hb.initPass(jvs.getJsonNode(), hashDir);
        FPHashDict dict = new FPHashDict(hb.fpHashLongMap, 0);
        TestEmitter context = new TestEmitter(4, dict, true);
        FilterContext fc = new FilterContext();
        fc.fpPhraseContext = context;
        fc.baseIndex = dict;


        BaseFile bfInf = this.getInputFileRelativeBaseFile("testPhraseEmitter.txt");
        this.assertFileExistsWithExplanation(bfInf);
        List<EmitterElem> el = getEmmitters(bfInf);


        GenericAnalyzer ga = new GenericAnalyzer("htstandard,emitphrase", Iso639Table.english, GenericAnalyzer.Mode.Index);

        StringReader reader = new StringReader("the quick brown fox jumped over the lazy dogs and had some amiga love");
        TokenStream ts = ga.tokenStream("body", reader);
        CharTermAttribute termAttLocal = ts.addAttribute(CharTermAttribute.class);
        while (ts.incrementToken()) {

        }
        ts.close();
        assertEquals(el.size(), context.list.size());
        for (int i = 0; i < el.size(); i++) {
            EmitterElem eeP = el.get(i);
            EmitterElem eeNew = el.get(i);
            assertEquals(eeP, eeNew);

        }
        /*BaseFile bf = this.getOutputFileRelativeBaseFile("testPhraseEmitter");
        writeToFile(compContext.listFiles, bf);
        */
    }


    /**
     * Emits phrases
     *
     * @throws IOException
     */
    public void testPhrases() throws IOException {
        JVS map = new JVS();
        HashBits hb = new HashBits();
        BaseFile hashDir = this.getOutputDirViaInputDirCopyBaseFile("smallphrase");
        hb.initPass(map.getJsonNode(), hashDir);
        FPHashDict dict = new FPHashDict(hb.fpHashLongMap, 0);
        TestEmitter context = new TestEmitter(4, dict, true);
        FilterContext fc = new FilterContext();
        fc.fpPhraseContext = context;
        fc.baseIndex = dict;

        // get the iterator of words to test against.
        BaseFile bfInf = this.getInputFileRelativeBaseFile("testPhrases.txt");
        this.assertFileExistsWithExplanation(bfInf);
        Iterator<String> sIter = BaseFileUtil.bf2lineiter.apply(bfInf);


        GenericAnalyzer ga = new GenericAnalyzer("htstandard,phrase", Iso639Table.english, GenericAnalyzer.Mode.Index);

        StringReader reader = new StringReader("the quick brown fox jumped over the lazy dogs and had some amiga love");
        TokenStream ts = ga.tokenStream("body", reader);
        CharTermAttribute termAttLocal = ts.addAttribute(CharTermAttribute.class);
        boolean tsHasNext = ts.incrementToken();
        boolean sHasNext = sIter.hasNext();
        assertEquals(tsHasNext, sHasNext);
        while (tsHasNext && sHasNext) {
            String tok = termAttLocal.toString();
            String compTok = sIter.next();
            assertEquals(tok, compTok);

            tsHasNext = ts.incrementToken();
            sHasNext = sIter.hasNext();
            assertEquals(tsHasNext, sHasNext);
        }
        ts.close();
        Console.println();
    }


    private List<EmitterElem> getEmmitters(BaseFile file) throws IOException {
        List<EmitterElem> list = new ArrayList();
        CSVIteratorImpl csvIter = new CSVIteratorImpl(file, Constants.UTF8);
        CSV2MapIterator csv2mapIter = new CSV2MapIterator(csvIter);
        AbstractIterator<EmitterElem> iter = csv2mapIter.map(EmitterElemMapper.mapper);
        while (iter.hasNext()) {
            list.add(iter.next());
        }
        return list;
    }

    /**
     * Utility to take the initial set of EmitterElem's and turn them into a csv
     *
     * @param elems
     * @param file
     * @throws IOException
     */
    private void writeToFile(List<EmitterElem> elems, BaseFile file) throws IOException {
        CollectionIterator<EmitterElem> ai = new CollectionIterator<EmitterElem>(elems);
        OutputStream os = file.getDataOutputStream();
        CSVSink csvSink = new CSVSink(EmitterElem.csvcols, os, Constants.UTF8, ',');
        int count = ai.map(Emmitter2CSV.e2csv).sink(csvSink);
    }


}


