/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.yqman.persistence.android.database;

import android.content.Context;

/**
 * Created by manyongqiang on 2017/12/15.
 * 使用WCDB的基础类
 */

public abstract class BaseWCDBDatabase implements IDatabaseContext {

    private IDatabaseOperation mDatabaseOperation;
    private boolean mWritable;
    private WCDBDatabaseInternal mWCDBDatabaseInternal;

    public BaseWCDBDatabase(Context context, String name, int version) {
        mWCDBDatabaseInternal = new WCDBDatabaseInternal(context, name, version, this);
    }

    @Override
    public IDatabaseOperation getDatabase(boolean writable) {
        synchronized(this) {
            if (mDatabaseOperation == null) {
                if (writable) {
                    mDatabaseOperation = new WCDBDatabaseInternal.WCDBDatabaseOperation(mWCDBDatabaseInternal.getWritableDatabase());
                } else {
                    mDatabaseOperation = new WCDBDatabaseInternal.WCDBDatabaseOperation(mWCDBDatabaseInternal.getReadableDatabase());
                }
                mWritable = writable;
            } else {
                if (writable && !mWritable) {
                    mDatabaseOperation = new WCDBDatabaseInternal.WCDBDatabaseOperation(mWCDBDatabaseInternal.getWritableDatabase());
                    mWritable = writable;
                }
            }
            return mDatabaseOperation;
        }
    }

    @Override
    public void open(IDatabaseOperation databaseOperation) {

    }
}
