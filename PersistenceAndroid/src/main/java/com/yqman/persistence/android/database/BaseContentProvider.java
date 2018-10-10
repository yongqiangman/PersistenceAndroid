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

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by manyongqiang on 2017/11/27.
 *
 */

public abstract class BaseContentProvider extends ContentProvider {

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s, @Nullable String[] strings1,
                        @Nullable String s1) {
        return doQuery(uri, strings, s, strings1, s1);
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        return doInsert(uri, contentValues);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        return doDelete(uri, s, strings);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s,
                      @Nullable String[] strings) {
        return doUpdate(uri, contentValues, s, strings);
    }

    protected final ContentResolver getContentResolver() {
        Context context = getContext();
        if (context == null) {
            return null;
        }
        return context.getContentResolver();
    }

    protected abstract Cursor doQuery(@NonNull Uri uri, @Nullable String[] projection,
                                      @Nullable String selection, @Nullable String[] selectionArgs,
                                      @Nullable String sortOrder);

    protected abstract Uri doInsert(@NonNull Uri uri, @Nullable ContentValues contentValues);

    protected abstract int doDelete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs);

    protected abstract int doUpdate(@NonNull Uri uri, @Nullable ContentValues contentValues,
                                  @Nullable String selection, @Nullable String[] selectionArgs);

}
