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
package com.hitorro.util.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.features.Feature;
import com.hitorro.util.json.keys.propaccess.PropaccessError;
import com.hitorro.util.propertykeys.complex.ComplexPropertiesException;
import com.hitorro.util.propertykeys.complex.ComplexPropertyContext;
import com.hitorro.util.propertykeys.complex.ComplexPropertyFactoryInterface;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;

import java.util.List;

@HTTest(runlevel = RunLevel.Smoke,
        email = "chris@hitorro.com",
        description = "")
public class ComplexPropsTest extends EnhancedTestCase {
    public void test() throws ComplexPropertiesException, PropaccessError {
        ComplexPropertyContext.add(new FeaturePropertyFactory());
        Feature f = ComplexPropertyContext.get("complex.a", "feature");
        List<Feature> features = ComplexPropertyContext.getList("complex", "feature");
        assertEquals(features.size(), 2);
    }
}

class FeaturePropertyFactory implements ComplexPropertyFactoryInterface<Feature> {
    public String[] getNames() {
        return new String[]{"feature"};
    }

    public Feature getInstance(final JsonNode map, final String type, final String parentPathName) {
        return new Feature();
    }
}
