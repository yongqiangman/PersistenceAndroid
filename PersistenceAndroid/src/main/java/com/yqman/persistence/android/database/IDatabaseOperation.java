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
