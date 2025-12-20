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

import com.hitorro.util.core.thread.farm.FarmCommand;


/**
 * Command that does nothing other than apply the integer to the equiv string
 */
public class TestFarmCommand extends FarmCommand<com.hitorro.util.core.thread.farm.jobfarm.JobFarmElement<Integer, String, TestJob>,
		com.hitorro.util.core.thread.farm.jobfarm.JobFarmElement<Integer, String, TestJob>,
        Object> {
    public com.hitorro.util.core.thread.farm.jobfarm.JobFarmElement<Integer, String, TestJob> apply(com.hitorro.util.core.thread.farm.jobfarm.JobFarmElement<Integer, String, TestJob> inElement) {
        Integer i = inElement.getElement();
        inElement.setResult(i.toString());
        return inElement;
    }
}
