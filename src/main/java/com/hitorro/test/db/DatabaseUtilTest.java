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
import com.hitorro.basedms.db.DatabaseUtil;
import com.hitorro.jsontypesystem.JVS;
import com.hitorro.util.core.Log;
import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.json.keys.propaccess.PropaccessError;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;

import java.io.IOException;
import java.sql.SQLException;

@HTTest(runlevel = RunLevel.Smoke,
        email = "chris@hitorro.com",
        description = "Verfiy DatabaseUtil correctly wraps Executor")
public class DatabaseUtilTest extends EnhancedTestCase {
    private static final String s_testHost = "localhost";
    private static final String s_testDatabase = "DatabaseUtilTest";


    public void testCreateDBParams() {
        /*   get default database connection properties apply.   */
        JVS defaultDbProperties = null;
        JsonNode defaultInfo = null;
        try {
            defaultDbProperties = DatabaseUtil.getDefaultDBContext();
            defaultInfo = DatabaseUtil.getConnectionInfoMap(defaultDbProperties);
            /*   build out database connection variables from connection properties apply.  */
            String username = DatabaseUtil.UsernameKey.apply(defaultInfo);
            String password = DatabaseUtil.PasswordKey.apply(defaultInfo);
            String databaseUrl = DatabaseUtil.DatabaseUrlKey.apply(defaultInfo);
            String hostName = DatabaseUtil.HostNameKey.apply(defaultInfo);
            String databaseName = DatabaseUtil.DatabaseNameKey.apply(defaultInfo);
            String rootUsername = DatabaseUtil.RootUsernameKey.apply(defaultInfo);
            String rootPassword = DatabaseUtil.RootPasswordKey.apply(defaultInfo);
            String rootDBUrl = DatabaseUtil.RootDatabaseUrlKey.apply(defaultInfo);

            /*    manually construct a new connection properties & get connection properties apply.   */
            JVS dbProperties = DatabaseUtil.getDBContext(username, password, hostName, databaseName, rootUsername, rootPassword);
            JsonNode dbInfo = DatabaseUtil.getConnectionInfoMap(dbProperties);

            assertTrue("default db properties apply same as constructed db properties apply", dbInfo.equals(defaultInfo));
        } catch (PropaccessError propaccessError) {
            fail("could not create db properties");
        }
    }


    public void testCanUserConnect() throws PropaccessError {
        JVS defaultDbProperties = DatabaseUtil.getDefaultDBContext();

        /*   verify user and rootuser defined in connection properties apply can connect to database.  */
        boolean userConnected = DatabaseUtil.canUserConnect(defaultDbProperties);

        assertTrue("default user can connect", userConnected);

    }

    public void t_estCanRootUserConnect() throws PropaccessError {
        JVS defaultDbProperties = DatabaseUtil.getDefaultDBContext();

        /*   verify user and rootuser defined in connection properties apply can connect to database.  */

        boolean rootUserConnected = DatabaseUtil.canRootUserConnect(defaultDbProperties);

        assertTrue("root user can connect", rootUserConnected);
    }

    /**
     * For now disable this test as it requires open credentials on root db account
     */
    public void t_estCreateDestroyDatabase() {
        try {
            boolean dbCreated = false;
            boolean userCanConnect = false;
            boolean dbDestroyed = false;
            boolean userCreated = false;


            JVS defaultDBProperties = DatabaseUtil.getDefaultDBContext();
            JsonNode connectInfo = DatabaseUtil.getConnectionInfoMap(defaultDBProperties);

            /*   create a database.  */
            dbCreated = DatabaseUtil.createDatabase(defaultDBProperties, s_testDatabase);
            assertTrue(Fmt.S("database created: %s", s_testDatabase), dbCreated);

            /*   connect to the newly created database.   */
            if (dbCreated) {
                String username = DatabaseUtil.UsernameKey.apply(connectInfo);

                defaultDBProperties = DatabaseUtil.getDefaultDBContext();
                userCanConnect = DatabaseUtil.canUserConnect(defaultDBProperties);
                assertTrue(Fmt.S("database created: %s; default user can connect: %s", s_testDatabase, username), userCanConnect);
            }

            /*   create a user in the newly created database.   connect as that user.  */
            if (dbCreated && userCanConnect) {
                String username = s_testDatabase;
                String password = DatabaseUtil.PasswordKey.apply(connectInfo);
                String hostname = DatabaseUtil.HostNameKey.apply(connectInfo);
                String url = DatabaseUtil.getDatabaseUrl(hostname, s_testDatabase);

                userCreated = DatabaseUtil.createUser(defaultDBProperties, username, password);
                userCanConnect = DatabaseUtil.canUserConnect(username, password, url);
                assertTrue(Fmt.S("user created: %s", username), userCreated);
                assertTrue(Fmt.S("created user: %s can connect to database: %s", username, s_testDatabase), userCanConnect);
            }

            /*  destroy the newly created database.  */
            if (dbCreated && userCanConnect) {
                String username = s_testDatabase;
                String password = DatabaseUtil.PasswordKey.apply(connectInfo);
                String hostname = DatabaseUtil.HostNameKey.apply(connectInfo);
                String url = DatabaseUtil.getDatabaseUrl(hostname, s_testDatabase);
                dbDestroyed = DatabaseUtil.dropDatabase(defaultDBProperties, s_testDatabase);

                /*   database destroyed, user exists, but should not be able to connnect to database.  */
                userCanConnect = DatabaseUtil.canUserConnect(username, password, url);
                assertFalse(Fmt.S("created user: %s can not connect to database: %s", username, s_testDatabase), userCanConnect);
            }
        } catch (IOException e) {
            fail(Fmt.S("failure creating a database: %s; creating a user; destroying the database", s_testDatabase));
            Log.test.error("failure creating a database; creating a user; destroying the database %s %e", e, e);
        } catch (SQLException e) {
            fail(Fmt.S("failure creating a database: %s; creating a user; destroying the database", e));
            Log.test.error("failure creating a database; creating a user; destroying the database %s %e", e, e);
        } catch (PropaccessError propaccessError) {
            propaccessError.printStackTrace();
        }
    }
}
