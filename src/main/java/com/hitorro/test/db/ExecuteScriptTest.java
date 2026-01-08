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
package com.hitorro.test.db;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.basedms.db.HibernateService;
import com.hitorro.basedms.db.executor.Executor;
import com.hitorro.basedms.db.executor.Result;
import com.hitorro.basedms.db.executor.ResultTable;
import com.hitorro.basedms.db.executor.Script;
import com.hitorro.jsontypesystem.propreaders.JVSProperties;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.params.HTProperties;
import com.hitorro.util.json.keys.propaccess.PropaccessError;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * <p/>
 * Test the Execution of SQL Scripts
 */
@HTTest(runlevel = RunLevel.Smoke,
        email = "chris@hitorro.com",
        description = "Test the execution of SQL scripts.")
public class ExecuteScriptTest extends EnhancedTestCase {

    //  Script filenames (with paths)
    private static final String TEST_SCRIPT_MULTI_STATEMENT = "test_multi_statement.sql";
    private static final String TEST_SCRIPT_INSERT = "test_insert.sql";
    private static final String TEST_SCRIPT_UPDATE = "test_update.sql";
	private static final String TEST_DROP_TABLE= "drop_table.sql";
    private static final String TEST_SCRIPT_CREATE_TABLE = "test_create_table.sql";

    //  Property Names & Values for Variable Resolution
    private static final String PROPERTY_TEST_URL = "test.url";
    private static final String PROPERTY_TEST_TABLE_NAME = "test.table.name";
    private static final String PROPERTY_TEST_UPDATE_SET = "test.update.set";
    private static final String PROPERTY_TEST_INSERT_COLUMNS = "test.insert.columns";
    private static final String PROPERTY_TEST_INSERT_VALUES = "test.insert.values";
    private static final String PROPERTY_TEST_CREATE_TABLE_COLUMNS = "test.create.table.columns";

    private static final String TEST_TABLE_NAME = "xyz_unit_test_table";
    private static final String TEST_HOST = "localhost";
    private static final String TEST_INSERT_COLUMNS = "(fielda, fieldb)";
    private static final String TEST_INSERT_VALUES = "(1, 1000), (2, 2000), (3, 3000), (4, 4000)";
    private static final String TEST_CREATE_TABLE_COLUMNS = "fielda INT NOT NULL, fieldb INT NOT NULL, PRIMARY KEY (fielda)";
    private static final String TEST_UPDATE_SET = "fieldb = 1500 * fielda";

    /**
     * @return HTProperties used to perform Tests
     */
    private HTProperties createProperties() {
        JsonNode connectInfo = null;
        try {
            connectInfo = JVSProperties.getProperties().get(HibernateService.DefaultDBKey.apply());
        } catch (PropaccessError propaccessError) {
            return null;
        }
        String username = HibernateService.UserName.apply(connectInfo);
        String password = HibernateService.Password.apply(connectInfo);
        String url = HibernateService.DBUrl.apply(connectInfo);

        HTProperties properties = new HTProperties();
        properties.setProperty(Script.PROPERTY_USER_NAME, username);
        properties.setProperty(Script.PROPERTY_USER_PASSWORD, password);
        properties.setProperty(Script.PROPERTY_HOST_NAME, TEST_HOST);
        properties.setProperty(PROPERTY_TEST_URL, url);

        return properties;
    }

    /**
     * @param properties HTProperties containing connection information to create an Executor
     * @return new instance of Executor
     */
    private Executor createExecutor(HTProperties properties) {
        String username = properties.get(Script.PROPERTY_USER_NAME);
        String password = properties.get(Script.PROPERTY_USER_PASSWORD);
        String url = properties.get(PROPERTY_TEST_URL);
        return new Executor(username, password, url);
    }


    /**
     * Test a Create Table Script
     *
     * @throws IOException
     * @throws SQLException
     */
    private void runTestCreateTable() throws IOException, SQLException {
        HTProperties properties = createProperties();
        properties.setProperty(PROPERTY_TEST_TABLE_NAME, TEST_TABLE_NAME);
        properties.setProperty(PROPERTY_TEST_CREATE_TABLE_COLUMNS, TEST_CREATE_TABLE_COLUMNS);

        Executor executor = createExecutor(properties);
        Script script = new Script(getInputFileRelative(TEST_SCRIPT_CREATE_TABLE));

        executor.execute(script, properties);
    }


