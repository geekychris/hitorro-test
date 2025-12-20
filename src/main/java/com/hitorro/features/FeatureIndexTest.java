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

import com.hitorro.features.collections.BaseCollection;
import com.hitorro.features.collections.FeatureCollectionContext;
import com.hitorro.features.index.FeatureCacheManager;
import com.hitorro.features.index.FeatureIndexManager;
import com.hitorro.features.index.FeatureIndexer;
import com.hitorro.features.index.FeatureQueryContext;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.Env;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;
import com.hitorro.util.typesystem.HTSerializableUtil;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import java.io.File;
import java.io.IOException;


/**
 * ht.features.FeatureIndexTest
 */
@HTTest(runlevel = RunLevel.Smoke,
        email = "chris@hitorro.com",
        description = "Exercise the feature indexer")

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FeatureIndexTest extends EnhancedTestCase {
    private FeatureValue grind(FeatureValue fv) {
        byte b[] = HTSerializableUtil.getHTSerializableBuffer(fv);
        return (FeatureValue) HTSerializableUtil.readHTSerializableFromBuffer(b, null);
    }

    public void testGeneration() throws Exception {
        File file = generatedSortedSampleFeatures(10000);
    }

    private File generatedSortedSampleFeatures(int iterations) throws Exception {
        BaseFile tempDir = Env.getBaseFile(this.getOutputFileRelative("tmpdir"));
        tempDir.deleteAndRecreateDir(true);

        BaseFile temp = Env.getBaseFile(this.getOutputFileRelative("temp.ser"));

        Feature intSingle = FeatureEngine.getEngine().getFeature("testintsingle");
        Feature floatSingle = FeatureEngine.getEngine().getFeature("testfloatsingle");


        Feature floatSingleDerived = FeatureEngine.getEngine().getFeature("testfloatsinglederived");

        FeatureValue fv = FeatureValue.getFeature(floatSingle, 1234, 1111, new Integer(4));
        FeatureValue out = this.grind(fv);
        FeatureIndexer linearizer = new FeatureIndexer(10000, 5 * 60, tempDir, 8);

        linearizer.start();

        for (int i = 0; i < 4; i++) {
            int offset = i * iterations;
            if (generateSampleFeatures(offset, iterations, linearizer, intSingle, floatSingle)) {
                linearizer.flushBucket();
                // in runnable mode no need to use flushbucket or process
                linearizer.process();
            }
        }
        FeatureCacheManager fcm = new FeatureCacheManager(tempDir);

        FeatureIndexManager manager = fcm.getFIM();
        FeatureQueryContext fqc = fcm.getQueryContext();


        FeatureSetKey fsk = FeatureSetKey.getFeatureAux(floatSingleDerived, RealmId.None, -1);

        FeatureSetKey fskRo = FeatureSetKey.getFeatureAux(floatSingle, RealmId.None, -1);

        manager.validateAllReaders(fskRo);
        BaseCollection bc = fqc.getCollection(fsk);
        FeatureCollectionContext fcc = new FeatureCollectionContext();

        assertNotNull(bc);
        long keys[] = floatSingleDerived.getKeyType().getKeyBag(null, 6);
        float f = bc.score(-1, keys, fcc);
        return null;
    }

    public void testMerge() throws IOException {
        BaseFile tempDir = Env.getBaseFile(this.getOutputFileRelative("tmpdir"));
        FeatureCacheManager fcm = new FeatureCacheManager(tempDir);

        FeatureIndexManager manager = fcm.getFIM();
        manager.merge();
        Feature intSingle = FeatureEngine.getEngine().getFeature("testintsingle");
        FeatureSetKey fsk = FeatureSetKey.getFeatureAux(intSingle, RealmId.None, -1);
        FeatureQueryContext fqc = fcm.getQueryContext();
        BaseCollection bc = fqc.getCollection(fsk);
        BaseCollection bc2 = fqc.getCollection(fsk);

    }


    private boolean generateSampleFeatures(int offset, int iterations, FeatureIndexer linearizer, Feature intSingle, Feature floatSingle) throws IOException {
        for (int i = iterations; i > 0; i--) {
            int id = i + offset;
            FeatureValue intFeatureValue = FeatureValue.getFeature(intSingle, id, 0, id % 125);
            assertNotNull(intFeatureValue);
            linearizer.add(intFeatureValue);

            FeatureValue floatFeatureValue = FeatureValue.getFeature(floatSingle, id, 0, ((float) id % 100) / 123);
            assertNotNull(floatFeatureValue);
            linearizer.add(floatFeatureValue);
        }
        return true;
    }
}

