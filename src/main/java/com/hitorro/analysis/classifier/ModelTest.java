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
package com.hitorro.analysis.classifier;

import com.hitorro.analysis.linkengine.Log;
import com.hitorro.base.objects.Post;
import com.hitorro.basedms.session.DMSSessionFactory;
import com.hitorro.basetext.PostUtil;
import com.hitorro.basetext.classifier.Loader;
import com.hitorro.basetext.classifier.ModelBuilderLoaderCallback;
import com.hitorro.basetext.classifier.Parts;
import com.hitorro.basetext.inverter.InverterUtils;
import com.hitorro.basetext.inverter.TFIDFTermMeasureFunction;
import com.hitorro.basetext.inverter.TermTupleSet;
import com.hitorro.basetext.inverter.TermTupleSetGroup;
import com.hitorro.conversationtracker.Conversation;
import com.hitorro.language.Iso639Table;
import com.hitorro.language.IsoLanguage;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.GenericKeyValue;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;
import com.hitorro.util.typesystem.BaseSession;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Test a simple text distance measure based "rote" machine learnt isQuestionClassifier.
 */
@HTTest(runlevel = RunLevel.Never,
        email = "chris@hitorro.com",
        description = "Test of catagorization model based upon simple text distance measure")
public class ModelTest extends EnhancedTestCase {
    public void test() {
        Loader loader = new Loader();
        ModelBuilderLoaderCallback callback = new ModelBuilderLoaderCallback();
        File dir = new File(Env.getHome(), "training");
        loader.load(dir, callback);
        callback.computeModelDocVectors();
        score("http://www.amiga.com", callback);
        score("http://www.ariba.com", callback);
        score("http://www.cnn.com", callback);
        score("http://www.abc.com", callback);
        score("http://www.tomsawyer.com", callback);
        score("http://www.scobleizer.com", callback);
        score("http://maryamie.spaces.live.com/", callback);
    }

    private void score(String url, ModelBuilderLoaderCallback callback) {
        TermTupleSetGroup tsg = InverterUtils.getTupleSetGroupFromUrl(url, 1000, "body", Iso639Table.english);
        if (tsg == null) {
            return;
        }
        //tsg.apply("title", "body", "all");
        TermTupleSet tts = tsg.getByName("all");

        List<Parts> list = callback.rank(tts, true, 30);
        if (list != null) {
            Console.println("Scores for %s", url);
            for (Parts e : list) {
                Console.println("Voted: %s, %s", e.m_cat, e.m_accum);
            }
        }
    }

    public void tstchris2() {
        // 20, (float) 0.3, 5);
        BaseSession session = DMSSessionFactory.getFactory().getSession();
        Iterator<Post> iter = session.getIteratorFromQuery("from Post");
        while (iter.hasNext()) {
            Post post = iter.next();
            List<GenericKeyValue<String, Double>> list = PostUtil.getSimilarDocuments(post, 20, (float) 0.3, 5);
            for (GenericKeyValue<String, Double> val : list) {
                Console.println("%s %s", val.getKey(), val.getValue());
            }


            Console.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        }

    }

    public void tstchris() {
        BaseSession session = DMSSessionFactory.getFactory().getSession();
        Iterator<Conversation> iter = session.getIteratorFromQuery("from Conversation");
        while (iter.hasNext()) {
            Conversation conv = iter.next();
            List<GenericKeyValue<String, Double>> list = conv.getPostSimilarityRank(session);
            for (GenericKeyValue<String, Double> val : list) {
                Console.println("%s %s", val.getKey(), val.getValue());
            }
            Console.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        }

    }

    private TermTupleSetGroup getGroup(Post post) {

        IsoLanguage language = Iso639Table.english;
        TermTupleSetGroup group = new TermTupleSetGroup("body", null, TermTupleSet.s_MeasureDescendComparitor, new TFIDFTermMeasureFunction());
        group.setId(post.getGuid());
        try {
            group.add("title", post.getTitle(), language);
            group.add("body", post.getBodyText(), language);
        } catch (IOException e) {
            Log.util.error("%s %e", e, e);
        }
        return group;
    }
}
