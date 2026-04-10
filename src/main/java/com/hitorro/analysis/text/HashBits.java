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
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.fs.file.FileFileSystem;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.longword.NamedBitsOfLong;
import com.hitorro.util.core.longword.WordBits;
import com.hitorro.util.core.map.FPHashLongMap;

import java.io.IOException;
import java.text.ParseException;


public class HashBits {

    WordBits bits;
    NamedBitsOfLong nbol;
    FPHashLongMap fpHashLongMap;
    FPHashLongMap fpPOSHashLongMap;
    NamedBitsOfLong adv;
    NamedBitsOfLong noun;
    NamedBitsOfLong verb;
    NamedBitsOfLong adj;
    WordBits posbits;

    public String initPass(final JsonNode map, BaseFile bOut) {
        bits = new WordBits();
        bits.add("iswikientry", 1);
        nbol = bits.get("iswikientry");
        bits.add("emotion", 4);
        nbol = bits.get("emotion");
        try {
            fpHashLongMap = FPHashLongMap.loadFromDirectory(bits, bOut, 10, true, true);
        } catch (IOException e) {
            return e.toString();
        } catch (ParseException e) {
            return e.toString();
        }
        return null;
    }

    public String getVal(long l) {
        StringBuilder sb = new StringBuilder();
        if (adv.isSet(l)) {
            Console.bprint(sb, "adv,");
        }
        if (noun.isSet(l)) {
            Console.bprint(sb, "noun,");
        }
        if (adj.isSet(l)) {
            Console.bprint(sb, "adj,");
        }
        if (verb.isSet(l)) {
            Console.bprint(sb, "verb,");
        }
        return sb.toString();
    }

    public String initPOSPass(final JsonNode map) {
        posbits = new WordBits();
        posbits.add("adv", 1);
        adv = posbits.get("adv");
        posbits.add("noun", 1);
        noun = posbits.get("noun");
        posbits.add("verb", 1);
        verb = posbits.get("verb");
        posbits.add("adj", 1);
        adj = posbits.get("adj");
        BaseFile bOut = FileFileSystem.Root.getFile("/ht/hitorro/data/wordnet");
        try {
            fpPOSHashLongMap = FPHashLongMap.loadFromDirectory(posbits, bOut, 10, true, true);
        } catch (IOException e) {
            return e.toString();
        } catch (ParseException e) {
            return e.toString();
        }
        return null;
    }
}
