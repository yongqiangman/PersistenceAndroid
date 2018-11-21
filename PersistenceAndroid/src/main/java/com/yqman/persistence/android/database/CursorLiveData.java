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

import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContentResolverCompat;

/**
 * 使用场景：用于加载数据库中数据量较小的内容，比如一千条一下的cursor数据，相对于CursorLoader的只需要一次查询的简单情况
 * 相对于CursorLoader的区别：直接对外以LiveData的形式提供，因此使用方式与LiveData一致，因此可以与多个Lifecycle进行绑定
 */
public class CursorLiveData<T> extends MutableLiveData<T> {
    public CursorLiveData(@NonNull Context context,
                          @NonNull IParser<T> parser,
                          @NonNull Uri uri, @Nullable String[] projection,
                          @Nullable String selection, @Nullable String[] selectionArgs,
                          @Nullable String sortOrder) {
        new LoadTask<>(this, parser, uri, projection, selection, selectionArgs, sortOrder).execute(context);
    }

    /**
     * 一次异步查询的任务
     */
    private static class LoadTask<T> extends AsyncTask<Context, Void, T> {
        private final IParser<T> mParser;
        private final Uri mUri;
        private final String[] mProjection;
        private final String mSelection;
        private final String[] mSelectionArgs;
        private final String mSortOrder;
        private final MutableLiveData<T> mLiveData;
        LoadTask(@NonNull MutableLiveData<T> liveData,
                 @NonNull IParser<T> parser,
                 @NonNull Uri uri, @Nullable String[] projection,
                 @Nullable String selection, @Nullable String[] selectionArgs,
                 @Nullable String sortOrder) {
            mLiveData = liveData;
            mParser = parser;

            mUri = uri;
            mProjection = projection;
            mSelection = selection;
            mSelectionArgs = selectionArgs;
            mSortOrder = sortOrder;
        }

        @Override
        protected T doInBackground(Context... contexts) {
            Cursor cursor = ContentResolverCompat.query(contexts[0].getContentResolver(), mUri, mProjection, mSelection,
                    mSelectionArgs, mSortOrder, null);
            return mParser.parse(cursor);
        }

        @Override
        protected void onPostExecute(T data) {
            mLiveData.setValue(data);
        }
    }

    /**
     * cursor解析器
     */
    public interface IParser<T> {
        T parse(@NonNull Cursor cursor);
    }
}
