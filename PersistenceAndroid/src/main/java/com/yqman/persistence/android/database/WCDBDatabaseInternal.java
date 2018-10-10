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

import com.tencent.wcdb.database.SQLiteDatabase;
import com.tencent.wcdb.database.SQLiteOpenHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

class WCDBDatabaseInternal extends SQLiteOpenHelper {
    private IDatabaseContext mDatabaseContext;

    WCDBDatabaseInternal(Context context, String name, int version,
                                 IDatabaseContext databaseContext) {
        super(context, name, null, version);
        mDatabaseContext = databaseContext;
    }

    @Override
    public final void onCreate(SQLiteDatabase db) {
        mDatabaseContext.create(new WCDBDatabaseOperation(db));
    }

    @Override
    public final void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        mDatabaseContext.upgrade(new WCDBDatabaseOperation(db), oldVersion, newVersion);
    }

    @Override
    public final void onOpen(SQLiteDatabase db) {
        db.enableWriteAheadLogging();
        mDatabaseContext.open(new WCDBDatabaseOperation(db));
    }

    static class WCDBDatabaseOperation implements IDatabaseOperation {
        private final SQLiteDatabase mSQLiteDatabase;

        WCDBDatabaseOperation(SQLiteDatabase sqliteDatabase) {
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
}