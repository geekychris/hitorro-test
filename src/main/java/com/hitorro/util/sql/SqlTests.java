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
package com.hitorro.util.sql;

import com.hitorro.base.filesetmanager.FileSet;
import com.hitorro.base.filesetmanager.FileSetManager;
import com.hitorro.util.basefile.fs.BaseFile;
import com.hitorro.util.basefile.fs.file.FileFileSystem;
import com.hitorro.util.core.Console;
import com.hitorro.util.core.iterator.AbstractIterator;
import com.hitorro.util.core.iterator.JsonValueSource;
import com.hitorro.util.json.JSONElement;
import com.hitorro.util.json.JSONNumber;
import com.hitorro.util.json.visitors.NodeCounter;
import com.hitorro.sql.iterators.SqlIterator;
import com.hitorro.sql.latch.JSONLatchSource;
import com.hitorro.sql.latch.exceptions.LatchException;
import com.hitorro.sql.latch.var.vartypes.VarBooleanIntf;
import com.hitorro.sql.latch.var.vartypes.VarIntIntf;
import com.hitorro.sql.latch.var.vartypes.VarIntf;
import com.hitorro.sql.parser.LatchContext;
import com.hitorro.sql.parser.StatementDeParser;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;
import com.hitorro.util.typesystem.TypeManager;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.Select;

import java.io.IOException;
import java.io.StringReader;


@HTTest(runlevel = RunLevel.Full,
        email = "chris@hitorro.com",
        description = "Tests for sql latch ")
public class SqlTests extends EnhancedTestCase {
    private static SimpleTestCase orderCases[] = new SimpleTestCase[]
            {
                    new SimpleTestCase("simple_json.csv", "sql_test", "select  * from foo order by age", 7),
            };
    private static SimpleTestCase cases[] = new SimpleTestCase[]{

            new SimpleTestCase("simple_json.csv", "sql_test", "select  first_name, last_name, age, location from foo where location='san francisco'", 2),

            // test basic aggregates and appropriate function type selection
            new SimpleTestCase("simple_json.csv", "sql_test", "select  count(first_name), min(age), max(age), sum(age), min(height), max(height), sum(height) from foo where id <= 6", 1) {
                public boolean ensureTrue(int row, JSONElement root) {
                    JSONElement l = root.getFromPath("count(first_name)");
                    if (l == null) {
                        return false;
                    }
                    return ((JSONNumber) l).get().intValue() == 6;
                }
            },

            new SimpleTestCase("simple_json.csv", "sql_test", "select  length(first_name), last_name, age, location from foo where id=1", 1) {
                public boolean ensureTrue(int row, JSONElement root) {
                    JSONElement l = root.getFromPath("length(first_name)");
                    if (l == null) {
                        return false;
                    }
                    return ((JSONNumber) l).get().intValue() == "chris".length();
                }
            },
            //single >= integer comparator
            new SimpleTestCase("simple_json.csv", "sql_test", "select  first_name, last_name, age, location from foo where age >= 40", 4),

            new SimpleTestCase("simple_json.csv", "sql_test", "select  replace(location, 'san', 'fred')  as rep from foo where id=2", 1) {
                public boolean ensureTrue(int row, JSONElement root) {
                    JSONElement l = root.getFromPath("rep");
                    if (l == null) {
                        return false;
                    }
                    return l.toString().equals("fred francisco");
                }
            },

            // single int equality
            new SimpleTestCase("simple_json.csv", "sql_test", "select  first_name, last_name, age, location from foo where id = 2", 1),

            // >=, and operator and < of integers
            new SimpleTestCase("simple_json.csv", "sql_test", "select  first_name, last_name, age, location from foo where age >= 40 and age < 42", 1),

            // string based test


            // select based on a math function from a selected field (derived select column)
            new SimpleTestCase("simple_json.csv", "sql_test", "select  first_name, (age*2) as multi_age from foo where multi_age=80", 1),

            //string concatination
            new SimpleTestCase("simple_json.csv", "sql_test", "select  first_name||' '||last_name as f_l, (age*2) as multi_age from foo where multi_age=80", 1) {
                public boolean ensureTrue(int row, JSONElement root) {
                    JSONElement o = root.getFromPath("f_l");
                    if (o == null) {
                        return false;
                    }
                    return o.toString().equals("chris collins");
                }

            }
    };

    public void teestOrder() throws IOException, JSQLParserException {
        testCaseSupport(orderCases);
    }

    public void testA() throws IOException, JSQLParserException {
        testCaseSupport(cases);
    }

    public void testCaseSupport(SimpleTestCase cases[]) throws IOException, JSQLParserException {
        for (SimpleTestCase stc : cases) {
            String sql = stc.getSql();
            SqlIterator sqlIter = setupFromCSV(stc.getFile(), stc.getType(), sql);
            int counter = 0;
            while (sqlIter.hasNext()) {
                JsonValueSource e = sqlIter.next();
                assertTrue(stc.ensureTrue(counter, e));
                counter++;
                Console.println("%m", e);
            }
            assertEquals(counter, stc.getExpected());
            //Console.println("%s %s %s", stc.getSql(), counter, stc.getExpected());
        }

    }

