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
package com.hitorro.test;

import com.hitorro.base.BaseBaseFileUtil;
import com.hitorro.basedms.session.DMSSession;
import com.hitorro.test.dms.NonPersistObject;
import com.hitorro.util.basefile.filters.FileExtension;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.iterator.LikeRowMerger;
import com.hitorro.util.core.iterator.mappers.DummyBaseMapper;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.io.largedata.BaseFileAccessingObjectFactory;
import com.hitorro.util.io.largedata.TakeRightRowMerger;
import com.hitorro.util.io.largedata.buckets.HTSerializableFileBucketWriter;
import com.hitorro.util.io.largedata.iterator.HTSelectTreeController;
import com.hitorro.util.io.largedata.iterator.HTSerializableInputIterator;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;
import com.hitorro.util.typesystem.HTSerializable;
import com.hitorro.util.typesystem.HTSerializableUtil;
import com.hitorro.util.typesystem.TypeManager;

import java.util.Comparator;
import java.util.Date;

/**
 */
@HTTest(runlevel = RunLevel.Smoke,
        email = "chris@hitorro.com",
        description = "Test select trees and bucketizer logic.")
public class SelectTreeTest extends EnhancedTestCase {
    public static TakeRightRowMerger<NonPersistObject> RightMerger = new TakeRightRowMerger();

    public static BaseFileAccessingObjectFactory getFactory() {

        BaseFileAccessingObjectFactory fact =
                new BaseFileAccessingObjectFactory();
        fact.setComparitor(new NPOComp())
                .setNewArrayClass(NonPersistObject.class)
                .setBaseFileToChainingMapper(BaseBaseFileUtil.bf2htser.combine(new DummyBaseMapper()))
                .setBaseFileSinkMapper(HTSerializableUtil.bf2htser.combine(new DummyBaseMapper()))
                .setNewArrayClass(HTSerializable.class)
                .setNewFunction(SelectTreeTest::getNewValue);
        return fact;
    }

    public static NonPersistObject getNewValue(Object o) {
        return new NonPersistObject();
    }

    public void testBucketizerAndSelectTreesWithMerger() throws Exception {
        int iters = 100000;
        BaseFile dir = Env.getBaseFile(this.getOutputFileRelative("/bucketizer/"));
        dir.deleteContentOfDir(true);
        dir.mkdir();
        TypeManager.getTypeManager().addTypeIfAbsent(NonPersistObject.class);
        String fileExtension = "ser";
        HTSerializableFileBucketWriter<NonPersistObject> writer =
                new HTSerializableFileBucketWriter<NonPersistObject>(10000, dir, fileExtension, getFactory());
        for (int i = iters; i > 0; i--) {
            NonPersistObject npo = new NonPersistObject();
            npo.int_ = i;
            npo.m_date = new Date();
            npo.m_string = Fmt.S("Number: %s", i);

            if (i % 102 == 0) {
                // lets put some dupes in.
                NonPersistObject npo2 = new NonPersistObject();
                npo2.int_ = i + 55;
                npo2.m_date = new Date();
                writer.add(npo2);
            }
            writer.add(npo);
        }

        writer.close();

        TypeManager.getTypeManager().addTypeIfAbsent(NonPersistObject.class);


        BaseFile files[] = dir.listFiles(FileExtension.ser);
        HTSerializableInputIterator[] ptsArray = new HTSerializableInputIterator[files.length];

        HTSelectTreeController stc = new HTSelectTreeController(dir, files, 10, getFactory(), true, fileExtension, true);
        BaseFile f = stc.merge();
        // now ensure that the contents only has incrementing id's and no dupes.
        HTSerializableInputIterator<NonPersistObject, DMSSession> iter = new HTSerializableInputIterator<NonPersistObject, DMSSession>(f, null);
        int count = 0;

        NonPersistObject npo = iter.next();
        assertNotNull(npo);
        int prev = npo.int_;
        while (iter.hasNext()) {

            npo = iter.next();
            assertNotNull(npo);
            count++;
            int curI = npo.int_;
            if (curI <= prev) {
                assertAlways("expected next value to be greater than previous");
            }
            prev = curI;
        }
        // ensure we have the right amount of rows....we slightly more based upon the extra NPO's we sneak in for dupe
        // creation.
        assertEquals(iters, count);
    }
}


class NPOComp implements Comparator<NonPersistObject> {

    public int compare(NonPersistObject nonPersistObject, NonPersistObject nonPersistObject1) {
        return nonPersistObject.int_ - nonPersistObject1.int_;
    }
}


class NPOMerger implements LikeRowMerger<NonPersistObject> {
    public NonPersistObject apply(NonPersistObject old, NonPersistObject newval) {
        newval.m_string = StringUtil.strcat(old.m_string, newval.m_string);
        return newval;
    }
}
