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
package com.hitorro.features;

import com.hitorro.basetext.indexer.IndexerUtil;
import com.hitorro.features.extractor.ExtractionContext;
import com.hitorro.features.extractor.ExtractionSet;
import com.hitorro.features.extractor.ExtractorManager;
import com.hitorro.features.extractor.Results;
import com.hitorro.util.bagio.XMLBagLoader;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.List;


/**
 * ht.features.FeatureExtractorTest Exercises the extractor process, ensures that fields are also mapped as
 * "id" is a mapped field.
 */
@HTTest(runlevel = RunLevel.Smoke,
        email = "chris@hitorro.com",
        description = "Ensure that the feature extraction engine extracts features")
public class FeatureExtractorTest extends EnhancedTestCase {
    public void testJap() throws IOException {
        IndexerUtil.test("15_10月度(7月20日締め)直取り商品明細（並行 交渉)");

        IndexerUtil.test("直取");

        IndexerUtil.test("直取り");

        IndexerUtil.test("15_11月度(8月20日締め)直取り商品明細（並行 交渉)");
    }

    public void testLoad() throws IOException, ParserConfigurationException, SAXException {
        File bagsFile = this.getInputFileRelative("bags.xml");
        this.assertFileExistsWithExplanation(bagsFile);
        XMLBagLoader loader = new XMLBagLoader();
        List<com.hitorro.util.typesystem.Bag> bags = loader.getBags(bagsFile, false);
        assertEquals(bags.size(), 3);
    }

    public void testoverideOfFTOnExistingType() {
        com.hitorro.util.typesystem.Type t = (com.hitorro.util.typesystem.Type) com.hitorro.util.typesystem.TypeManager.getTypeManager().getTypeByShortName("93");
        com.hitorro.util.typesystem.TypeFieldIntf tf = t.getField("publisheddate");
        assertNotNull(tf);
        // FT meta was added by the overide
        assertNotNull(tf.getFullTextMeta());
    }

    public void testOverideOfBag() {
        com.hitorro.util.typesystem.Type t2 = (com.hitorro.util.typesystem.Type) com.hitorro.util.typesystem.TypeManager.getTypeManager().getTypeByShortName("extendedwebpage");
        // from super type
        assertNotNull(t2.getField("url"));
        assertNotNull(t2.getField("extendedbody"));

    }

    public void test() {
        com.hitorro.util.typesystem.Bag b = com.hitorro.util.typesystem.Bag.getBagForType("webpage");
        assertNotNull(b);
        ExtractorManager em = FeatureEngine.getEngine().getExtractorManager();
        assertNotNull(em);
        com.hitorro.util.typesystem.ValueSourceBaseFields bf = new com.hitorro.util.typesystem.ValueSourceBaseFields();
        ExtractionSet es = em.getExtractionSet("a", bf);
        assertNotNull(es);
        assertTrue(es.size() != 0);

        b.setValue(b, "url", new String("http://www.cnn.com/foo/bar/default.txt"));
        b.setValue(b, "created", System.currentTimeMillis());
        b.setValue(b, "title", new String("title of this epic yarn"));
        b.setValue(b, "body", new String("this is the body text of this baggy poohs"));
        Results res = new Results();
        ExtractionContext ec = es.getExtractionContext();
        int count = es.extract(b, res, ec);
        count = es.extract(b, res, ec);
        FeatureValue fv = res.getAllValues().get(0);
        assertNotNull(fv);
        Number n = fv.getNumber();
        assertNotNull(n);
        assertTrue(fv.getID() != 0);
    }
}
