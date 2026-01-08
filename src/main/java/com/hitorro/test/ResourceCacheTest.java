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

import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.io.resourcecache.basefile.BaseFileResourceCache;
import com.hitorro.util.io.resourcecache.basefile.BaseFileResourceContext;
import com.hitorro.util.io.resourcecache.file.ResourceCache;
import com.hitorro.util.io.resourcecache.file.ResourceContext;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;
import com.hitorro.util.versioning.BaseFileDirectoryVersionNode;
import com.hitorro.util.versioning.DirectoryVersionNode;

import java.io.File;
import java.io.IOException;

/**
 */
@HTTest(runlevel = RunLevel.Smoke,
        email = "chris@hitorro.com",
        description = "Series of resource cache tests")
public class ResourceCacheTest extends EnhancedTestCase {
    private static final String TmpResource = "tmpresource";


    public void testBaseFileCreation() throws IOException {
        BaseFileResourceCache resourceCache = BaseFileResourceCache.getCache();
        BaseFileResourceContext rc = createBaseFileResource(resourceCache, TmpResource, 1, 2, 3);
        rc.commit();

        rc = createBaseFileResource(resourceCache, TmpResource, 1, 2, 3);
        // need this for verification in a minute
        long oneTwoThreeBuild = rc.getVersionNode().getBuildNumber();
        rc.commit();
        rc = createBaseFileResource(resourceCache, TmpResource, 2, 0, 1);
        long twoZeroOne = rc.getVersionNode().getBuildNumber();
        // need this for verification in a minute
        rc.commit();
        rc.getVersionNode().setOpenResourceVersionLink(true, true);

        BaseFileDirectoryVersionNode dvn = resourceCache.getResource(TmpResource, "1.2.3+");
        assertNotNull(dvn);
        assertEquals(dvn.getBuildNumber(), oneTwoThreeBuild);
        BaseFile target = dvn.getDirectory().getChild("test.txt");
        assertTrue(target.exists());
        String val = target.readString();
        assertNotNull(val);
        assertEquals(dvn.getName(), val);


        dvn = resourceCache.getResource(TmpResource, "2.0.1");
        assertNotNull(dvn);
        assertEquals(dvn.getBuildNumber(), twoZeroOne);

        target = dvn.getDirectory().getChild("test.txt");
        assertTrue(target.exists());
        val = target.readString();
        assertNotNull(val);
        assertEquals(dvn.getName(), val);
    }

    public void testCreation() throws IOException {
        ResourceCache resourceCache = ResourceCache.getCache();
        ResourceContext rc = createResource(resourceCache, TmpResource, 1, 2, 3);
        rc.commit();

        rc = createResource(resourceCache, TmpResource, 1, 2, 3);
        // need this for verification in a minute
        long oneTwoThreeBuild = rc.getVersionNode().getBuildNumber();
        rc.commit();
        rc = createResource(resourceCache, TmpResource, 2, 0, 1);
        long twoZeroOne = rc.getVersionNode().getBuildNumber();
        // need this for verification in a minute
        rc.commit();

        DirectoryVersionNode dvn = resourceCache.getResource(TmpResource, "1.2.3+");
        assertNotNull(dvn);
        assertEquals(dvn.getBuildNumber(), oneTwoThreeBuild);
        File target = new File(dvn.getDirectory(), "test.txt");
        assertFileExistsWithExplanation(target);
        StringBuilder sb = FileUtil.readFromFile(target);
        assertNotNull(sb);
        assertEquals(dvn.getName(), sb.toString());


        dvn = resourceCache.getResource(TmpResource, "2.0.1");
        assertNotNull(dvn);
        assertEquals(dvn.getBuildNumber(), twoZeroOne);

        target = new File(dvn.getDirectory(), "test.txt");
        assertFileExistsWithExplanation(target);
        sb = FileUtil.readFromFile(target);
        assertNotNull(sb);
        assertEquals(dvn.getName(), sb.toString());
    }

