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

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentLinkedQueue;

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
 * 加载数据库数据
 */
public class CursorLiveData<T> extends MutableLiveData<T> implements GenericLifecycleObserver {
    private final @NonNull QueryInfo mQueryInfo;
    private final @NonNull LoadTask mLoadTask;
    private final @NonNull IParser<T> mParser;
    private final @NonNull CursorContentObserver mContentObserver;
    private @Nullable Cursor currentCursor = null;
    private final ConcurrentLinkedQueue<LifecycleOwner> mLifecycleOwners = new ConcurrentLinkedQueue<>();
    private final Boolean mIsNeedObserver;
    public CursorLiveData(@NonNull Context context,
                           @NonNull Uri uri, @Nullable String[] projection,
                           @Nullable String selection, @Nullable String[] selectionArgs,
                           @Nullable String sortOrder, @NonNull IParser<T> parser, Boolean isNeedObserver) {
        mQueryInfo = new QueryInfo(context.getApplicationContext(),
                uri, projection, selection, selectionArgs, sortOrder);
        mLoadTask = new LoadTask(this);
        mContentObserver = new CursorContentObserver(this);
        mIsNeedObserver = isNeedObserver;
        mParser = parser;
    }

    private void setCursor(@Nullable Cursor cursor) {
        if (mLifecycleOwners.isEmpty()) { // 解决已经没有观察者时，异步查询结果回来时，已经没有观察者，此时忽略该查询结果
            return;
        }
        setValue(mParser.parse(cursor));
        updateCursor(cursor);
    }

    private void updateCursor(@Nullable Cursor cursor) {
        Cursor oldCursor = currentCursor;
        currentCursor = cursor;
        if (oldCursor != null) {
            oldCursor.unregisterContentObserver(mContentObserver);
            if (!oldCursor.isClosed()) {
                oldCursor.close();
            }
        }
        if (cursor != null && mIsNeedObserver) {
            cursor.registerContentObserver(mContentObserver);
        }
    }

    @Override
    public void observe(@NonNull LifecycleOwner owner, @NonNull Observer<T> observer) {
        if (mLifecycleOwners.isEmpty()) {
            mLifecycleOwners.add(owner);
            startQuery();
        } else {
            mLifecycleOwners.add(owner);
        }
        owner.getLifecycle().addObserver(this);
        super.observe(owner, observer);
    }

    private void startQuery() {
        mLoadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mQueryInfo);
    }

    @Override
    public void onStateChanged(LifecycleOwner source, Lifecycle.Event event) {
        if (source.getLifecycle().getCurrentState() == DESTROYED) {
            mLifecycleOwners.remove(source);
        }
        if (mLifecycleOwners.isEmpty()) {
            updateCursor(null);
        }
    }

    /**
     * 查询信息
     */
    private static class QueryInfo {
        private final @NonNull Context context;
        private final @NonNull Uri uri;
        private final String[] projection;
        private final String selection;
        private final String[] selectionArgs;
        private final String sortOrder;

        private QueryInfo( @NonNull Context context,
                          @NonNull Uri uri, String[] projection, String selection,
                          String[] selectionArgs, String sortOrder) {
            this.uri = uri;
            this.projection = projection;
            this.selection = selection;
            this.selectionArgs = selectionArgs;
            this.sortOrder = sortOrder;
            this.context = context;
        }

    }

    /**
     * 一次异步查询的任务
     */
    private static class LoadTask extends AsyncTask<QueryInfo, Void, Cursor> {
        private final WeakReference<CursorLiveData> mLiveData;

        private LoadTask(CursorLiveData liveData) {
            mLiveData = new WeakReference<CursorLiveData>(liveData);
        }

        @Override
        protected Cursor doInBackground(QueryInfo... queryInfos) {
            final QueryInfo queryInfo = queryInfos[0];
            return ContentResolverCompat.query(queryInfo.context.getContentResolver(),
                    queryInfo.uri, queryInfo.projection, queryInfo.selection,
                    queryInfo.selectionArgs, queryInfo.sortOrder, null);
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            final CursorLiveData liveData = mLiveData.get();
            if (liveData != null) {
                liveData.setCursor(cursor);
            }
        }
    }

    /**
     * cursor监听
     */
    private static class CursorContentObserver extends ContentObserver {
        private final WeakReference<CursorLiveData> mLiveData;

        private CursorContentObserver(CursorLiveData liveData) {
            super(new Handler());
            mLiveData = new WeakReference<CursorLiveData>(liveData);
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
            final CursorLiveData liveData = mLiveData.get();
            if (liveData != null) {
                liveData.startQuery();
            }
        }
    }
    /**
     * cursor解析器
     */
    public interface IParser<T> {
        T parse(@Nullable Cursor cursor);
    }
}
