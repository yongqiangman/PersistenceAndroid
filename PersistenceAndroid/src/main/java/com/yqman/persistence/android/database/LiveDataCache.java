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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;

/**
 * 提供LiveData缓存的工具，提供给Repository使用
 * @param <T>
 */
public class LiveDataCache<T> {
    private final int mMaxLiveDataSize;
    private LinkedHashMap<String, MutableLiveData<T>> mLiveData = new LinkedHashMap<>();
    private Lock mLock = new ReentrantLock();
    public LiveDataCache() {
        this(5);
    }

    public LiveDataCache(int maxLiveDataSize) {
        mMaxLiveDataSize = maxLiveDataSize;
    }

    public MutableLiveData<T> getCache(String key) {
        mLock.lock();
        checkLiveDataCache();
        MutableLiveData<T> liveData;
        if (mLiveData.containsKey(key)) {
            liveData = mLiveData.get(key);
        } else {
            liveData = new CacheLiveData();
            mLiveData.put(key, liveData);
        }
        mLock.unlock();
        return liveData;
    }

    private void clearLiveDataCache() {
        Iterator<Map.Entry<String, MutableLiveData<T>>> iterator = mLiveData.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, MutableLiveData<T>> entry = iterator.next();
            if (!entry.getValue().hasObservers()) {
                iterator.remove();
            }
            if (mLiveData.size() < mMaxLiveDataSize) {
                return;
            }
        }
    }

    private void checkLiveDataCache() {
        if (mLiveData.size() >= mMaxLiveDataSize) {
            mLock.tryLock();
            clearLiveDataCache();
            mLock.unlock();
        }
    }

    private class CacheLiveData extends MutableLiveData<T> {
        @Override
        public void removeObserver(@NonNull Observer<T> observer) {
            super.removeObserver(observer);
            checkLiveDataCache();
        }
    }
}


