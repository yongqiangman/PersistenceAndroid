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

package com.yqman.persistence.android;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.yqman.persistence.android.database.BaseWCDBDatabase;
import com.yqman.persistence.android.database.IDatabaseOperation;
import com.yqman.persistence.android.file.AndroidDirectory;
import com.yqman.persistence.file.FileAccessErrException;
import com.yqman.persistence.file.IFileVisitor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.provider.DocumentFile;

/**
 * String持久化工具，可以利用来存储json信息
 * 不支持并发操作，如果并发请求会存在不可预知的问题
 */
public class StringPersistenceTools {

    private final IStringPersistence mStringPersistence;
    /**
     * 最大字符个数
     */
    private static int MAX_CHARACTER_COUNT = 10*1024; // 默认存储10MB的字符数

    private int mMaxCharacterCount = MAX_CHARACTER_COUNT;
    /**
     * 当前最大字符数
     */
    private int mCurrentCharacterCount = 0;
    private static LinkedHashMap<String, StringValue> mStringCache = new LinkedHashMap<>();

    public StringPersistenceTools(Context context) throws FileAccessErrException {
        this(context, MAX_CHARACTER_COUNT, null);
    }

    public StringPersistenceTools(Context context, int maxCharacterCount) throws FileAccessErrException {
        this(context, maxCharacterCount, null);
    }

    public StringPersistenceTools(Context context, int maxCharacterCount, IStringPersistence stringPersistence) throws
            FileAccessErrException {
        mMaxCharacterCount = maxCharacterCount > 0 ? maxCharacterCount : MAX_CHARACTER_COUNT;
        mStringPersistence = (stringPersistence != null) ? stringPersistence : new FileStringPersistenceImpl(context);
    }

    /**
     * @param key 待存储的字符串对应key
     * @param value 待存储的字符串
     */
    public synchronized void save(@NonNull String key, @NonNull String value) {
        StringValue lastString = mStringCache.get(key);
        if (lastString != null) {
            mCurrentCharacterCount -= lastString.length();
            mCurrentCharacterCount += value.length();
            lastString.value = value;
            lastString.isNewValue = true;
        } else {
            mCurrentCharacterCount += value.length();
            mStringCache.put(key, new StringValue(value, true));
        }
        if (mCurrentCharacterCount > MAX_CHARACTER_COUNT) {
            handleCharacterOverflow();
        }
    }

    /**
     * 根据key获取存储的字符串
     * @param key 待获取的字符串对应key
     */
    public synchronized String obtain(String key) {
        if (mStringCache.containsKey(key)) {
            return mStringCache.get(key).value;
        }
        final String localString = mStringPersistence.obtainString(key);
        mStringCache.put(key, new StringValue(localString, false));
        if (localString != null) {
            mCurrentCharacterCount += localString.length();
            if (mCurrentCharacterCount > MAX_CHARACTER_COUNT) {
                handleCharacterOverflow();
            }
        }
        return localString;
    }

    /**
     * 清空缓存，检测可能需要写入本地的写入本地
     * @return true缓存清空完毕
     */
    public synchronized boolean cleanCacheAndPersistence() {
        Iterator<Map.Entry<String, StringValue>> mapIterator = mStringCache.entrySet().iterator();
        while (mapIterator.hasNext()) {
            Map.Entry<String, StringValue> entry = mapIterator.next();
            if (!entry.getValue().isNeedSaveToLocal()
                    || mStringPersistence.saveString(entry.getKey(), entry.getValue().value)) {
                mapIterator.remove();
                mCurrentCharacterCount -= entry.getValue().length();
            }
        }
        return mCurrentCharacterCount == 0;
    }

    /**
     * 处理字符溢出
     * 1. LRU算法清除缓存中多余字符
     * 2. 被清除的字符进行持久化
     */
    private void handleCharacterOverflow() {
        Iterator<Map.Entry<String, StringValue>> mapIterator = mStringCache.entrySet().iterator();
        while (mCurrentCharacterCount > mMaxCharacterCount && mapIterator.hasNext()) {
            Map.Entry<String, StringValue> entry = mapIterator.next();
            if (!entry.getValue().isNeedSaveToLocal()
                    || mStringPersistence.saveString(entry.getKey(), entry.getValue().value)) {
                mapIterator.remove();
                mCurrentCharacterCount -= entry.getValue().length();
            }
        }
    }

