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
package com.hitorro.test.statemachine;

import com.hitorro.util.graph.Graph;
import com.hitorro.util.graph.GraphPanel;
import com.hitorro.util.statemachine.DirectedEdge;
import com.hitorro.util.statemachine.MooreStateMachine;
import com.hitorro.util.statemachine.State;
import com.hitorro.util.statemachine.StateMachineUtil;
import com.hitorro.util.statemachine.scoreboard.ScoreBoard;
import com.hitorro.util.statemachine.scoreboard.StateRow;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: chris Date: Feb 12, 2005 Time: 8:42:20 AM
 */

@HTTest(runlevel = RunLevel.Smoke,
        email = "chris@hitorro.com",
        description = "Test the state machine")
public class StateMachineTest extends EnhancedTestCase {
    public static JFrame f;
    public static Thread t;


    public static void tesdtStateMachineGraphForNanny() {
        Graph g = new Graph();
        GraphPanel gp = g.getGraphPanel();
        MooreStateMachine sm = StateMachineUtil.initStateRegistry(new File("/ht/platform/data/statemachine/nanny.xls"));
        State states[] = sm.getStates();
        for (State state : states) {
            DirectedEdge edges[] = state.getStateTransitions();
            for (DirectedEdge edge : edges) {
                gp.addEdge(edge.getThisState().getName(), edge.getNextState().getName(), 150, edge.getDescription());
            }
        }

        //1. Optional: Specify who draws the window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);

        //2. Create the frame.
        f = new JFrame("Graph");

        //3. Optional: What happens when the frame closes?
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //4. Create components and put them in the frame.
        //...create emptyLabel...
        g.init();
        t = new Thread();
        g.start();
        g.setSize(1000, 600);
        f.getContentPane().add(g, BorderLayout.CENTER);
        f.getContentPane().setVisible(true);
        //5. Size the frame.
        f.setSize(1000, 600);
        //frame.pack();

        //6. Show it.
        f.setVisible(true);


    }

    public void tsestStateMachineGraph() {
        Graph g = new Graph();
        GraphPanel gp = g.getGraphPanel();
        MooreStateMachine sm = StateMachineUtil.initStateRegistry(new File("/Users/chriscollins/statetest.xls"));
        State states[] = sm.getStates();
        for (State state : states) {
            DirectedEdge edges[] = state.getStateTransitions();
            for (DirectedEdge edge : edges) {
                gp.addEdge(edge.getThisState().getName(), edge.getNextState().getName(), 50, edge.getDescription());
            }
        }

        //1. Optional: Specify who draws the window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);

        //2. Create the frame.
        f = new JFrame("Graph");

        //3. Optional: What happens when the frame closes?
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //4. Create components and put them in the frame.
        //...create emptyLabel...
        g.init();
        t = new Thread();
        g.start();
        g.setSize(500, 300);
        f.getContentPane().add(g, BorderLayout.CENTER);
        f.getContentPane().setVisible(true);
        //5. Size the frame.
        f.setSize(500, 300);
        //frame.pack();

        //6. Show it.
        f.setVisible(true);


    }

    public void tsestStateMachine() {
        MooreStateMachine sm = StateMachineUtil.initStateRegistry(new File("/Users/chriscollins/statetest.xls"));

        TestStateRow thirteen = new TestStateRow();
        thirteen.setPayload(new Long(13));


        TestStateRow twenty = new TestStateRow();
        twenty.setPayload(new Long(20));
    }

    public void test() {
        ScoreBoard<Long, TestStateRow> board = new ScoreBoard<Long, TestStateRow>(4);
    }
}

class TestStateRow extends StateRow<Long> {

}
