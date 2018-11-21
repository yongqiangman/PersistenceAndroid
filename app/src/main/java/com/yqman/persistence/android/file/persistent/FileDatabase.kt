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

import android.content.Context
import android.net.Uri
import com.yqman.persistence.android.database.BaseWCDBDatabase
import com.yqman.persistence.android.database.IDatabaseOperation

class FileDatabase(context: Context): BaseWCDBDatabase(context, "FileDatabase",1) {

    override fun create(databaseOperation: IDatabaseOperation) {
        databaseOperation.execSQL("CREATE TABLE "+ FileContract.TABLE +" ("
                + FileContract.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + FileContract.PATH + " TEXT not null, "
                + FileContract.NAME + " TEXT not null, "
                + " UNIQUE("+ FileContract.PATH + ") ON CONFLICT REPLACE)")
    }

    override fun upgrade(databaseOperation: IDatabaseOperation, oldVersion: Int, newVersion: Int) {

    }
}

data class LocalFile(val path: String, val name: String)

object FileContract {
    val URI = Uri.parse("content://com.yqman.persistence.android/cloudfile")
    const val TABLE = "cloudfile"
    const val ID = "_id"
    const val PATH = "path"
    const val NAME = "name"
}
