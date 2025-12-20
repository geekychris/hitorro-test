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
package com.hitorro.util.core.topologicalsort;


import com.hitorro.util.core.Console;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;

import java.util.List;

/**
 * User: chris
 */
@HTTest(runlevel = RunLevel.Full,
        email = "chris@hitorro.com",
        description = "Test the topological sort.")
public class TopologicalSortTest extends EnhancedTestCase {
    public void test() {
        com.hitorro.util.core.topologicalsort.TopologicalSorter sorter = new com.hitorro.util.core.topologicalsort.TopologicalSorter();
        sorter.add(new TestNode("d", new String[]{"e", "f", "g"}));
        sorter.add(new TestNode("c", new String[]{"e", "d"}));
        sorter.add(new TestNode("b", new String[]{"c", "g"}));
        sorter.add(new TestNode("a", new String[]{"b", "d"}));
        List<com.hitorro.util.core.topologicalsort.NodeInterface> sorted = sorter.getSorted();

        StringBuilder sb = new StringBuilder();
        for (com.hitorro.util.core.topologicalsort.NodeInterface tn : sorted) {
            Console.bprint(sb, "[%s] ", tn.getKey());
            for (com.hitorro.util.core.topologicalsort.NodeInterface t : tn.getDependentNodes()) {
                Console.bprint(sb, " %s", t.getKey());
            }
            Console.bprint(sb, "|");
        }
        assertEquals("[e] |[f] |[g] |[d]  e f g|[c]  e d|[b]  c g|[a]  b d|", sb.toString());
    }
}
