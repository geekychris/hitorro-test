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
import com.hitorro.jsontypesystem.propreaders.JVSProperties;
import com.hitorro.util.json.keys.propaccess.PropaccessError;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved. User: Chris Date: Nov 16, 2006 Time: 5:17:54 PM
 * <p/>
 * Test Database Connectivity
 */

@HTTest(runlevel = RunLevel.Smoke,
        email = "chris@hitorro.com",
        description = "Test database connectivity")
public class ConnectionTest extends EnhancedTestCase {
    private static final String EXCEPTION_ON_CONNECTION_CLOSE = "Exception occurred when closing Connection.";

    /**
     * Test Database Connection
     */
    public void testConnection() throws PropaccessError {
        JsonNode connectInfo = JVSProperties.getProperties().get(HibernateService.DefaultDBKey.apply());
        String username = HibernateService.UserName.apply(connectInfo);
        String password = HibernateService.Password.apply(connectInfo);
        String url = HibernateService.DBUrl.apply(connectInfo);

        Connection connection = null;

        try {
            connection = Executor.newConnection(username, password, url);
        } catch (SQLException e) {
            connection = null;
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                assertAlways(EXCEPTION_ON_CONNECTION_CLOSE);
            }
        }

        assertNotNull(connection);

    }


}
