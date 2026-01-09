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

import com.hitorro.util.core.string.Fmt;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.typesystem.HTObjectInputStream;
import com.hitorro.util.typesystem.HTObjectOutputStream;
import com.hitorro.util.typesystem.HTSerializable;
import com.hitorro.util.typesystem.annotation.TypeClassMetaInfo;

import java.io.IOException;
import java.util.Date;

/**
 * User: chris Date: Oct 25, 2006 Time: 7:17:21 PM
 */

@TypeClassMetaInfo(shortTypeName = "npoTest",
        isView = false,
        isPersisted = true,
        schemaVersion = 1)
public class NonPersistObject implements HTSerializable {
    public String m_string;
    public Date m_date;
    public int int_;
    public boolean bool;
    public NonPersistObject m_ref;

    /**
     * Needed for persistence and serialization
     */
    public NonPersistObject() {

    }

    public NonPersistObject(String string, Date date, int i, boolean bool) {
        m_string = string;
        m_date = date;
        this.int_ = i;
        this.bool = bool;

    }

    public String toString() {
        return Fmt.S("NPO: %s", int_);
    }

    public void set(NonPersistObject a) {
        m_ref = a;
    }

    public void serialize(HTObjectOutputStream os) throws IOException, StoreException {
        os.writeInt(getSerializationVersion());
        os.writeString(m_string);
        os.writeDate(m_date);
        os.writeInt(int_);
        os.writeBoolean(bool);
        os.writeVersionedObject(m_ref);
    }

    public void deserialize(HTObjectInputStream os)
            throws IOException, ClassNotFoundException, StoreException {
        int version = os.readInt();
        m_string = os.readString();
        m_date = os.readDate();
        int_ = os.readInt();
        bool = os.readBoolean();
        m_ref = (NonPersistObject) os.readVersionedObject();
    }

    public int getSerializationVersion() {
        return 1;
    }

    public boolean isPersisted() {
        return false;
    }

    public boolean hasGuid() {
        return false;
    }

    public boolean hasSoftGuid() {
        return false;
    }
}