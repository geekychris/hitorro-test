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

import com.hitorro.base.objects.Document;
import com.hitorro.base.objects.User;
import com.hitorro.base.objects.VersionableObject;
import com.hitorro.util.core.classes.ClassUtil;
import com.hitorro.util.testframework.EnhancedTestCase;
import com.hitorro.util.testframework.HTTest;
import com.hitorro.util.testframework.RunLevel;
import com.hitorro.util.typesystem.annotation.TypeClassMetaInfo;
import com.hitorro.util.typesystem.annotation.ViewClassReference;
import com.hitorro.util.typesystem.constraint.IsFullTextable;

import java.util.List;

/**
 */
@HTTest(runlevel = RunLevel.Smoke,
        email = "chris@hitorro.com",
        description = "Type system tests")
public class TypeSystemTest extends EnhancedTestCase {
    public void testGetGetterReturnType() {
        com.hitorro.util.typesystem.Type t = com.hitorro.util.typesystem.TypeManager.getTypeManager().getTypeForClass(Document.class);
        assertNotNull(t);
        com.hitorro.util.typesystem.TypeFieldIntf tf = t.getField("nextVersion");
        assertNotNull(tf);
        Class c = tf.getReturnType();
        assertEquals(c, VersionableObject.class);
    }

    public void testTypes() {
        Document d = new Document();
        // within a test server we do not want to put the Document type again, but standalone it is needed
        //TypeManager.getTypeManager().addType(Document.class);
        com.hitorro.util.typesystem.TypeIntf t = com.hitorro.util.typesystem.TypeManager.getTypeManager().getTypeForBaseType(d);
        assertNotNull("Type not found for Document", t);
        TypeClassMetaInfo meta = t.getTypeMeta();
        assertNotNull("Type meta not found for document", meta);
        assertEquals(meta.shortTypeName(), TypeClassMetaInfo.Document);
        List<com.hitorro.util.typesystem.TypeFieldIntf> fields = t.getTypeFieldsByConstraint(new IsFullTextable());
        if (fields.size() == 0) {
            assertAlways("Expected at least one indexable field");
        }
    }

    public void testTypesViews() {
        User d = new User();
        // within a test server we do not want to put the Document type again, but standalone it is needed
        //TypeManager.getTypeManager().addType(Document.class);
        com.hitorro.util.typesystem.TypeIntf t = com.hitorro.util.typesystem.TypeManager.getTypeManager().getTypeForBaseType(d);
        com.hitorro.util.typesystem.TypeIntf view = t.getView(ViewClassReference.ListView);
        assertNotNull("User should have a blog view view", view);
        assertTrue(view.isView());
    }

    /**
     * Test to make sure that all persisted classes have the required structure. We require each persisted class to
     * implement serialize, deserialize and copy themselves.
     */
    public void testClassStructure() {

        // get all the persisted classes
        com.hitorro.util.typesystem.TypeManager manager = com.hitorro.util.typesystem.TypeManager.getTypeManager();
        List<com.hitorro.util.typesystem.Type> types = manager.getListOfPersistedTypes();

        // the parameters to our expected methods
        Class sysObjClass = VersionableObject.class;
        Class outputClass = com.hitorro.util.typesystem.HTObjectOutputStream.class;
        Class inputClass = com.hitorro.util.typesystem.HTObjectInputStream.class;
        Class[] copyArgs = new Class[1];
        copyArgs[0] = sysObjClass;
        Class[] serializeArgs = new Class[1];
        serializeArgs[0] = outputClass;
        Class[] deserializeArgs = new Class[2];
        deserializeArgs[0] = inputClass;
        deserializeArgs[1] = Integer.TYPE;

        for (com.hitorro.util.typesystem.Type tt : types) {
            Class javac = tt.getClass();
            if (javac == com.hitorro.util.typesystem.Type.class) {
                // the type class is in the persisted listFiles - but it doesn't need the methods, skip it
                continue;
            }
            assertNotNull(ClassUtil.getDeclaredMethodSwallowError(javac, "copy", copyArgs));
            assertNotNull(ClassUtil.getDeclaredMethodSwallowError(javac, "serialize", serializeArgs));
            assertNotNull(ClassUtil.getDeclaredMethodSwallowError(javac, "deserialize", deserializeArgs));
        }
    }


    /**
     * Ensure that our bag can be round tripped intact.
     */
    public void testBagReconstitution() {
        com.hitorro.util.typesystem.Bag b = new com.hitorro.util.typesystem.Bag();
        com.hitorro.util.typesystem.Type t = (com.hitorro.util.typesystem.Type) com.hitorro.util.typesystem.TypeManager.getTypeManager().getTypeByShortName("webpage");
        b.setType(t);
        String names[] = b.getFieldNames();

        String title = "title text";
        String bodyText = "body text";
        Long created = new Long(123456);
        b.setValue(null, "title", title);
        b.setValue(null, "body", bodyText);
        b.setValue(null, "created", created);
        b = grindBag(b);
        names = b.getFieldNames();


        assertEquals(b.getValue(null, "title"), title);
        assertEquals(b.getValue(null, "body"), bodyText);
        assertEquals(b.getValue(null, "created"), created);
    }


    private com.hitorro.util.typesystem.Bag grindBag(com.hitorro.util.typesystem.Bag fv) {
        byte b[] = com.hitorro.util.typesystem.HTSerializableUtil.getHTSerializableBuffer(fv);
        return (com.hitorro.util.typesystem.Bag) com.hitorro.util.typesystem.HTSerializableUtil.readHTSerializableFromBuffer(b, null);
    }
}
