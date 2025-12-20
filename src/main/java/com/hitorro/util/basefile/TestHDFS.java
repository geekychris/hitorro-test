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
package com.hitorro.util.basefile;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.basedms.session.DMSSessionFactory;
import com.hitorro.util.basefile.filters.FileExtension;
import com.hitorro.util.basefile.filters.FileStartsWith;
import com.hitorro.util.basefile.filters.IsDir;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.fs.file.FileFileSystem;
import com.hitorro.util.basefile.tools.BaseLock;
import com.hitorro.util.basefile.tools.dirreplicator.CopyStatus;
import com.hitorro.util.basefile.tools.dirreplicator.DirectoryReplicator;
import com.hitorro.util.basefile.tools.queue.reader.DirectoryVisitorIterator;
import com.hitorro.util.basefile.tools.queue.reader.GoodFileKeyWriter;
import com.hitorro.util.basefile.tools.queue.reader.StreamingDirectoryVisitorIterator;
import com.hitorro.util.basefile.tools.queue.writer.BaseFileToQueueStructureMover;
import com.hitorro.util.basefile.tools.queue.writer.BlockFileQueueWriter;
import com.hitorro.util.basefile.tools.queue.writer.partitioners.DateBasedWriterPartitioner;
import com.hitorro.util.basefile.tools.queue.writer.serializer.HTSerializableWriterWritable;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.opers.HTPredicate;
import com.hitorro.util.core.opers.LogicalNotOperator;
import com.hitorro.util.core.opers.LogicalOrOperator;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.html.Link;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;
import com.hitorro.util.typesystem.BaseSession;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

/**
 * ht.util.file.TestHDFS
 */
@HTTest(runlevel = RunLevel.Never,
        email = "chris@hitorro.com",
        description = "")
public class TestHDFS extends EnhancedTestCase {
    public void testDirCopy() throws IOException {
        File inDir = this.getInputFileRelative("");
        FileFileSystem inFFS = new FileFileSystem(new File("/"));
        BaseFile src = inFFS.getFile(inDir.getAbsolutePath());
        File outDir = this.getOutputFileRelative("testcopy");
        FileFileSystem outFFS = new FileFileSystem(new File("/"));
        BaseFile dest = outFFS.getFile(outDir.getAbsolutePath());
        dest.mkdir();
        FileStartsWith dot = new FileStartsWith(".", false);
        HTPredicate<BaseFile> not = new LogicalNotOperator(dot);
        DirectoryReplicator dc = new DirectoryReplicator(src, dest, not, 10);
        CopyStatus flagCopy = dc.copy();
        if (!flagCopy.success()) {
            Console.println("not good");
        }
    }

    public void teestLock() throws IOException {
        //File lockDir = this.getOutputFileRelative("");
        File lockDir = new File("/tmp");
        FileFileSystem ffs = new FileFileSystem(lockDir);
        BaseFile bf = ffs.getFile("");
        BaseLock bl = bf.getLock("mylock.txt", "123");
        if (bl.takeLock()) {
            if (bl.takeLock()) {
                // as expected
                Console.println();
            } else {
                Console.println();
            }

        }
        BaseLock bl2 = bf.getLock("mylock.txt", "124");
        if (bl2.takeLock()) {
            Console.println();


        } else {
            // expected path
            Console.println();
        }
        bl.release();
        if (bl2.takeLock()) {
            // valid path
            Console.println();


        } else {
            // not valid path path
            Console.println();
        }
    }

    public void te3stWriter() throws IOException, StoreException {
        int DocsToWrite = 10000;
        File tmpWriter = this.getOutputFileRelative("writer");
        FileUtil.deleteDirectoryContent(tmpWriter, false);
        File queueroot = this.getOutputFileRelative("queueroot");
        FileUtil.deleteDirectoryContent(queueroot, false);
        FileFileSystem ffs = new FileFileSystem(queueroot);
        BaseFile bf = ffs.getFile("written");

        LinkPartitioner part = new LinkPartitioner();
        BaseFile tmpOutputBF = FileFileSystem.Root.getFile(tmpWriter.getAbsolutePath());
        BaseFile tmpOutgoing = tmpOutputBF.getChild("out");
        BaseFileToQueueStructureMover mover = new BaseFileToQueueStructureMover(false, tmpOutgoing, bf, false);
        BlockFileQueueWriter writer = new BlockFileQueueWriter(DMSSessionFactory.getFactory().getSession(), new HTSerializableWriterWritable(),
                bf, 1000, part, mover);
        long now = System.currentTimeMillis();
        for (int i = 0; i < DocsToWrite; i++) {
            // Link (String url, LinkType type, String title, String typeString, URL root)
            Link l = new Link(Fmt.S("http://urlkhasdkfhasdkfhl %s", i),
                    Link.LinkType.StyleSheet,
                    Fmt.S("Title af:%s", i),
                    "ts",
                    new URL(Fmt.S("http://root%s", i)));
            l.setPublishedDate(now + (i * 100));
            writer.add(l);
        }
        writer.flushAndHoldTillComplete();
        //int wrote = writer.getTotalObjectsWritten();
        //int filesWritten = writer.getFilesWrittenCount();
        File lgkFile = new File("/tmp/lgk.lgk");
        lgkFile.delete();
        GoodFileKeyWriter lgk = new GoodFileKeyWriter(lgkFile);
        FileExtension filter = new FileExtension("ser", true);
        DirectoryVisitorIterator dirvisiter = new DirectoryVisitorIterator(bf, -1, lgk, new LogicalOrOperator(new IsDir(), filter));

        BaseSession session = DMSSessionFactory.getFactory().getSession();
        StreamingDirectoryVisitorIterator iter = new StreamingDirectoryVisitorIterator(dirvisiter, session);
        int counter = 0;
        while (iter.hasNext()) {
            Link l = (Link) iter.next();
            //Console.println("%s-%s", l, counter);
            counter++;
        }
        assertEquals(counter, DocsToWrite);
    }
}

class LinkPartitioner extends DateBasedWriterPartitioner<Link> {
    public Date getDateField() {
        return new Date(current.getPublishedDate());
    }

    @Override
    public void initForPass(final JsonNode params) {

    }
}
