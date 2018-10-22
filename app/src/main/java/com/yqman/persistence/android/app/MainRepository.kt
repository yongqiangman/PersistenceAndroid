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

import com.yqman.persistence.android.database.LiveDataCache

class MainRepository {
    val mainLiveDataCache = LiveDataCache<String>()

    fun getContentString(pos: Int) = mainLiveDataCache.getCache("content:$pos")

    fun refresh(pos: Int) {
        val liveData = mainLiveDataCache.getCache("content:$pos")
        liveData.postValue("test value")
    }
}