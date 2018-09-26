package com.yqman.persistence.android.database;

import android.content.ContentValues;
import android.database.Cursor;

/**
 * Created by manyongqiang on 2017/12/15.
 */

public interface IDatabaseOperation {
    Cursor query(String table, String[] columns, String selection,
                 String[] selectionArgs, String groupBy, String having, String orderBy);

    long insert(String table, String nullColumnHack, ContentValues values);

    int delete(String table, String whereClause, String[] whereArgs);

    int update(String table, ContentValues values, String whereClause, String[] whereArgs);

    Cursor rawQuery(String sql, String[] selectionArgs);

    void execSQL(String sql);
}
