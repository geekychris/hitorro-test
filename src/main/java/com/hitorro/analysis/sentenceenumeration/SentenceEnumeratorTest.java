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
package com.hitorro.analysis.sentenceenumeration;

import com.hitorro.analysis.sentenceenumeration.BaseSentenceWrapper;
import com.hitorro.analysis.sentenceenumeration.RawSentenceSelectionContext;
import com.hitorro.analysis.sentenceenumeration.SimpleScorer;
import com.hitorro.analysis.sentenceenumeration.TokenizingFPHashBaseMap;
import com.hitorro.analysis.sentenceenumeration.collectingscorer.CollectingPhraseScorer;
import com.hitorro.analysis.sentenceenumeration.collectingscorer.CollectingSentenceWrapper;
import com.hitorro.analysis.sentenceenumeration.collectingscorer.PhraseCollection;
import com.hitorro.analysis.sentenceenumeration.collectingscorer.PhraseHitLogicalLongOper;
import com.hitorro.language.Iso639Table;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.GenericKeyValue;
import com.hitorro.util.core.longword.WordBits;
import com.hitorro.util.core.longword.opers.BitSetOperator;
import com.hitorro.util.core.map.CascadingFPHashBaseMap;
import com.hitorro.util.core.map.FPHashBaseMap;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests for demonstration of two things:
 * 1) enumeration of sentences in a body of text.
 * 2) given a sentence the execution of some kind of score / extraction phase.
 * 3) single and dual layer hash dictionaries used for detecting words or phrases.  These
 * words and phrases can have attributes that describe them.
 * 4) Given 3, provide tools that allow the filtering of words that are "hits" by their
 * dictionary attribute and to detect inclusion of words within other words "new york steak", "new york", "steak"
 */
@HTTest(runlevel = RunLevel.Smoke,
        email = "chris@hitorro.com",
        description = "Test text sentenceenumeration",
        module = "analysis")
public class SentenceEnumeratorTest extends EnhancedTestCase {

    public void testEmotionSummarizer() throws IOException {
        BaseFile bf = this.getInputFileRelativeBaseFile("emotional_text.txt");
        assertFileExistsWithExplanation(bf);
        String text = bf.readString();
        List<GenericKeyValue<String, Integer>> emo = new ArrayList();
        emo.add(new GenericKeyValue<String, Integer>("love", 1));
        emo.add(new GenericKeyValue<String, Integer>("hate", 1));
        emo.add(new GenericKeyValue<String, Integer>("sucked", 1));
        List<GenericKeyValue<String, Integer>> ent = new ArrayList();
        ent.add(new GenericKeyValue<String, Integer>("Courtney Love", 1));
        ent.add(new GenericKeyValue<String, Integer>("apple ipads", 1));
        ent.add(new GenericKeyValue<String, Integer>("ipads", 1));
        ent.add(new GenericKeyValue<String, Integer>("ipods", 1));
        String analyzerString = "htstandard,case,porterstem";

        WordBits wb = new WordBits();
        PhraseHitLogicalLongOper phraseIsEmo =
                new PhraseHitLogicalLongOper(
                        new BitSetOperator(
                                wb.add("emo", 1)));

        TokenizingFPHashBaseMap emotions =
                new TokenizingFPHashBaseMap(analyzerString,
                        Iso639Table.english,
                        wb,
                        emo.size(),
                        0).setPhrases(emo, "emo");

        PhraseHitLogicalLongOper phraseIsEntity =
                new PhraseHitLogicalLongOper(
                        new BitSetOperator(
                                wb.add("ent", 1)));

        // initialize for simple GKV list
        TokenizingFPHashBaseMap entities =
                new TokenizingFPHashBaseMap(analyzerString,
                        Iso639Table.english,
                        wb,
                        ent.size(),
                        0).setPhrases(ent, "ent");

        CascadingFPHashBaseMap cascader =
                new CascadingFPHashBaseMap(
                        new FPHashBaseMap[]{emotions, entities});


        CollectingPhraseScorer bs =
                new CollectingPhraseScorer(
                        emotions.getAnalyzer(),
                        wb,
                        cascader,
                        100);

        RawSentenceSelectionContext ssc =
                new RawSentenceSelectionContext().
                        set(text,
                                null,
                                Iso639Table.english,
                                bs);
        if (ssc.parse()) {
            List<CollectingSentenceWrapper> l = ssc.getSentences();
            for (CollectingSentenceWrapper sw : l) {
                PhraseCollection pc = sw.getPhraseCollection();
                int index = pc.indeOf(0, phraseIsEmo);
                if (index != -1) {
                    Console.println("Emotion of %s", pc.getPhraseHitCopy(index));
                }


                int indexEntity = pc.indeOf(0, phraseIsEntity);
                if (indexEntity != -1) {
                    Console.println("Entity of %s", pc.getPhraseHitCopy(indexEntity));
                }

                pc.hasOverlapping();
                Console.println("%s %s", sw.getScore(), sw.getText());
            }
        }
    }

    public void testSummarizer() throws IOException {
        BaseFile bf = this.getInputFileRelativeBaseFile("hack.txt");
        assertFileExistsWithExplanation(bf);
        String text = bf.readString();
        List<GenericKeyValue<String, Integer>> list = new ArrayList();
        list.add(new GenericKeyValue<String, Integer>("Closed captioning", 5));
        list.add(new GenericKeyValue<String, Integer>("President Obama", 10));
        list.add(new GenericKeyValue<String, Integer>("Richardson", 8));

        SimpleScorer bs = new SimpleScorer("htstandard,PORTERSTEM", Iso639Table.english, list);
        RawSentenceSelectionContext ssc = new RawSentenceSelectionContext();
        ssc.set(text, null, Iso639Table.english, bs);
        if (ssc.parse()) {
            List<BaseSentenceWrapper> l = ssc.getSentences();
            for (BaseSentenceWrapper sw : l) {
                Console.println("%s %s", sw.getScore(), sw.getText());
            }
        }
    }

}
