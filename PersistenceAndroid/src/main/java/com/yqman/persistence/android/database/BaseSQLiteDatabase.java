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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by manyongqiang on 2017/12/15.
 * 使用原生SQLite的实现类
 */

public abstract class BaseSQLiteDatabase implements IDatabaseContext {

    private IDatabaseOperation mDatabaseOperation;
    private boolean mWritable;
    private SQLDatabaseInternal mSQLDatabaseInternal;

    public BaseSQLiteDatabase(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        mSQLDatabaseInternal = new SQLDatabaseInternal(context, name, factory, version);
    }

    @Override
    public void open(IDatabaseOperation databaseOperation) {

    }

    protected boolean isEnableWriteAheadLogging() {
        return false;
    }

    @Override
    public IDatabaseOperation getDatabase(boolean writable) {
        synchronized(this) {
            if (mDatabaseOperation == null) {
                if (writable) {
                    mDatabaseOperation = new SQLiteDatabaseOperation(mSQLDatabaseInternal.getWritableDatabase());
                } else {
                    mDatabaseOperation = new SQLiteDatabaseOperation(mSQLDatabaseInternal.getReadableDatabase());
                }
                mWritable = writable;
            } else {
                if (writable && !mWritable) {
                    mDatabaseOperation = new SQLiteDatabaseOperation(mSQLDatabaseInternal.getWritableDatabase());
                    mWritable = writable;
                }
            }
            return mDatabaseOperation;
        }
    }

    private static class SQLiteDatabaseOperation implements IDatabaseOperation {
        private final SQLiteDatabase mSQLiteDatabase;

        private SQLiteDatabaseOperation(SQLiteDatabase sqliteDatabase) {
            mSQLiteDatabase = sqliteDatabase;
        }

        @Override
        public Cursor query(String table, String[] columns, String selection, String[] selectionArgs, String groupBy,
                            String having, String orderBy) {
            return mSQLiteDatabase.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);
        }

        @Override
        public long insert(String table, String nullColumnHack, ContentValues values) {
            return mSQLiteDatabase.insert(table, nullColumnHack, values);
        }

        @Override
        public int delete(String table, String whereClause, String[] whereArgs) {
            return mSQLiteDatabase.delete(table, whereClause, whereArgs);
        }

        @Override
        public int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
            return mSQLiteDatabase.update(table, values, whereClause, whereArgs);
        }

        @Override
        public Cursor rawQuery(String sql, String[] selectionArgs) {
            return mSQLiteDatabase.rawQuery(sql, selectionArgs);
        }

        @Override
        public void execSQL(String sql) {
            mSQLiteDatabase.execSQL(sql);
        }
    }

    private class SQLDatabaseInternal extends SQLiteOpenHelper {

        private SQLDatabaseInternal(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public final void onCreate(SQLiteDatabase sqLiteDatabase) {
            create(new SQLiteDatabaseOperation(sqLiteDatabase));
        }

        @Override
        public final void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            upgrade(new SQLiteDatabaseOperation(sqLiteDatabase), i, i1);
        }

        @Override
        public final void onOpen(SQLiteDatabase sqLiteDatabase) {
            if (isEnableWriteAheadLogging()) {
                sqLiteDatabase.enableWriteAheadLogging();
            }
            open(new SQLiteDatabaseOperation(sqLiteDatabase));
        }

    }
}