    public void testImport() throws IOException {
        String tmpName = "2.0.1.1166548718286";
        File inDir = getInputFileRelative(tmpName, true);
        File outDir = this.getOutputFileRelative(tmpName, true);
        assertFileExistsWithExplanation(inDir);
        try {
            FileUtil.copyDirectory(inDir, outDir);
        } catch (IOException e) {
            assertAlways("Unable to copy test input tree %s %e", e, e);
        }
        assertFileExistsWithExplanation(outDir);
        ResourceCache resourceCache = ResourceCache.getCache();
        ResourceContext rc = resourceCache.getTempResourceContextFromDirectory(TmpResource, outDir);
        resourceCache.remove(rc.getVersionNode());
        rc.commit();
        File dir = rc.getPath();
        this.assertFileExistsWithExplanation(dir);
        File testFile = new File(dir, "test.txt");
        assertFileExistsWithExplanation(testFile);
        StringBuilder sb = FileUtil.readFromFile(testFile);
        assertNotNull(sb);
        assertEquals(rc.getVersionNode().getName(), sb.toString());

    }


    public void testImportWithCopy() throws IOException {
        String tmpName = "2.0.1.1166548718286";
        File inDir = getInputFileRelative(tmpName, true);
        assertFileExistsWithExplanation(inDir);

        ResourceCache resourceCache = ResourceCache.getCache();
        ResourceContext rc = resourceCache.getTempResourceContextFromDirectoryWithCopy(TmpResource, 2, 3, 4, inDir);
        resourceCache.remove(rc.getVersionNode());
        rc.commit();
        File dir = rc.getPath();
        this.assertFileExistsWithExplanation(dir);
        File testFile = new File(dir, "test.txt");
        assertFileExistsWithExplanation(testFile);
        StringBuilder sb = FileUtil.readFromFile(testFile);
        assertNotNull(sb);
    }


    public void testLink() throws Exception {
        String tmpName = "2.0.1.1166548718286";
        File inDir = getInputFileRelative(tmpName, true);
        assertFileExistsWithExplanation(inDir);

        ResourceCache resourceCache = ResourceCache.getCache();
        ResourceContext rc = resourceCache.getTempResourceContextFromDirectoryWithCopy(TmpResource, 2, 3, 4, inDir);
        resourceCache.remove(rc.getVersionNode());
        rc.commit();
        File dir = rc.getPath();
        this.assertFileExistsWithExplanation(dir);
        File testFile = new File(dir, "test.txt");
        assertFileExistsWithExplanation(testFile);
        StringBuilder sb = FileUtil.readFromFile(testFile);
        assertNotNull(sb);
        rc.getVersionNode().setOpenResourceVersionLink(true, true);

    }

    /**
     * Ensure that rollback of a compContext results in the removal of its directory tree.
     */
    public void testRollback() {
        ResourceCache resourceCache = ResourceCache.getCache();
        ResourceContext rc = createResource(resourceCache, TmpResource, 1, 2, 3);
        rc.rollback();
        File dir = rc.getPath();
        assertFileDoesNotExistWithExplanation(dir);
    }

    private ResourceContext createResource(ResourceCache rc, String resource, long major, long minor, long patch) {
        ResourceContext rcontext = rc.getTempResourceContext(resource, major, minor, patch);
        File dir = rcontext.getPath();
        File tstFile = new File(dir, "test.txt");
        assertFileExistsWithExplanation(dir);
        try {
            FileUtil.writeToFile(tstFile, rcontext.getVersionNode().getName());
        } catch (IOException e) {
            assertAlways("Exception thrown in testCreation %s %e", e, e);
        }
        return rcontext;
    }

    private BaseFileResourceContext createBaseFileResource(BaseFileResourceCache rc, String resource, long major, long minor, long patch) {
        BaseFileResourceContext rcontext = rc.getTempResourceContext(resource, major, minor, patch);
        BaseFile dir = rcontext.getPath();
        BaseFile tstFile = dir.getChild("test.txt");
        assertTrue(dir.exists());
        try {
            tstFile.writeString(rcontext.getVersionNode().getName());
        } catch (IOException e) {
            assertAlways("Exception thrown in testCreation %s %e", e, e);
        }
        return rcontext;
    }
}
