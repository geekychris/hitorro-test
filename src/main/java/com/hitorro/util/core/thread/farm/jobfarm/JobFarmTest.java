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
package com.hitorro.util.core.thread.farm.jobfarm;

import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;


/**
 * Test that the JobFarm is able to process multiple "jobs" one after the other.  Ensures that the result container
 * contains the correct amount of results and the correct results. In this test we simply have a playload of some
 * integers.  we apply them to their string form and validate the results test.
 */
@HTTest(runlevel = RunLevel.Smoke,
        email = "chris@hitorro.com",
        description = "Test of the job pipeline wrapper for thread farms")
public class JobFarmTest extends EnhancedTestCase {
    public void test() {
        TestEnqueueJobAgent agent = new TestEnqueueJobAgent();
        TestFarmCommand command = new TestFarmCommand();
        // input queue of 100 slots, 4 threads.
        com.hitorro.util.core.thread.farm.jobfarm.JobFarm jp = new com.hitorro.util.core.thread.farm.jobfarm.JobFarm(agent, 100, 4, command);

        runOneThrough(jp, new TestJob(0, 100));
        runOneThrough(jp, new TestJob(159, 150));
        runOneThrough(jp, new TestJob(170, 200));
        runOneThrough(jp, new TestJob(159, 150));
        runOneThrough(jp, new TestJob(170, 200));
    }

    private void runOneThrough(com.hitorro.util.core.thread.farm.jobfarm.JobFarm jp, TestJob tj1) {
        com.hitorro.util.core.thread.farm.jobfarm.JobResultContainer container = jp.execute(tj1);
        assertContainer(container, tj1);

    }

    /**
     * ensure that the result container contains what we expect
     *
     * @param container
     * @param tj
     */
    private void assertContainer(com.hitorro.util.core.thread.farm.jobfarm.JobResultContainer container, TestJob tj) {
        int contCount = container.getProcessedCount();
        int tjCount = tj.getIters();
        assertEquals(contCount, tjCount);
        for (int i = 0; i < tj.getIters(); i++) {
            String s = new Integer(i + tj.getStart()).toString();
            assertTrue(tj.expectedValue(s));
        }
    }
}
