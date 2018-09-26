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
