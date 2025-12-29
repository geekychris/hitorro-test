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
package com.hitorro.test.typesystem;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.basedms.db.DatabaseUtil;
import com.hitorro.jsontypesystem.propreaders.JVSProperties;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.json.keys.StringProperty;
import com.hitorro.util.json.keys.propaccess.PropaccessError;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;
import com.hitorro.util.testframework.TestPlus;

import java.io.File;


@HTTest(runlevel = RunLevel.Never,
        email = "chris@hitorro.com",
        description = "Dump and load tests")
public abstract class DumpLoadTest extends EnhancedTestCase {
    /*   build version; invoking testcase properties  */
    protected static final StringProperty m_BuildVersionKey = new StringProperty("build.version", "default build version", null);
    protected final StringProperty m_invokingClassKey = new StringProperty("test.invokingclass", "class that invokes LoadLoadTest", null);
    /*   testcase-specific files   */
    protected File m_dumpFile;
    protected File m_resultsProperties;
    protected File m_canonDumpFile;
    protected File m_canonResultsProperties;
    /*   track the class 'responsible' for the testcase.   for DumpLoadTest, 'this' is not always sufficient.  */
    protected String m_classSimpleName;
    protected Class m_class;
    protected String m_invokingClass = m_invokingClassKey.apply();
    protected JsonNode m_connectInfo;


    /* jdbc database connection properties  */
    protected String m_rootUsername;
    protected String m_rootPassword;
    protected String m_url;
    /*   load utility classes     */
    TypeSystemTestUtil typeSystemTestUtil = new TypeSystemTestUtil();

    /**
     * define various files and filepaths for dump / load / loadload, regardless the actual method (test) that gets
     * run.
     */
    public void setUp() {
        /**
         *   which class is the relevant one for constructing paths?  if 'dump' or 'load' test, then their own classes.
         *   if it is the 'loadload' test exec'd via 'load' in its own process to do the actual load
         *   then derive the path from a property passed from 'load' to 'loadload'.  Otherwise all dumpfile
         *   loads will be from same path...which is bad.  hokey inter-process communication.
         */

        try {
            m_connectInfo = DatabaseUtil.getConnectionInfoMap(JVSProperties.getProperties());
            m_rootUsername = DatabaseUtil.RootUsernameKey.apply(m_connectInfo);
            m_rootPassword = DatabaseUtil.RootPasswordKey.apply(m_connectInfo);
            m_url = DatabaseUtil.DatabaseUrlKey.apply(m_connectInfo);
        } catch (PropaccessError e) {
            fail(Fmt.S("Unable to initalize parameters %s", e));
        }
        if (!StringUtil.nullOrEmptyOrBlankString(m_invokingClass)) {
            try {
                m_class = Class.forName(m_invokingClass);
            } catch (ClassNotFoundException e) {
                Log.test.debug(Fmt.S("invoking class parameter does not resolve to actual class. "));
                fail(Fmt.S("invoking 'Load' testcase failed to pass via process args a valid class.  Passed %s", m_invokingClass));
            }
        } else {
            m_class = this.getClass();
        }

        m_classSimpleName = m_class.getSimpleName();

        /*   clean up outputdir   */
        File outputDir = new File(TestPlus.getOutputTreeRoot(), TestPlus.getClassPathAsPath(m_class, true));
        //FileUtil.deleteDirectoryContent(outputDir, true);
        outputDir.mkdirs();

        /*   define output dumpfile name.   */
        String dumpFilename = StringUtil.strcat(File.separator, m_classSimpleName, ".v", m_BuildVersionKey.apply(), ".dmp");
        m_dumpFile = new File(outputDir, dumpFilename);

        /*   define output query results properties file.   */
        String resultsFilename = StringUtil.strcat(File.separator, m_classSimpleName, ".properties");
        m_resultsProperties = new File(outputDir, resultsFilename);

        /*   define canonical input dumpfile name   */
        File inputDir = new File(getInputTreeRoot(), TestPlus.getClassPathAsPath(m_class, true));
        String canonFilename = StringUtil.strcat(File.separator, m_classSimpleName, ".v", m_BuildVersionKey.apply(), ".canon.dmp");
        m_canonDumpFile = new File(inputDir, canonFilename);

        /*   define canonical query results properties file.   */
        String canonResultsFilename = StringUtil.strcat(File.separator, m_classSimpleName, "canon.properties");
        m_canonResultsProperties = new File(inputDir, canonResultsFilename);
    }


    /**
     * dump to a dumpfile objects from cms.  dumpfile derived from OutputTreeRoot and current classname.
     */
    public void testDump() {
        int counter = typeSystemTestUtil.dump(m_dumpFile);

        if (counter == TypeSystemTestUtil.DumpSatusFailure) {
            fail(Fmt.S("typesystem failure to dump to file %s", m_dumpFile.toString()));
        }
    }


    /**
     * verify the integrity of the generated dumpfile.
     */
    public abstract void verifyDump();


    /**
     * load a dumpfile into cms.   Path and filename constructed from InputTreeRoot and current classname.
     */
    public void testLoad() {
        String hostname = DatabaseUtil.getHostFromUrl(m_url);
        String databaseName = this.getClass().getSimpleName();
        String url = DatabaseUtil.getDatabaseUrl(hostname, databaseName);

        /*   main load call   */
        int results = typeSystemTestUtil.load(m_rootUsername, m_rootPassword, url, this.getClass());

        if (results != TypeSystemTestUtil.LoadStatusSuccess) {
            fail(Fmt.S("exception while performing load operation.  expected: %s.  actual: %s.", TypeSystemTestUtil.LoadStatusSuccess, results));
        }
    }


    /**
     * testcase performs actual load of file into a new server/dbms database.   'Load' testcase exec's a new process
     * that in turn runs a DumpLoad testcase and drop into testLoadLoad testcase.
     */
    public void testLoadLoad() {
        boolean results = typeSystemTestUtil.loadLoad(m_canonDumpFile);

        if (!results) {
            fail(Fmt.S("failure to load dumpfile %s", m_canonDumpFile));
        }
    }


    /**
     * verify the integrity of the load of the dumpfile.
     */
    public abstract void verifyLoad();


    public void tearDown() {
        FileUtil.delete(TypeSystemTestUtil.DestroyDBSentinelFile.toString());
    }
}