    /**
     * Test a Drop Table Script
     *
     * @throws IOException
     * @throws SQLException
     */
    private void runTestDropTable() throws IOException, SQLException {
        HTProperties properties = createProperties();
        properties.setProperty(Script.PROPERTY_TABLE_NAME, TEST_TABLE_NAME);

        Executor executor = createExecutor(properties);

        Script script = new Script(getInputFileRelative(TEST_DROP_TABLE));

        executor.execute(script, properties);

        //  Check Number of Rows Affected (should be 0)
        String firstResult = getFirstRow(executor.getResults());
        int rowsAffected = Integer.valueOf(firstResult);
        assertTrue(rowsAffected == 0);

    }
	private File getRoot(){
		return Env.getBin();
	}


    /**
     * Test an Insert Script
     *
     * @throws IOException
     * @throws SQLException
     */
    private void runTestInsert() throws IOException, SQLException {
        HTProperties properties = createProperties();
        properties.setProperty(PROPERTY_TEST_TABLE_NAME, TEST_TABLE_NAME);
        properties.setProperty(PROPERTY_TEST_INSERT_COLUMNS, TEST_INSERT_COLUMNS);
        properties.setProperty(PROPERTY_TEST_INSERT_VALUES, TEST_INSERT_VALUES);

        Executor executor = createExecutor(properties);
        Script script = new Script(getInputFileRelative(TEST_SCRIPT_INSERT));

        executor.execute(script, properties);

        //  Check Number of Rows Inserted (should be 4)
        String firstResult = getFirstRow(executor.getResults());
        int rowsInserted = Integer.valueOf(firstResult);
        assertTrue(rowsInserted == 4);

    }


    /**
     * Test an Update Script
     *
     * @throws IOException
     * @throws SQLException
     */
    private void runTestUpdate() throws IOException, SQLException {
        HTProperties properties = createProperties();
        properties.setProperty(PROPERTY_TEST_TABLE_NAME, TEST_TABLE_NAME);
        properties.setProperty(PROPERTY_TEST_UPDATE_SET, TEST_UPDATE_SET);

        Executor executor = createExecutor(properties);
        Script script = new Script(getInputFileRelative(TEST_SCRIPT_UPDATE));

        executor.execute(script, properties);

        //  Check Number of Rows Updated (should be 4)
        String firstResult = getFirstRow(executor.getResults());
        int rowsUpdated = Integer.valueOf(firstResult);
        assertTrue(rowsUpdated == 4);

    }


    /**
     * Test a multi-line script
     *
     * @throws IOException
     * @throws SQLException
     */
    private void runTestMultiLine() throws IOException, SQLException {
        Script script = new Script(getInputFileRelative(TEST_SCRIPT_MULTI_STATEMENT));
        HTProperties properties = createProperties();
        Executor executor = createExecutor(properties);

        executor.execute(script);

        //  Verify that 10 statements were executed
        List<Result> results = executor.getResults();
        assertNotNull(results);
        assertTrue(results.size() == 10);
    }


    /**
     * Test execution of different scripts
     */
    public void testExecuteScript() {

        String msg = "";

        try {

            //  Test DROP TABLE
            msg = "DROP TABLE Script (to clean up table from previous test run)";
            runTestDropTable();

            //  Test CREATE TABLE
            msg = "CREATE TABLE Script";
            runTestCreateTable();

            //  Test INSERT
            msg = "INSERT Script";
            runTestInsert();

            //  Test UPDATE
            msg = "UPDATE Script";
            runTestUpdate();

            //  Test DROP TABLE
            msg = "DROP TABLE Script";
            runTestDropTable();

            //  Test Multi-Line Script (CREATES, INSERTS, SELECTS, UPDATES & DROPS)
            msg = "Multi-Statement Script";
            runTestMultiLine();

        } catch (IOException e1) {
            assertAlways("IO Exception occurred reading script:" + msg + "\r\n" + e1.getMessage());
        } catch (SQLException e2) {
            assertAlways("Database Exception while executing script: " + msg + "\r\n" + e2.getMessage());
        }

    }

    /**
     * @param results List of Results returned by Executor
     * @return the first row of the results of the test execution
     */
    private String getFirstRow(List<Result> results) {
        //  Get Results
        assertNotNull(results);
        assertTrue(results.size() > 0);

        //  Get Table within first Result
        Result r = results.get(0);
        List<ResultTable> tables = r.getResults();
        ResultTable t = tables.get(0);
        assertNotNull(t);

        //  Determine if we get a single row returned containing the number of rows inserted
        List<String> rows = t.getRows();
        assertNotNull(rows);
        assertTrue(rows.size() > 0);

        return rows.get(0);
    }

}
