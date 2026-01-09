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
package com.hitorro.test.dms;

import com.hitorro.base.objects.PersistedSerializedObject;
import com.hitorro.basedms.session.DMSSessionFactory;
import com.hitorro.util.core.UTCDateUtil;
import com.hitorro.util.io.FileUtil;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Date;

/**
 */
@HTTest(runlevel = RunLevel.Smoke,
        email = "chris@hitorro.com",
        description = "Test serialization mechanism")
public class HTSerializableTest extends EnhancedTestCase {
    /**
     * Test the serialization and deserialization of two non persisted objects of type NonPersistObject.  These objects
     * point at each other and also have a string, date, boolean.  We attempt to serialize both objects.  This test
     * confirms we can:
     * <p/>
     * - serialize and deserialize a non persisted object - exercises the typemanager factory. - ensures that circular
     * references are handled correctly. - ensures that prior references are handled correctly (reference not
     * serialization of same object)
     */
    public void testNonPersistentObject() {
        com.hitorro.util.typesystem.TypeManager.getTypeManager().addTypeIfAbsent(NonPersistObject.class);
        File f = this.getOutputFileRelative("test.ser");
        Date datea = new Date();
        Date dateb = UTCDateUtil.getDateFor(1971, 3, 2);
        int numberA = 310687;
        int numberB = 1234;
        String stringA = "bla a";
        String stringB = "bla b";
        NonPersistObject a = new NonPersistObject(stringA, datea, numberA, false);
        NonPersistObject b = new NonPersistObject(stringB, dateb, numberB, true);
        a.set(b);
        b.set(a);
        OutputStream oos = null;
        try {
            com.hitorro.util.typesystem.BaseSession session = DMSSessionFactory.getFactory().getSession();
            oos = FileUtil.getBufferedFileOutputStream(f);
            com.hitorro.util.typesystem.HTObjectOutputStream os = new com.hitorro.util.typesystem.HTObjectOutputStreamImpl(session, oos,
                    com.hitorro.util.typesystem.TypeManager.getTypeManager(),
                    false);
            os.writeVersionedObject(a);
            os.writeVersionedObject(b);
            oos.flush();
            oos.close();


            InputStream bis = FileUtil.getBufferedFileInputStream(f);
            com.hitorro.util.typesystem.HTObjectInputStream is = new com.hitorro.util.typesystem.HTObjectInputStreamImpl(bis,
                    com.hitorro.util.typesystem.TypeManager.getTypeManager(),
                    session);
            NonPersistObject aLoad = (NonPersistObject) is.readVersionedObject();
            NonPersistObject bLoad = (NonPersistObject) is.readVersionedObject();
            assertEquals(a.m_string, aLoad.m_string);
            assertEquals(b.m_string, bLoad.m_string);

            assertEquals(datea, a.m_date);
            assertEquals(numberA, a.int_);
            assertEquals(stringA, a.m_string);
            assertEquals(dateb, b.m_date);
            assertEquals(numberB, b.int_);
            assertEquals(stringB, b.m_string);

            NonPersistObject bLoadPrime = aLoad.m_ref;
            assertEquals(bLoad, bLoadPrime);
            bis.close();

        } catch (IOException e) {
            assertAlways(e.getMessage());
        } catch (ClassNotFoundException e) {
            assertAlways(e.getMessage());
        } catch (StoreException e) {
            assertAlways(e.getMessage());
        }
    }

    public void testPersistedSerializedObject() {
        //make sure this type is in the type system.
        com.hitorro.util.typesystem.TypeManager.getTypeManager().addType(NonPersistObject.class);
        com.hitorro.util.typesystem.BaseSession session = DMSSessionFactory.getFactory().getSession();
        Date datea = new Date();
        Date dateb = UTCDateUtil.getDateFor(1971, 3, 2);
        int numberA = 310687;
        int numberB = 1234;
        String stringA = "bla a";
        String stringB = "bla b";
        NonPersistObject a = new NonPersistObject(stringA, datea, numberA, false);
        NonPersistObject b = new NonPersistObject(stringB, dateb, numberB, true);
        a.set(b);
        b.set(a);
        PersistedSerializedObject pso = new PersistedSerializedObject();
        pso.setName("PSO Test 1");
        try {
            pso.setSerializableObject(a, session);
            session.persist(pso);
            session.commit();
            DMSSessionFactory.getFactory().commitAndCloseSession(session);
            String guid = pso.getGuid();
            com.hitorro.util.typesystem.BaseSession s2 = DMSSessionFactory.getFactory().getSession();
            PersistedSerializedObject pos2 = (PersistedSerializedObject) s2.getObjectFromGuid(guid);
            assertNotNull(pos2);
            NonPersistObject npo2 = (NonPersistObject) pos2.getSerializableObject(s2);
            assertNotNull(npo2);
            assertEquals(a.m_date, npo2.m_date);
            assertEquals(a.m_string, npo2.m_string);
            assertEquals(a.int_, npo2.int_);
            DMSSessionFactory.getFactory().rollbackCloseSession(s2);
        } catch (IOException e) {
            this.assertAlways("Unable to persist PSO %s %e", e, e);
        } catch (StoreException e) {
            this.assertAlways("Unable to persist PSO %s %e", e, e);
        } catch (SQLException e) {
            this.assertAlways("Unable to persist PSO %s %e", e, e);
        } catch (ClassNotFoundException e) {
            this.assertAlways("Unable to persist PSO %s %e", e, e);
        }
    }
}
/*
We've got stars directing our fate
And we're praying it's not too late
Millennium

Some say that we are players
Some say that we are pawns
But we've been making money
Since the day that we were born
Got to slow down

Before we fall down

We've got stars directing our fate
And we're praying it's not too late
'Cause we know we're falling from grace
Millennium

Live for liposuction
Detox for your rent
Overdose for Christmas
Give it up for lent
My friends are all so cynical
Refuse to keep the faith
We all enjoy the madness
'Cause we know we're going to fade away

We've got stars directing our fate
And we're praying it's not too late
'Coz we know we're falling from grace
Millennium

Come and have a go if you think you are hard enough
Come and have a go if you think you are hard enough
Millennium
Millennium

We've got stars directing our fate
And we're praying it's not too late
'Coz we know we're falling from grace
Millennium

And when we come we always come too late
I have to think that we were born to hate
Get up and see the sarcasm in my eyes

And when we come we always come too late
I have to think that we were born to hate
Get up and see the sarcasm in my eyes

We've got stars directing our fate
Millennium
And we're praying it's not too late
Millennium
'Cause we know we're falling from grace
Millennium

And we won't stop

We've got stars directing our fate
And we're praying it's not too late
'Cause we know we're falling from grace (to fade)

-- Robbie Williams (Good old Stokey!) Millenium
 */

