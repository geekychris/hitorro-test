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
package com.hitorro.test;

import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;

/**
 * Copyright (c) 2003 - present HiTorro All rights reserved.
 * <p/>
 * User: chris
 */
@HTTest(runlevel = RunLevel.Smoke,
        email = "chris@hitorro.com",
        description = "Test basic authentication")
public class AuthenticationTest extends EnhancedTestCase {
    public void testTokens() {
        com.hitorro.util.auth.AuthenticationService service = com.hitorro.util.auth.AuthenticationService.getService();
        assertNotNull(service);

        com.hitorro.util.auth.AuthenticationMethod method = service.getMethod(com.hitorro.util.auth.TokenAuthenticationMethodImpl.Key);
        assertNotNull(method);

        assertTrue(method.isTokenGenerating());
        com.hitorro.util.auth.TokenAuthenticationMethod m = (com.hitorro.util.auth.TokenAuthenticationMethod) method;
        String token = m.getToken("chris");
        assertNotNull(token);

        assertTrue(m.authenticate("chris", token).getState() == com.hitorro.util.auth.AuthResponse.AuthResponseState.Valid);

        assertFalse(m.authenticate("bob", token).getState() == com.hitorro.util.auth.AuthResponse.AuthResponseState.Valid);

        // now try with method specified by auth method
        assertTrue(service.authenticateUser("token:chris", token).getState() == com.hitorro.util.auth.AuthResponse.AuthResponseState.Valid);

        m.revokeToken("chris", token);

        assertFalse(m.authenticate("chris", token).getState() == com.hitorro.util.auth.AuthResponse.AuthResponseState.Valid);
    }
}
