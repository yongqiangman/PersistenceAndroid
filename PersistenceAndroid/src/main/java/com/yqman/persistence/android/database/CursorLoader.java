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

import static android.arch.lifecycle.Lifecycle.State.DESTROYED;

import android.arch.lifecycle.GenericLifecycleObserver;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContentResolverCompat;

/**
 * 使用场景：用于加载数据库中数据量大的内容，比如一千条以上的cursor数据，如果一次性将所有数据加载进内存很容易导致oom
 * 相对于Android的CursorLoader和LoaderManager
 * 1. 具有和后者同样的特性，充分利用了cursor的特性，同时自动的生命周期管理，避免cursor泄漏
 * 2. 使用更加简单，代码量更少，
 *    2.1 不需要是配合LoaderManager，直接需要实例一个对象然后添加观察者即可
 *    2.2 根据用户的解析器自动将数据解析成ArrayData使用起来跟普通数组一样
 *    2.3 一个CursorLoader可以注册多个Observer，在一个Activity多个Fragment切换时可以利用上次的查询结果，不过需要注意的是构造CursorLoader的时候传入的Lifecycle
 *    对应Activity的Lifecycle
 */
public class CursorLoader<T> {
    private final Context mContext;
    private LifecycleOwner mLifecycleOwner;
    private final MutableLiveData<ArrayData<T>> mCursorLiveData;
    private final IParser<T> mParser;
    private final CursorContentObserver mCursorContentObserver;
    private final Uri mUri;
    private final String[] mProjection;
    private final String mSelection;
    private final String[] mSelectionArgs;
    private final String mSortOrder;
    private Cursor mCurrentCursor;
    private volatile boolean mIsDestroyed = true;

    public CursorLoader(@NonNull Context context,
                        @NonNull LifecycleOwner lifecycleOwner,
                        @NonNull IParser<T> parser,
                        @NonNull Uri uri, @Nullable String[] projection,
                        @Nullable String selection, @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        mContext = context;
        mLifecycleOwner = lifecycleOwner;
        mLifecycleOwner.getLifecycle().addObserver(new MyLifecycleObserver(lifecycleOwner));
        mCursorLiveData = new MutableLiveData<>();
        mParser = parser;

        mCursorContentObserver = new CursorContentObserver();

        mUri = uri;
        mProjection = projection;
        mSelection = selection;
        mSelectionArgs = selectionArgs;
        mSortOrder = sortOrder;
        query();
    }

    /**
     * 开始新一轮的查询
     */
    private void query() {
        new LoadTask().execute((Void[]) null);
    }

    /**
     * 暂停当前正在运行load、关闭对当前的cursor的监听、关闭当前的cursor; 可能生命周期结束后主动调用或者外部调用
     */
    public void destroy() {
        mIsDestroyed = false;
        mLifecycleOwner = null;
        if (mCurrentCursor != null) {
            mCurrentCursor.unregisterContentObserver(mCursorContentObserver);
            if (!mCurrentCursor.isClosed()) {
                mCurrentCursor.close();
            }
        }
    }

    /**
     * 添加监听
     */
    public void addObserver(@NonNull  Observer<ArrayData<T>> observer) {
        mCursorLiveData.observe(mLifecycleOwner, observer);
    }

    /**
     * 移除监听
     */
    public void removeObserver(@NonNull Observer<ArrayData<T>> observer) {
        mCursorLiveData.removeObserver(observer);
    }

    private Cursor loadingInBackground() {
        Cursor cursor = ContentResolverCompat.query(mContext.getContentResolver(), mUri, mProjection, mSelection,
                mSelectionArgs, mSortOrder, null);
        if (cursor != null) {
            try {
                // Ensure the cursor window is filled.
                cursor.getCount();
                cursor.registerContentObserver(mCursorContentObserver);
            } catch (RuntimeException ex) {
                cursor.close();
                throw ex;
            }
        }
        return cursor;
    }

    /**
     * 将新的cursor数据更新到ui
     */
    private void deliverCursor(@Nullable Cursor cursor) {
        Cursor oldCursor = mCurrentCursor;
        mCurrentCursor = cursor;
        mCursorLiveData.setValue(new ArrayData<>(mCurrentCursor, mParser));
        if (oldCursor != null && !oldCursor.isClosed()) {
            oldCursor.close();
        }
    }

    /**
     * 重新走一次查询的流程
     */
    private void onContentChanged() {
        query();
    }

    /**
     * 对生命周期的监听
     */
    private class MyLifecycleObserver implements GenericLifecycleObserver {
        @NonNull final LifecycleOwner mOwner;

        MyLifecycleObserver(@NonNull LifecycleOwner lifecycleOwner) {
            mOwner = lifecycleOwner;
        }

        @Override
        public void onStateChanged(LifecycleOwner source, Lifecycle.Event event) {
            if (mOwner.getLifecycle().getCurrentState() == DESTROYED) {
                mLifecycleOwner.getLifecycle().removeObserver(this);
                destroy();
            }
        }
    }

    /**
     * 对cursor的监听
     */
    private class CursorContentObserver extends ContentObserver {
        CursorContentObserver() {
            super(new Handler());
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
            if (mIsDestroyed) {
                onContentChanged();
            } else if (mCurrentCursor != null) {
                mCurrentCursor.unregisterContentObserver(this);
            }
        }
    }

    /**
     * 一次异步查询的任务
     */
    private class LoadTask extends AsyncTask<Void, Void, Cursor> {
        @Override
        protected Cursor doInBackground(Void... voids) {
            if (mIsDestroyed) {
                return loadingInBackground();
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            if (mIsDestroyed) {
                deliverCursor(cursor);
            }
        }
    }

    /**
     * cursor解析器
     */
    public interface IParser<T> {
        T parse(@NonNull Cursor cursor);
    }

    /**
     * 动态获取cursor中的数据，并解析
     */
    public static class ArrayData<T> {
        private final Cursor mCursor;
        private final IParser<T> mParser;

        public ArrayData(@Nullable Cursor cursor, @NonNull IParser<T> parser) {
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
}