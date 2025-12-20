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

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.jsontypesystem.JVS;
import com.hitorro.jsontypesystem.predicates.ForEach;
import com.hitorro.jsontypesystem.predicates.JVS2JsonPredicate;
import com.hitorro.jsontypesystem.predicates.PathMatch;
import com.hitorro.network.rpc.cluster.ClusterResults;
import com.hitorro.network.rpc.cluster.IDef;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.collection.BaseCollection;
import com.hitorro.util.core.opers.HTPredicate;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;


@HTTest(runlevel = RunLevel.Smoke,
        email = "chris@hitorro.com",
        description = "Tests of cluster management",
        module = "util")
public class ClusterTest extends EnhancedTestCase {
    public static String nodeIds[] = {"nodea", "nodeb", "nodec", "noded", "nodee"};


    public void testClusterA() {
        BaseCollection<String, JVS> cluster = new BaseCollection();
        cluster.add(createNode("nodea")
                .addInstanceCapability("index", "sega", true)
                .addInstanceCapability("index", "segb", true)
                .addInstanceCapability("object", "sega", true).getInstanceStub());
        cluster.add(createNode("nodeb")
                .addInstanceCapability("index", "sega", true)
                .addInstanceCapability("index", "segb", true)
                .addInstanceCapability("object", "sega", true).getInstanceStub());
        cluster.add(createNode("nodec")
                .addInstanceCapability("index", "segx", true)
                .addInstanceCapability("index", "segy", true).getInstanceStub());
        cluster.add(createNode("noded")
                .addInstanceCapability("index", "segg", true)
                .addInstanceCapability("index", "segh", true)
                .addInstanceCapability("object", "segh", true).getInstanceStub());
        cluster.add(createNode("nodee")
                .addInstanceCapability("index", "sega", true)
                .addInstanceCapability("index", "segb", true)
                .addInstanceCapability("object", "sega", true).getInstanceStub());

        HTPredicate<JsonNode> pred = IDef.getCapabilityPredicate("index", "segy");

        ForEach fe = new ForEach(IDef.capabilitiesKey, pred);

        HTPredicate<JsonNode> p = fe.getPredicate().and(new PathMatch(IDef.realmIdKey, "default", PathMatch.stringEquals));

        ClusterResults cr = new ClusterResults();
        cr.pred = new JVS2JsonPredicate(p);

        //for (int i = 0; i < 10; i ++)
        cluster.findAll(cr.pred, cr, false);
        String s = p.toString();
        Console.println();
    }

    private IDef createNode(String id) {
        IDef idef = new IDef().initLocal();
        return idef.setID(id);
    }

    private JVS addCap(JVS jvs, String cap, String seg) {
        IDef.addCapability(cap, "", "", seg, true, jvs);
        return jvs;
    }
}