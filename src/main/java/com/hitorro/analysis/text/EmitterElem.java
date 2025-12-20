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

import com.hitorro.util.core.string.Fmt;

/**
 *
 */
public class EmitterElem {
    public static final String csvcols[] = {"fp", "startPos", "size", "text", "val"};

    long fp;
    int startPos;
    int size;
    String text;
    long val;

    private int hash;

    public EmitterElem(long fingerPrint, int startPosition, int size, String text, long val) {
        this.fp = fingerPrint;
        this.startPos = startPosition;
        this.size = size;
        this.text = text;
        this.val = val;

        this.hash = hashCodeAux();
    }

    public String toString() {
        return Fmt.S("text=%s hash=%s size=%s startPos=%s val=%s", text, fp, size, startPos, val);
    }

    public boolean equals(Object o) {
        if (o instanceof EmitterElem) {
            EmitterElem e = (EmitterElem) o;
            return (fp == e.fp &&
                    startPos == e.startPos &&
                    size == e.size &&
                    text.equals(e.text) &&
                    val == e.val);
        }
        return false;
    }

    public int hashCode() {
        return hash;
    }

    private int hashCodeAux() {
        int h = startPos;
        h ^= size;
        h ^= text.hashCode();
        h ^= (int) (val ^ val >>> 32);
        h ^= (int) (fp ^ fp >>> 32);
        return h;
    }

}