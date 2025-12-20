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
package com.hitorro.util.core.queue;

import com.hitorro.util.core.Console;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.iterator.queue.AbstractEnqueue;
import com.hitorro.util.core.iterator.queue.Dequeue;
import com.hitorro.util.core.iterator.queue.Enqueue;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;

import java.util.HashSet;
import java.util.Set;

/**
 * Basic tests of Thread Queues
 */
@HTTest(runlevel = RunLevel.Smoke,
        email = "chris@hitorro.com",
        description = "Test threaded queue operations")
public class TestThreadedQueue extends EnhancedTestCase {

    public void testThreadedQuque () throws InterruptedException {
        // does not test threading aspect.
        com.hitorro.util.core.queue.ThreadedQueue<String> tq = new com.hitorro.util.core.queue.ThreadedQueue(10);
        Enqueue<String> nq = new Enqueue(tq);
        Dequeue<String> dq = new Dequeue<>(tq);
        nq.put("one");
        nq.put("two");
        assertEquals(tq.capacity(), 10);
        assertEquals(tq.remainingCapacity(), 8);
        assertEquals(dq.take(), "one");
        assertEquals(dq.take(), "two");
        assertEquals(tq.remainingCapacity(), 10);
    }
    public void testSimpleFillAndDrain() throws com.hitorro.util.core.queue.ThreadedQueueTimeoutException, com.hitorro.util.core.queue.ThreadedQueueCanceledException, InterruptedException {

        Enqueue<String> onet = new Enqueue(new com.hitorro.util.core.queue.ThreadedQueue(10));
        Enqueue<String> twot = new Enqueue(new com.hitorro.util.core.queue.ThreadedQueue(10));
        Enqueue<String> threet = new Enqueue(new com.hitorro.util.core.queue.ThreadedQueue(10));

        AbstractEnqueue<String> queues[] = new AbstractEnqueue[]{onet, twot, threet};

        int priorities[] = new int[]{2, 4, 3};
        com.hitorro.util.core.queue.RoundRobinEnqueue<String> rre = new com.hitorro.util.core.queue.RoundRobinEnqueue("", queues, priorities);

        com.hitorro.util.core.queue.RoundRobinDeQueue<String> rrd = new com.hitorro.util.core.queue.RoundRobinDeQueue("", rre, priorities);
        int iterCount = 30;
        for (int i = 0; i < iterCount; i++) {
            rre.put(Integer.toString(i));
        }

        // Here we expect the numbers to come out in sequence ONLY because we are not over populating
        // any queue (we are doing a fill and drain in sequence)
        for (int i = 0; i < iterCount; i++) {
            String fromQ = rrd.take();
            String expected = Integer.toString(i);
            assertEquals(fromQ, expected);
        }
        assertTrue(rrd.isEmpty());

    }

    public void testConcurrentFillDrain() throws com.hitorro.util.core.queue.ThreadedQueueTimeoutException, com.hitorro.util.core.queue.ThreadedQueueCanceledException {
        Enqueue<String> onet = new Enqueue(new com.hitorro.util.core.queue.ThreadedQueue(10));

        Enqueue<String> twot = new Enqueue(new com.hitorro.util.core.queue.ThreadedQueue(10));

        Enqueue<String> threet = new Enqueue(new com.hitorro.util.core.queue.ThreadedQueue(10));

        AbstractEnqueue<String> queues[] = new AbstractEnqueue[]{onet, twot, threet};

        int priorities[] = new int[]{2, 4, 3};
        com.hitorro.util.core.queue.RoundRobinEnqueue<String> rre = new com.hitorro.util.core.queue.RoundRobinEnqueue("", queues, priorities);

        com.hitorro.util.core.queue.RoundRobinDeQueue<String> rrd = new com.hitorro.util.core.queue.RoundRobinDeQueue("", rre, priorities);
        int iterCount = 1000;
        TestRunner tr = new TestRunner(iterCount, rrd);
        Thread t = new Thread(tr);
        t.setName("RoundRobinTestThread");
        t.start();
        for (int i = 0; i < iterCount; i++) {
            rre.put(Integer.toString(i));
        }

        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < 10; i++) {
            Env.sleepNSeconds(1);
            if (tr.getIters() == iterCount) {
                break;
            }
        }
        // did we actually pull out iterCount worth of items?
        int count = tr.getIters();
        assertEquals(count, iterCount);

        assertTrue(tr.gotAllItems());

    }

    class TestRunner implements Runnable {
        private int iterCount = 0;
        private com.hitorro.util.core.queue.RoundRobinDeQueue<String> rrd;
        private int i = 0;
        private Set<String> set = new HashSet();

        public TestRunner(int iterCount, com.hitorro.util.core.queue.RoundRobinDeQueue rrd) {
            this.iterCount = iterCount;
            this.rrd = rrd;
        }

        public int getIters() {
            return i;
        }

        public void run() {
            Env.sleepNSeconds(3);
            for (i = 0; i < iterCount; i++) {
                try {
                    set.add(rrd.take());
                } catch (InterruptedException e) {
                    Log.util.error("Error: %s %s", e, e);
                }
            }
            Console.println();
        }

        public boolean gotAllItems() {
            for (int i = 0; i < iterCount; i++) {
                String s = Integer.toString(i);
                if (!set.contains(s)) {
                    return false;
                }
            }
            return true;
        }
    }

}
