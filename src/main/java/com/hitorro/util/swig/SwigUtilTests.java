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
package com.hitorro.util.swig;

import com.hitorro.util.core.Platform;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;

/**
 * User: chris
 */
@HTTest(runlevel = RunLevel.Never,
        email = "chris@hitorro.com",
        description = "Exercise swig basic OS functionality",
        platform = {Platform.Linux, Platform.MacOSX, Platform.Solaris})
public class SwigUtilTests extends EnhancedTestCase {
    /*public void test ()
    {
        if (Env.loadLib("libSwigUtils", "ht/util/swig"))
        {
            StatFSData data = SwigUtils.statFS("/");
            Console.println("avail blocks: %s, free blocks: %s, free file nodes: %s, fsid: %s, fstype: %s", data.getAvailableBlocks(),
                            data.getFreeBlocks(),
                            data.getFreeFileNodes(),
                            data.getFsID(),
                            data.getFsType());
            Console.println("Process id: %s, parent process id: %s, group id: %s", SwigUtils.getProcessId(), SwigUtils.getParentProcessId(),
                            SwigUtils.getProcessGroupId());

            Console.println("Grentt: %s, group id: %s, effective group id: %s", SwigUtils.getGrent(), SwigUtils.getGroupId(), SwigUtils.getEffectiveGroupId());

            for (UnixEnums.SystemResources e : UnixEnums.SystemResources.values())
            {
                Console.println("Limit: %s, value: %s", e.getDescription(), SwigUtils.getLimit(e.getResourceOrdinal(), 1));
            }

            //StatData sd = SwigUtils.stat("/home/chris/script.charva");
            File f = new File(Env.getBin(), "/lib/jdom.jar");
            StatData sd = SwigUtils.stat(f.getAbsolutePath());

            if (sd != null)
            {
                long mt = sd.getLastDateModified() * 1000;
                long at = sd.getLastAccessTime() * 1000;
                long ct = sd.getLastFileStatusChange() * 1000;
                Console.println("blocksalloc: %s, deviceInodeOn:%s devicetype:%s, filegennumber:%s, filesize:%s, group:%s, inodeprotmode:%s, inodenum:%s, hardlinks:%s, userflags:%s, userid:%s" +
                        "mt:%s, mtdate:%s, at:%s, atdate:%s, ct:%s ctdate:%s, optimalblocksize:%s",
                                sd.getBlocksAllocatedToFile(), sd.getDeviceInodeResidesOn(), sd.getDeviceType(),
                                sd.getFileGenerationNumber(), sd.getFileSizeInBytes(),
                                sd.getGroupId(), sd.getInodeProtectionMode(), sd.getInodesNumber(),
                                sd.getNumberOfHardlinks(), sd.getUserDefinedFlags(), sd.getUserId(),
                                mt, new Date(mt), at, new Date(at), ct, new Date(ct),
                                sd.getOptimalIOOpsBlockSize());
            }
        }
    } */
}
