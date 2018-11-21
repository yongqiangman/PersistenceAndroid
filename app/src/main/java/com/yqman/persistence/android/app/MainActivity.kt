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

package com.yqman.persistence.android.app

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.yqman.persistence.android.database.CursorLiveData
import com.yqman.persistence.android.database.CursorLoader
import com.yqman.persistence.android.file.persistent.FileContract
import com.yqman.persistence.android.file.persistent.FileDao
import com.yqman.persistence.android.file.persistent.LocalFile

class MainActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        insertTest()
        query()
        Handler().postDelayed({
            Log.d("MainActivity0618", "insert===============")
            insertTest()
        }, 2000)
    }

    private fun insertTest() {
        val fileDao = FileDao(this)
        fileDao.insert(arrayListOf<LocalFile>().apply {
            add(LocalFile("path${System.nanoTime()}1", "path"))
            add(LocalFile("path${System.nanoTime()}2", "path"))
            add(LocalFile("path${System.nanoTime()}3", "path"))
            add(LocalFile("path${System.nanoTime()}4", "path"))
            add(LocalFile("path${System.nanoTime()}5", "path"))
        }.toTypedArray())
    }
    private fun query() {
        CursorLiveData<Array<LocalFile>>(this, FileContract.URI, arrayOf(FileContract.PATH, FileContract.NAME), null, null, null) {
            cursor ->
            val files = mutableListOf<LocalFile>()
            if (cursor.moveToFirst()) {
                do {
                    files.add(LocalFile(cursor.getString(cursor.getColumnIndex(FileContract.PATH)),
                            cursor.getString(cursor.getColumnIndex(FileContract.NAME))))
                } while (cursor.moveToNext())
            }
            files.toTypedArray()
        }.observe(this, Observer {
            it?.forEach {
                file ->
                Log.d("MainActivity0618", "first file: ${file.path}")
            }
        })

        CursorLoader<LocalFile>(this, this, FileContract.URI, arrayOf(FileContract.PATH, FileContract.NAME), null,
                null, null, CursorLoader.IParser<LocalFile> {
            cursor ->
            LocalFile(cursor.getString(cursor.getColumnIndex(FileContract.PATH)), cursor.getString(cursor.getColumnIndex(FileContract.NAME)))
        }).addObserver {
            it?.apply {
                for (index in 0 until count) {
                    val item = getItem(index)
                    Log.d("MainActivity0618", "second file: ${item.path}")
                }
            }
        }
    }
}