    private SqlIterator setupFromCSV(String inputFile, String type, String sql) throws IOException, JSQLParserException {
        BaseFile bf = this.getInputFileRelativeBaseFile(inputFile);
        assertTrue(bf.exists());
        return SqlIterator.getIteratorFromCSVFile(bf, type, sql);
    }

    /*
   Case

   SELECT store_name, CASE store_name
     WHEN 'Los Angeles' THEN Sales * 2
     WHEN 'San Diego' THEN Sales * 1.5
     ELSE Sales
     END
   "New Sales",
   Date
   FROM Store_Information


       || (concat)

       @@ (matches)




    */
    /*
    Done

    is null
    is not null


     */
    public void teestJSONSql() throws IOException, LatchException, JSQLParserException {
        BaseFile bf = FileFileSystem.Root.getFile("/Users/chris/chris_twitter.json.gz");
        FileSet twitter = FileSetManager.getSet("twitterraw");
        AbstractIterator<JsonValueSource> iter = twitter.getIterator(bf);
        String sql = "select  user.friends_count as friends_count, sqrt(4 * 4) as s from my_table where s < 90 and user.friends_count > 200 and in_reply_to_user_id_str is null order by user.friends_count, retweet_count desc";

        SqlIterator sqlIter = new SqlIterator(iter, "twitter_co", sql);
        int counter = 0;

        while (sqlIter.hasNext()) {
            JsonValueSource e = sqlIter.next();
            Object s = e.getValue(e, "s");
            Object c = e.getValue(e, "friends_count");
            Console.println("%s %s", s, c);
            counter++;
            if (counter > 1000) {
                break;
            }
        }
        Console.println();
    }

    public void twestJSON() throws IOException, LatchException {
        JSONLatchSource ls = new JSONLatchSource(TypeManager.get().getTypeByShortName("twitter_co"));
        BaseFile bf = FileFileSystem.Root.getFile("/Users/chris/chris_twitter.json.gz");
        FileSet twitter = FileSetManager.getSet("twitterraw");
        AbstractIterator<JsonValueSource> iter = twitter.getIterator(bf);

        NodeCounter nc = new NodeCounter();
        int counter = 0;
        VarIntf friendCount = ls.getLatch("user.friends_count");
        VarIntf oneHundred = ls.getConstant("100", 100);
        VarBooleanIntf gt = ls.getComparator("user.friends_count", "100", ">=", "gt100");
        VarIntIntf multIt = (VarIntIntf) ls.getOperator("user.friends_count", "100", "*", "multIt");
        while (iter.hasNext()) {
            JsonValueSource e = iter.next();
            nc.visit(e, 0);
            ls.present(e);
            boolean val = gt.getBoolean();
            Console.println("%s %s mult: %s", val, friendCount, multIt.getInt());
            Object docId = e.getValue("docid");
            Console.println("%s", docId);
            counter++;
            if (counter > 1000) {
                break;
            }
        }
        nc.printElemsAsType();
        Console.println();
    }

    public void teestJSQL() throws JSQLParserException, IOException {
        CCJSqlParserManager pm = new CCJSqlParserManager();
        //String sql = "SELECT sqrt(x.a + y.b + 1.5) as fred, json.fred, a.json.frank FROM MY_TABLE1 x, MY_TABLE2 y, (SELECT * FROM MY_TABLE3) LEFT OUTER JOIN MY_TABLE4 "+
        //" WHERE ID = (SELECT MAX(ID) FROM MY_TABLE5) AND ID2 IN (SELECT * FROM MY_TABLE6)" ;
        String sql = "Select user, user.friends_count, sqrt(4 * 4)as s from my_table where s > 90 and user.friends_count = 10 order by user.friends_count, retweet_count desc";
        net.sf.jsqlparser.statement.Statement statement = pm.parse(new StringReader(sql));
        Select s = (Select) statement;
        StringBuffer sb = new StringBuffer();
        JSONLatchSource ls = new JSONLatchSource(TypeManager.get().getTypeByShortName("twitter_co"));
        LatchContext lc = new LatchContext(ls);
        StatementDeParser deparser = new StatementDeParser(sb, lc);
        deparser.visit(s);
        Console.println("%s", sb.toString());
    }
}

class SimpleTestCase {
    private String file;
    private String sql;
    private String type;
    private int expected;

    public SimpleTestCase(String file, String type, String sql, int expectedCount) {
        this.setFile(file);
        this.setSql(sql);
        this.setType(type);
        this.setExpected(expectedCount);
    }

    public String getFile() {
        return file;
    }

    public void setFile(final String file) {
        this.file = file;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(final String sql) {
        this.sql = sql;
    }

    public int getExpected() {
        return expected;
    }

    public void setExpected(final int expected) {
        this.expected = expected;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    /**
     * replace this function if you want to have some kind of test against the data row.
     *
     * @param row
     * @param root
     * @return
     */
    public boolean ensureTrue(int row, JsonValueSource root) {
        return true;
    }
}