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
import com.hitorro.base.typesystem.Load;
import com.hitorro.base.typesystem.commands.DumpContext;
import com.hitorro.basedms.db.DatabaseUtil;
import com.hitorro.jsontypesystem.JVS;
import com.hitorro.util.cmdline.CommandLine;
import com.hitorro.util.commandandcontrol.LogResponse;
import com.hitorro.util.core.Env;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.Platform;
import com.hitorro.util.core.map.MapUtil;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.core.string.StringUtil;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.json.keys.propaccess.PropaccessError;
import com.hitorro.util.osprocessexec.ExecContext;
import com.hitorro.util.osprocessexec.JavaExecWrapper;
import com.hitorro.util.osprocessexec.TerminationKey;
import com.hitorro.util.startupframework.HTServer;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.RunLevel;
import com.hitorro.util.testframework.TestPlus;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class TypeSystemTestUtil implements TerminationKey {
    public static final int DumpSatusFailure = -1;
    public static final int LoadStatusFailure = -1;
    public static final int LoadStatusSuccess = 1;
    private static final String DestroyDBFilename = "destroyDB.txt";
    public static File DestroyDBSentinelFile = new File(Env.getHome().toString(), DestroyDBFilename);
    /*   spawned HTServer process related constants   */
    private static final int ExecProcessWaitTime = 1000 * 60 * 3;        /*  3 minutes.  */
    private static final int ExecProcessNodeID = 666;
    private static final int ExecProcessBaseSocket = 7000;
    private static TypeSystemTestUtil s_TypeSystemTestUtil = null;
    private static int ExecProcessSocketOffset = 666;

    /*   spawned HTServer process compContext-related callback variables   */
    private boolean m_execDone = false;
    private int execExitCode;


    /**
     * Singleton and lazy instantiation of unit test utility class.
     *
     * @return lazy instance of TypeSystemTestUtil
     */
    public static TypeSystemTestUtil getTypeSystemTestUtil() {
        if (s_TypeSystemTestUtil == null) {
            s_TypeSystemTestUtil = new TypeSystemTestUtil();
        }

        return s_TypeSystemTestUtil;
    }

    /**
     * Creates process compContext to be used when a process gets exec'd via ExecContext.
     * <p/>
     * use async callback mechanism for execing a process, specifically a junit test, ht.test.typesystem.LoadLoadTest,
     * to actually accomplish the load.
     *
     * @param dbUsername    - root username to connect to dbms
     * @param dbPassword    - password associated with root user
     * @param dbUrl         - url to make connecto to dbms, including host and databasename.
     * @param invokingClass - pass in class (or other) from which target, LoadLoad class derives filenames, pathnames.
     *
     * @return JavaExecWrapper that services a process compContext.
     */
    private static JavaExecWrapper getLoadExecWrapper(String dbUsername, String dbPassword, String dbUrl, Class invokingClass) {
        Map<String, String> args = new HashMap<String, String>();

        args.put("command", HTServer.class.getName());
        args.put("servertype", "test");                                                                            /*  force HT_HOME of new server into testcase output files dir.  no overlay of running instance.  */
        args.put("dbinit", "false");
        args.put("autodbcreate", "true");
        args.put("node", Integer.toString(ExecProcessNodeID));
        args.put("level", RunLevel.Smoke.toString());
        args.put("test", LoadLoadTest.class.getName());                                                              /*   hard-coded for now.  not going to change.  */
        args.put("defaultdb.username", dbUsername);
        args.put("defaultdb.password", dbPassword);
        args.put("defaultdb.url", dbUrl);
        args.put("defaulthttp.port", Integer.toString(ExecProcessBaseSocket + ExecProcessSocketOffset++));           /*   avoid socket collisions.   */
        args.put("telnetport", Integer.toString(ExecProcessBaseSocket + ExecProcessSocketOffset++));                 /*   avoid socket collisions.   */
        args.put("rpcport", Integer.toString(ExecProcessBaseSocket + ExecProcessSocketOffset++));                    /*   avoid socket collisions.   */
        args.put("test.invokingclass", invokingClass.getName());                                                     /*   pass class name across running processes to include in file paths. ipc hack.  */


        JavaExecWrapper execWrapper = new JavaExecWrapper(Env.getBin(),
                Env.getHome(),
                Platform.getPlatform().getJavaRoot(),
                CommandLine.class.getCanonicalName(),
                new ArrayList<String>(),
                args);
        return execWrapper;
    }

    /**
     * Serialize hibernate orms-based objects in running cms instance and write them to a file.
     *
     * @param dumpFile - name of file to write serialized cms orms objects to.
     *
     * @return count of object serialized and written to the dumpfile.
     */
    public final int dump(File dumpFile) {
        DumpContext dc = DumpContext.getDumpContextForUpgrade(dumpFile);

        int counter = DumpSatusFailure;
        try {
            counter = dc.dump();
            Log.test.info("wrote %s root level objects", Integer.toString(counter));
        } catch (IOException e) {
            Log.test.error("Unable to create dump file %s %s %e", dumpFile.toString(), e, e);
        }

        return counter;
    }

    /**
     * load the contents of a cms-generated dumpfile of hibernate orms-based, serialized objects.   execs a HTServer in
     * a new process that in turn will run the unittest framework, specifically the LoadLoad.
     *
     * @param dbUsername    - root username to connect to dbms
     * @param dbPassword    - password associated with root user
     * @param dbUrl         - url to make connecto to dbms, including host and databasename.
     * @param invokingClass - pass 'invoking' Load testcase from unit testcase to TypeSystemTestUtil over to invoked
     *                      LoadLoad testcase. Used to construct unique pathnames and filenames.
     *
     * @return process exit code.
     */
    public final int load(String dbUsername, String dbPassword, String dbUrl, Class invokingClass) {
        int exitCode = LoadStatusFailure;

        /*  derive output filepathname.   */
        File outputFilePath = TestPlus.getOutputFileRelativeForClass("", invokingClass, true);
        String className = invokingClass.getSimpleName();

        try {
            boolean initialized = initializeDB(dbUsername, dbPassword, dbUrl);

            JavaExecWrapper ew = getLoadExecWrapper(dbUsername, dbPassword, dbUrl, invokingClass);

            Map<String, String> map = new HashMap<String, String>();
            String ptHomeDir = new File(TestPlus.getOutputTreeRoot(), TestPlus.getClassPathAsPath(invokingClass, true)).toString();

            map.put("HT_HOME", ptHomeDir);
            map.put("HT_BIN", Env.getBin().toString());
            String args[] = MapUtil.getMapAsArgsArray(map);

            ExecContext ec = new ExecContext(this);
            ec.setOutputFile(new File(StringUtil.strcat(outputFilePath, File.separator, className, ".out.txt")));
            ec.setErrorFile(new File(StringUtil.strcat(outputFilePath, File.separator, className, ".err.txt")));
            ec.setProgram(ew.getExecString());
            ec.setArgs(args);

            /*   spawn a new process that will initialize the test database and run testLoadLoad().   */
            try {
                exitCode = ec.execAndWait(ExecProcessWaitTime);

                if ((exitCode != LoadStatusSuccess) ||
                        (m_execDone != true) ||
                        (execExitCode != LoadStatusSuccess)) {
                    Log.test.error("load failure.  exitCode = %s; execDone = %s", Integer.toString(exitCode), Boolean.toString(m_execDone));
                }
            } catch (InterruptedException e) {
                Log.test.error("process timeout while performing load operation %s %e", e, e);
            }
        } catch (FileNotFoundException e) {

            Log.test.error("output filepath, %s, not found. %s %e", outputFilePath.toString(), e, e);
        } catch (IOException e) {
            Log.test.error("error writing out %s, not found. %s %e", outputFilePath.toString(), e, e);
        }

        return exitCode;
    }

    /**
     * handle underlying database dump file will get loaded into.  drop if exists.  create.
     *
     * @param dbUsername - root username to connect to dbms
     * @param dbPassword - password associated with root user
     * @param dbUrl      - url to make connecto to dbms, including host and databasename.
     *
     * @return True - if database dropped (if it exists) and recreated.  False - if error database dropping/creation.
     */
    private boolean initializeDB(String dbUsername, String dbPassword, String dbUrl) {
        boolean results = false;


        if (DatabaseUtil.canUserConnect(dbUsername, dbPassword, dbUrl)) {
            /*   existing database   */
            results = markDBForDeletion();
            Log.test.debug(Fmt.S("database %s exists.  marking for deletion.", dbUrl));

        } else {
            /*   new database   */
            JVS properties = null;
            JsonNode connectInfo = null;
            try {
                properties = DatabaseUtil.getDefaultDBContext();
                connectInfo = DatabaseUtil.getConnectionInfoMap(properties);
            } catch (PropaccessError propaccessError) {
                Log.test.error("Unable to create database %s %e", propaccessError, propaccessError);
            }

            String rootUsername = DatabaseUtil.RootUsernameKey.apply(connectInfo);
            String rootPassword = DatabaseUtil.RootPasswordKey.apply(connectInfo);
            String rootUrl = DatabaseUtil.RootDatabaseUrlKey.apply(connectInfo);
            String databaseName = DatabaseUtil.getDatabaseFromUrl(dbUrl);

            try {
                boolean databaseCreated = DatabaseUtil.createDatabase(rootUsername, rootPassword, rootUrl, databaseName);

                if (!databaseCreated) {

                    Log.test.error("Unable to create database %s", databaseName);
                }
            } catch (IOException e) {
                Log.test.error("Unable to create database %s %s %e", databaseName, e, e);
            } catch (SQLException e) {
                Log.test.error("Unable to create database %s %s %e", databaseName, e, e);
            }

            Log.test.debug(Fmt.S("database %s does not exist.  creating.", dbUrl));
        }

        return results;
    }

    /**
     * load the contents of a cms-generated dumpfile of hibernate orms-based, serialized objects.
     *
     * @param dumpFile - file pathname of dumpfile.
     *
     * @return True - if load suceeded cleanly.  False - if there was a total failure or failure in loading individual
     *         objects.
     */
    public final boolean loadLoad(File dumpFile) {
        Load load = new Load();
        String rawValue = "";
        LogResponse response = new LogResponse(Log.test);
        JVS args = new JVS();

        boolean results = false;
        try {
            args.set("file", dumpFile.toString());
            args.set("includecontent", "true");
            results = load.execute(rawValue, args.getJsonNode(), response, null);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        Log.test.debug(Fmt.S("dumpfile load results: %s; for file %s", Boolean.toString(results), dumpFile.toString()));

        return results;
    }

    /**
     * Write a marker file so that a Server, when starting, will initialize its database.
     *
     * @return True - if marker file properly created.  False - if error creating file.
     */
    private boolean markDBForDeletion() {
        /*   create destroydb file.  triggers db destruction on init.  */
        final String destroyDBFilename = "destroyDB.txt";
        File destroyDBSentinelFile = new File(Env.getHome().toString(), destroyDBFilename);


        if (destroyDBSentinelFile.exists()) {
            FileUtil.delete(destroyDBSentinelFile.toString());
        }

        try {
            FileUtil.writeToFile(destroyDBSentinelFile, Long.toString(System.currentTimeMillis()));
        } catch (java.io.FileNotFoundException e) {
            Log.test.error("Unable to create db deletion sentinel file %s %s %e", destroyDBSentinelFile.toString(), e, e);
        } catch (java.io.IOException e) {
            Log.test.error("Unable to create db deletion sentinel file %s %s %e", destroyDBSentinelFile.toString(), e, e);
        }

        return destroyDBSentinelFile.exists();
    }

    /**
     * TerminationKey interface, and process 'exec' callback mechanism.
     */
    public void complete(int exitCode) {
        m_execDone = true;
        execExitCode = exitCode;
    }

}
