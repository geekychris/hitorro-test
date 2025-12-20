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

import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;
import com.hitorro.util.typesystem.HTSerializableUtil;

/**
 * Feature Value serialization tests
 */
@HTTest(runlevel = RunLevel.Smoke,
        email = "chris@hitorro.com",
        description = "Feature serialization tests")
public class FeatureSerializationTest extends EnhancedTestCase {
    public void testTypes() {
        long id = 310687;
        long time = 1234567;
        int partition = 199;

        doSimpleReadWrite("testintsingle", DataType.integerType, Integer.MAX_VALUE - 1, id, time, partition);
        doSimpleReadWrite("testlongsingle", DataType.longType, Long.MAX_VALUE - 1, id, time, partition);

        doSimpleReadWrite("testbytesingle", DataType.byteType, (byte) (Byte.MAX_VALUE - 1), id, time, partition);
        doSimpleReadWrite("testshortsingle", DataType.shortType, (short) (Short.MAX_VALUE - 1), id, time, partition);
        doSimpleReadWrite("testfloatsingle", DataType.floatType, Float.MAX_VALUE - 1, id, time, partition);
        doSimpleReadWrite("testdoublesingle", DataType.doubleType, Double.MAX_VALUE - 1, id, time, partition);

        // index test
        doSimpleReadWrite("testlongindex", DataType.longType, Long.MAX_VALUE - 1, id, time, partition, 126);

        // variable test
        doSimpleReadWrite("testlongvariable", DataType.longType, Long.MAX_VALUE - 1, id, time, partition, "foo");

    }

    public FeatureValue doSimpleReadWrite(String name, DataType dt, Number value, long id, long time, int partition) {
        Feature feat = getFeature(name, dt);
        FeatureValue fv = FeatureValue.getFeature(feat, id, time, value);
        fv.setPartition(partition);
        fv = grind(fv);
        ensureBasicValuesCorrect(fv, id, time, feat, partition, dt.getClassType(), value);
        return fv;
    }

    public FeatureValue doSimpleReadWrite(String name, DataType dt, Number value, long id, long time, int partition, int index) {
        Feature feat = getFeature(name, dt);
        FeatureValue fv = FeatureValue.getFeature(feat, id, time, value);
        fv.setPartition(partition);
        fv.setIndex(index);
        fv = grind(fv);
        ensureBasicValuesCorrect(fv, id, time, feat, partition, dt.getClassType(), value);
        assertEquals(index, fv.getIndex());
        return fv;
    }

    public FeatureValue doSimpleReadWrite(String name, DataType dt, Number value, long id, long time, int partition, String var) {
        Feature feat = getFeature(name, dt);
        FeatureValue fv = FeatureValue.getFeature(feat, id, time, value);
        fv.setPartition(partition);
        fv.setVariable(var);
        fv = grind(fv);
        ensureBasicValuesCorrect(fv, id, time, feat, partition, dt.getClassType(), value);
        assertEquals(var, fv.getVariable());
        return fv;
    }


    public void ensureBasicValuesCorrect(FeatureValue fv, long id, long time,
                                         Feature feat, int partition, Class c, Number n) {
        assertEquals(fv.getFeature(), feat);
        assertEquals(fv.getID(), id);
        assertEquals(fv.getTime(), time);
        assertEquals(fv.getParition(), partition);
        assertSameType(fv.getNumber(), c);
        Number nNew = fv.getNumber();
        assertEquals(fv.getNumber(), n);
    }

    private Feature getFeature(String name, DataType expectedDTType) {
        Feature f = FeatureEngine.getEngine().getFeature(name);
        assertNotNull(f);
        assertEquals(f.getDataType(), expectedDTType);
        return f;
    }

    private FeatureValue grind(FeatureValue fv) {
        byte b[] = HTSerializableUtil.getHTSerializableBuffer(fv);
        return (FeatureValue) HTSerializableUtil.readHTSerializableFromBuffer(b, null);
    }

}