    public interface IStringPersistence {
        boolean saveString(String key, String value);
        String obtainString(String key);
    }

    /**
     * 使用文件存储字符串
     */
    public static class FileStringPersistenceImpl implements IStringPersistence {

        private final AndroidDirectory mDirectory;

        public FileStringPersistenceImpl(Context context) throws FileAccessErrException {
            File directory = new File(context.getCacheDir(), "StringPersistence");
            if (directory.exists() || directory.mkdirs()) {
                mDirectory = new AndroidDirectory(context, DocumentFile.fromFile(directory));
            } else {
                throw new FileAccessErrException("create directory failed:" + directory.getAbsolutePath());
            }
        }

        @Override
        public boolean saveString(String key, String value) {
            try {
                IFileVisitor fileVisitor = getLocalFile(key);
                if (fileVisitor == null) {
                    return false;
                }
                fileVisitor.writeString(value, false);
                return true;
            } catch (FileAccessErrException e) {
                return false;
            }
        }

        @Override
        public String obtainString(String key) {
            try {
                IFileVisitor fileVisitor = getLocalFile(key);
                if (fileVisitor == null) {
                    return null;
                }
                InputStream inputStream = fileVisitor.getInputStream();
                if (inputStream == null) {
                    return null;
                }
                StringBuilder builder = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String value = reader.readLine();
                while (value != null) {
                    builder.append(value);
                    value = reader.readLine();
                }
                reader.close();
                return builder.toString();
            } catch (FileNotFoundException e) {
                return null;
            } catch (IOException e) {
                return null;
            }
        }

        private IFileVisitor getLocalFile(String key) throws FileAccessErrException {
            String fileName = key + ".json";
            for (IFileVisitor fileVisitor: mDirectory.listFiles()) {
                if (fileVisitor.getDisplayName().equals(fileName)) {
                    return fileVisitor;
                }
            }
            return mDirectory.createNewFile(fileName);
        }
    }

    public static class SQLiteStringPersistenceIml implements IStringPersistence {

        private final Database mDatabase;

        public SQLiteStringPersistenceIml(Context context) {
            mDatabase = new Database(context);
        }

        @Override
        public boolean saveString(String key, String value) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(Contract.KEY, key);
            contentValues.put(Contract.VALUE, value);
            return mDatabase.getDatabase(true).insert(Contract.TABLE, null, contentValues) != -1;
        }

        @Override
        public String obtainString(String key) {
            Cursor cursor = mDatabase.getDatabase(false).query(Contract.TABLE, null,
                    Contract.KEY + "=?", new String[] {key},
                    null, null, null);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                return cursor.getString(cursor.getColumnIndex(Contract.VALUE));
            }
            return null;
        }

        private static class Database extends BaseWCDBDatabase {

            Database(Context context) {
                super(context, "SQl_StringPersistence", 1);
            }

            @Override
            public void create(@NonNull IDatabaseOperation databaseOperation) {
                databaseOperation.execSQL("CREATE TABLE "+ Contract.TABLE +" ("
                        + Contract.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + Contract.KEY + " TEXT not null, "
                        + Contract.VALUE + " TEXT not null, "
                        + " UNIQUE("+ Contract.KEY + ") ON CONFLICT REPLACE)");
            }

            @Override
            public void upgrade(@NonNull IDatabaseOperation databaseOperation, int oldVersion, int newVersion) {

            }
        }

        private interface Contract {
            String TABLE = "db_string_persistence";
            String ID = "_id";
            String KEY = "key";
            String VALUE = "value";

        }
    }

    private static class StringValue {
        public StringValue(String value, Boolean isNewValue) {
            this.value = value;
            this.isNewValue = isNewValue;
        }

        String value = null;
        Boolean isNewValue = false;

        /**
         * 是否需要存储到本地
         * @return true 需要存储到本地
         */
        Boolean isNeedSaveToLocal() {
            return isNewValue;
        }

        int length() {
            return value != null ? value.length() : 0;
        }
    }
}
