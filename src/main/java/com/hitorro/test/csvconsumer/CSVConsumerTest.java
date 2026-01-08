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
package com.hitorro.test.csvconsumer;

import com.hitorro.util.excelaccess.POICSVReader;
import com.hitorro.util.io.csv.csvconsumer.CSVConsumer;
import com.hitorro.util.io.csv.csvconsumer.MultiSheetCSV;
import com.hitorro.util.io.csv.csvconsumer.MultiSheetCSVReaderInterface;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 */

@HTTest(runlevel = RunLevel.Smoke,
        email = "chris@hitorro.com",
        description = "Test csv consumer logic")
public class CSVConsumerTest extends EnhancedTestCase {
    /**
     * Read two sheets from two csv files that pretend to be a multisheet file.
     *
     * @throws IOException
     */
    public void testCSVMultiPageReader() throws IOException {
        File dir = this.getInputFileRelative("csvmulti");
        MultiSheetCSVReaderInterface reader = MultiSheetCSV.getReader(dir);
        assertEquals(reader.getClass(), MultiSheetCSV.class);
        TestConsumer tcP1 = new TestConsumer();
        reader.readLines(tcP1, "page1");
        TestConsumer tcP2 = new TestConsumer();
        reader.readLines(tcP2, "page2");

        assertSheets(tcP1, tcP2);

    }

    /**
     * read two sheets from an multi sheet excel file.
     *
     * @throws IOException
     */
    public void testCSVMultiPageXLSReader() throws IOException {
        File file = this.getInputFileRelative("twosheet.xls");
        if (file.exists()) {
            MultiSheetCSVReaderInterface reader = MultiSheetCSV.getReader(file);
            assertEquals(reader.getClass(), POICSVReader.class);

            TestConsumer tcP1 = new TestConsumer();
            reader.readLines(tcP1, "page1");
            TestConsumer tcP2 = new TestConsumer();
            reader.readLines(tcP2, "page2");

            assertSheets(tcP1, tcP2);
        }

    }


    private void assertSheets(TestConsumer tcP1, TestConsumer tcP2) {
        List<String[]> rows = tcP1.getRows();
        assertEquals(rows.get(0)[0], "Even");

        assertEquals(rows.get(1)[0], "2");

        assertEquals(rows.get(2)[0], "4");

        rows = tcP2.getRows();
        assertEquals(rows.get(0)[0], "First");

        assertEquals(rows.get(1)[0], "Chris");

        assertEquals(rows.get(2)[0], "Kevin");
    }
}

class TestConsumer implements CSVConsumer {
    List<String[]> m_rows = new ArrayList<String[]>();

    public List<String[]> getRows() {
        return m_rows;
    }

    public void line(int row, String[] line) {
        m_rows.add(line);
    }
}
