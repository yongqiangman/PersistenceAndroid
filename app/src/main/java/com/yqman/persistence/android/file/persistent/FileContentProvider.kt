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

package com.yqman.persistence.android.file.persistent

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.util.Log
import com.yqman.persistence.android.database.DatabaseTools

class FileContentProvider: ContentProvider() {
    override fun bulkInsert(uri: Uri, values: Array<ContentValues>): Int {
        if (values.isEmpty()) {
            return 0
        }
        database.getDatabase(true).execSQL(DatabaseTools.buildInsertSqlString(FileContract.TABLE, values).apply {
            Log.d("FileContentProvider", "insert $this")
        })
        context?.contentResolver?.apply {
            notifyChange(uri, null, false)
            Log.d("FileContentProvider", "notify $uri")
        }
        return values.size
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        database.getDatabase(true).insert(FileContract.TABLE, null, values)
        return null
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?, selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        Log.d("FileContentProvider", "query $uri")
        return database.getDatabase(false).query(FileContract.TABLE,
                projection, selection, selectionArgs, null, null, sortOrder)?.apply {
            setNotificationUri(context?.contentResolver, uri)
        }
    }

    private lateinit var database: FileDatabase

    override fun onCreate(): Boolean {
        database = FileDatabase(context)
        return true
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<String>?): Int {
        return database.getDatabase(true).update(FileContract.TABLE, values, selection, selectionArgs)
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return database.getDatabase(true).delete(FileContract.TABLE, selection, selectionArgs)
    }

    override fun getType(uri: Uri): String? {
        return null
    }
}