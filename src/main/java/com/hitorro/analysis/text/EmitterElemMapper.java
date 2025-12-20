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

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.core.iterator.Mapper;
import com.hitorro.util.json.keys.IntegerProperty;
import com.hitorro.util.json.keys.LongProperty;
import com.hitorro.util.json.keys.StringProperty;

/**
 *
 */
public class EmitterElemMapper implements Mapper<JsonNode, EmitterElem> {
    public static final LongProperty FpKey = new LongProperty("fp", "", 0l);
    public static final IntegerProperty StartKey = new IntegerProperty("startPos", "", 0);
    public static final IntegerProperty SizeKey = new IntegerProperty("size", "", 0);
    public static final StringProperty TxtKey = new StringProperty("text", "", null);
    public static final LongProperty ValKey = new LongProperty("val", "", 0l);
    public static EmitterElemMapper mapper = new EmitterElemMapper();

    @Override
    public EmitterElem apply(JsonNode e) {
        EmitterElem elem = new EmitterElem(FpKey.apply(e),
                StartKey.apply(e),
                SizeKey.apply(e),
                TxtKey.apply(e),
                ValKey.apply(e));
        return elem;
    }
}
