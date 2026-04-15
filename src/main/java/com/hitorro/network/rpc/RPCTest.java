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
package com.hitorro.network.rpc;

import com.hitorro.util.core.Env;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.Timer;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;

import java.io.IOException;

/**
 * rpc call to ourselfs. async (UDP) status message callback.  using point to point, and local + global multicast.
 */
@HTTest(runlevel = RunLevel.Full,
        email = "chris@hitorro.com",
        description = "Sync and async rpc tests (basic)")
public class RPCTest extends EnhancedTestCase {
    private int m_counter = 0;

    public static Callback register() {
        Callback c = new Callback();
        com.hitorro.network.rpc.StatusRPC rpc = (com.hitorro.network.rpc.StatusRPC) com.hitorro.network.rpc.RPCService.s_service.getHandler(com.hitorro.network.rpc.StatusRPC.StatusRPCMethod);
        if (rpc == null) {
            return null;
        }
        rpc.registerRPCCallbackHandler(c);
        return c;
    }

    public static void deregister(Callback c) {
        com.hitorro.network.rpc.StatusRPC rpc = (com.hitorro.network.rpc.StatusRPC) com.hitorro.network.rpc.RPCService.s_service.getHandler(com.hitorro.network.rpc.StatusRPC.StatusRPCMethod);
        if (rpc == null) {
            return;
        }
        rpc.unregisterCallbackHandler(c);
    }

    public void testLoopBackLocalDatagram() {
        if (!com.hitorro.network.rpc.RPCService.EnableMultigram.apply()) {
            Log.test.info("testLoopBackLocalDatagram test skipped as multigram is not enabled.");
            return;
        }
        int max = 100;
        Callback c = register();
        for (int i = 0; i < max; i++) {
            long val = singleLocalLoopbackDatagramCall();
            assertSameSequenceNumber(c, val);
        }
        deregister(c);
    }

    /**
     * Requires a multigram enabled network
     */
    public void testGlobalBackLocalDatagram() {
        if (!com.hitorro.network.rpc.RPCService.EnableMultigram.apply()) {
            Log.test.info("testGlobalBackLocalDatagram test skipped as multigram is not enabled.");
            return;
        }
        int max = 100;
        Callback c = register();
        try {
            for (int i = 0; i < max; i++) {
                long val = singleGlobalLoopbackDatagramCall();
                assertSameSequenceNumber(c, val);

            }
        } finally {
            deregister(c);
        }
    }

    /**
     * Does not need a multigram enabled network
     */
    public void testLoopBackDatagram() {
        Callback c = register();
        try {
            int max = 100;
            for (int i = 0; i < max; i++) {
                long val = singleLoopbackDatagramCall();
                assertSameSequenceNumber(c, val);
            }
        } finally {
            deregister(c);
        }
    }

    private void assertSameSequenceNumber(Callback c, long val) {
        //Env.sleepNSeconds(1);
        Env.sleepMillis(10);
        com.hitorro.network.rpc.RPCMessage msg = c.msg;
        assertNotNull(msg);
        assertEquals(val, msg.getSequenceNumber());
    }

    public void teeestSynchronousLoopBack() {
        Timer t = new Timer();
        int max = 100;
        for (int i = 0; i < max; i++) {
            t();
        }
        t.stop();

    }

    public boolean t() {
        com.hitorro.network.rpc.RPCListener listener = com.hitorro.network.rpc.RPCService.s_service.getRPCListener();
        com.hitorro.network.rpc.RPCMessage msg = com.hitorro.network.rpc.RPCService.s_service.getMessageToSend(listener.getListeningIp(), listener.getListeningPort(), com.hitorro.network.rpc.StatusRPC.StatusRPCMethod, true);
        com.hitorro.network.rpc.RPCMessage response = null;
        try {
            response = msg.sendSyncMessage();
            m_counter++;
        } catch (IOException e) {
            Log.test.error("IOE count  %s %s %e", m_counter, e, e);
            assertAlways("IOE");
        } catch (StoreException e) {
            assertAlways("se");
        } catch (ClassNotFoundException e) {
            assertAlways("cnfe");
        }

        assertNotNull(response);
        return true;
    }

    private long sendMessage(final com.hitorro.network.rpc.RPCMessage msg) {
        long number = msg.aquireNewSequenceNumber(com.hitorro.network.rpc.RPCService.s_service);
        try {
            msg.sendAsyncMessage();
            return number;
        } catch (IOException e) {
            assertAlways("IOE");
        } catch (StoreException e) {
            assertAlways("se");
        }
        return 0;
    }

    public long singleLoopbackDatagramCall() {
        com.hitorro.network.rpc.RPCListener listener = com.hitorro.network.rpc.RPCService.s_service.getRPCListener();
        com.hitorro.network.rpc.RPCMessage msg = com.hitorro.network.rpc.RPCService.s_service.getMessageToSend(listener.getListeningIp(), listener.getListeningPort(), com.hitorro.network.rpc.StatusRPC.StatusRPCMethod, false);
        return sendMessage(msg);
    }

    public long singleLocalLoopbackDatagramCall() {
        com.hitorro.network.rpc.RPCMessage msg = com.hitorro.network.rpc.RPCService.s_service.getLocalBroadcastMessageToSend(com.hitorro.network.rpc.StatusRPC.StatusRPCMethod);
        return sendMessage(msg);
    }

    public long singleGlobalLoopbackDatagramCall() {
        com.hitorro.network.rpc.RPCMessage msg = com.hitorro.network.rpc.RPCService.s_service.getGlobalBroadcastMessageToSend(com.hitorro.network.rpc.StatusRPC.StatusRPCMethod);
        return sendMessage(msg);
    }

    static class Callback implements com.hitorro.network.rpc.RPCAsyncResponseCallbackHandler {
        public com.hitorro.network.rpc.RPCMessage msg;

        public boolean process(com.hitorro.network.rpc.RPCHandler handler, com.hitorro.network.rpc.RPCMessage message) {
            msg = message;
            return true;
        }
    }
}