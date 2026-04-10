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

import com.hitorro.util.core.iterator.queue.AbstractEnqueue;



public class TestEnqueueJobAgent implements com.hitorro.util.core.thread.farm.jobfarm.JobFarmEnqueueAgent<TestJob, Integer, String> {
    public int executeEnqueue(TestJob testJob, AbstractEnqueue<com.hitorro.util.core.thread.farm.jobfarm.JobFarmElement<Integer, String, TestJob>> threadedQueue) {
        int val = testJob.getStart();
        for (int i = 0; i < testJob.getIters(); i++) {
            com.hitorro.util.core.thread.farm.jobfarm.JobFarmElement jpe = new com.hitorro.util.core.thread.farm.jobfarm.JobFarmElement(new Integer(val + i), this, testJob);
            try {
                threadedQueue.put(jpe);
            } catch (InterruptedException e) {
            }
        }
        return testJob.getIters();
    }

    public com.hitorro.util.core.thread.farm.jobfarm.JobResultContainer getResultContainer() {
        return new com.hitorro.util.core.thread.farm.jobfarm.ListJobResultContainer();
    }
}
