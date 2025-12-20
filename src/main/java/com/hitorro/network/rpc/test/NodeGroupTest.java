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
package com.hitorro.network.rpc.test;

import com.hitorro.util.core.Console;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;
import com.hitorro.util.typesystem.HTObjectInputStream;
import com.hitorro.util.typesystem.HTObjectOutputStream;
import com.hitorro.util.typesystem.HTObjectStreamFactory;
import com.hitorro.util.typesystem.HTSerializable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Oct 29, 2005 Time: 7:08:28 PM
 */
@HTTest(runlevel = RunLevel.Full,
        email = "chris@hitorro.com",
        description = "Group nodes")
public class NodeGroupTest extends EnhancedTestCase {
    public static final String TQ = "transcodequeue";

    public static final HTSerializable getSerializedLoopback(HTSerializable pts)
            throws IOException, ClassNotFoundException, StoreException {
        return getHTSerializableFromByteArray(getBytesFromHTObject(pts));

    }

    public static final byte[] getBytesFromHTObject(HTSerializable pts)
            throws IOException, StoreException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        HTObjectOutputStream os = HTObjectStreamFactory.getOutputStream(baos, null, false);
        os.writeVersionedObject(pts);
        os.flush();
        baos.close();
        byte buf[] = baos.toByteArray();
        return buf;
    }

    public static final HTSerializable getHTSerializableFromByteArray(byte buf[])
            throws IOException, StoreException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(buf);

        HTObjectInputStream is = HTObjectStreamFactory.getInputStream(bais, null);

        return is.readVersionedObject();
    }

    public void testBasicGroup() throws IOException, StoreException, ClassNotFoundException {
        com.hitorro.network.rpc.cluster.group.NodeGroup ngEmpty = new com.hitorro.network.rpc.cluster.group.NodeGroup();
        com.hitorro.network.rpc.cluster.group.NodeGroup ng = new com.hitorro.network.rpc.cluster.group.NodeGroup();
        String sysId = "n-1";
        com.hitorro.network.rpc.cluster.group.NodeGroupMember member = ng.getOrAddNode(sysId);
        com.hitorro.network.rpc.cluster.group.QueueDefinition qd = new com.hitorro.network.rpc.cluster.group.QueueDefinition();
        qd.setName(TQ);
        qd.setMod(10);
        qd.setMods(new int[]{1, 2, 4});
        member.addMemberDefinition(qd);

        com.hitorro.network.rpc.cluster.group.NodeGroupMember t = ng.getMember("n-1");
        assertNotNull(t);
        com.hitorro.network.rpc.cluster.group.MemberDefinition md = t.get(TQ);
        assertNotNull(md);
        assertEquals(((com.hitorro.network.rpc.cluster.group.QueueDefinition) md).getMod(), 10);

        com.hitorro.network.rpc.test.TestCallback tc = new com.hitorro.network.rpc.test.TestCallback();
        com.hitorro.network.rpc.cluster.group.NodeGroupDiffer differ = new com.hitorro.network.rpc.cluster.group.NodeGroupDiffer();
        differ.diff(ngEmpty, ng, tc);
        Console.println(">>>");
        Console.println(tc.getBuff());

        com.hitorro.network.rpc.cluster.group.NodeGroup ng2 = ng.copy();
        com.hitorro.network.rpc.cluster.group.NodeGroupMember n2 = ng2.getOrAddNode("n-2");
        com.hitorro.network.rpc.cluster.group.QueueDefinition qd2 = new com.hitorro.network.rpc.cluster.group.QueueDefinition();
        qd2.setName(TQ);
        qd2.setMod(10);
        qd2.setMods(new int[]{3, 5, 7, 9});
        n2.addMemberDefinition(qd2);

        tc.clear();

        // test not only adding a second node (which checks out some more of the diffing logig, it
        // also tests that the object is "wire worthy"
        differ.diff(ng, (com.hitorro.network.rpc.cluster.group.NodeGroup) getSerializedLoopback(ng2), tc);
        Console.println(">>>");
        Console.println(tc.getBuff());
    }

}
