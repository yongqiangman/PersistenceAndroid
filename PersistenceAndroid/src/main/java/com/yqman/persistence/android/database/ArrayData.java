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

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * 动态获取cursor中的数据，并解析
 */
public class ArrayData<T> {
    private final Cursor mCursor;
    private final CursorLiveData.IParser<T> mParser;

    public ArrayData(@Nullable Cursor cursor, @NonNull CursorLiveData.IParser<T> parser) {
        mCursor = cursor;
        mParser = parser;
    }

    public int getCount() {
        if (mCursor != null && !mCursor.isClosed()) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }

    public T getItem(int index) {
        if (mCursor != null && !mCursor.isClosed()) {
            mCursor.moveToPosition(index);
            return mParser.parse(mCursor);
        } else {
            return null;
        }
    }
}