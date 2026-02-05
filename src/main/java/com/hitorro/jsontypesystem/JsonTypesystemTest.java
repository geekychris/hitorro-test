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
package com.hitorro.jsontypesystem;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.jsontypesystem.dynamic.DynamicMapper;
import com.hitorro.language.HTJWNLDictionary;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.Env;
import com.hitorro.util.json.JsonInitable;
import com.hitorro.util.json.keys.BooleanProperty;
import com.hitorro.util.json.keys.IntegerProperty;
import com.hitorro.util.json.keys.PropaccessProperty;
import com.hitorro.util.json.keys.StringProperty;
import com.hitorro.util.json.keys.propaccess.Propaccess;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;

import java.io.File;
import java.util.Date;
import java.util.List;

@HTTest(runlevel = RunLevel.Smoke,
        email = "chris@hitorro.com",
        description = "Json Type System Test")
public class JsonTypesystemTest extends EnhancedTestCase {

    /**
     * XXX TODO make this a proper test!!!!
     */
    public  void test() {
        try {

            HTJWNLDictionary dict = HTJWNLDictionary.me.get();


            BaseFile bf = Env.getBinBaseFileSystem().getFile("data/typeexamples/sysobject_example.json");
            if (bf.exists()) {
                PropaccessProperty propKey = new PropaccessProperty("", "", null);
                DynamicMapper dm = new DynamicMapper();
                JsonNode n = bf.getJsonNode();
                com.hitorro.jsontypesystem.JVS jvs = new com.hitorro.jsontypesystem.JVS(n);
                com.hitorro.jsontypesystem.Type t = jvs.getType();

                String text = jvs.getString("title.mls[en].text");
                jvs.set("title.mls[2].segmented", "hello");
                Date created = jvs.getDate("dates.created");

                String id = jvs.getString("id.id");
                String text2 = jvs.getString("title.mls[en].text");
                String clean = jvs.getString("title.mls[en].clean");
                List<String> segmented = jvs.getStringList("title.mls[en].segmented");
                List<String> segmentedGerman = jvs.getStringList("title.mls[de].segmented");
                JsonNode segmented_parsed = jvs.get("title.mls[en].segmented_parsed");
                JsonNode segmented_parsed_german = jvs.get("title.mls[de].segmented_parsed");
                List<String> classes = jvs.getStringList("title.mls[en].segmented_answers");
                List<String> ner = jvs.getStringList("title.mls[en].segmented_ner");
                long[] normHash = jvs.getLongArray("title.mls[en].segmented_normhash");
                long idHash = jvs.getLong("id.id_hash");
                Console.println();
            }

            com.hitorro.jsontypesystem.Type t2 = com.hitorro.jsontypesystem.JsonTypeSystem.getMe().getType("sysobject");
            Propaccess pa = new Propaccess("dates.created");
            com.hitorro.jsontypesystem.Field f1 = t2.getField(pa);
            com.hitorro.jsontypesystem.Field f2 = t2.getField(pa, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}