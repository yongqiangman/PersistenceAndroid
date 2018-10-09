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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by manyongqiang on 2017/11/27.
 *
 */

public class CreateTableSQLBuilder {
    private static final String TAG = "CreateTableSQLBuilder";
    private String mTableName;
    private String mTableContract;
    private StringBuilder mContentBuilder;

    public CreateTableSQLBuilder(@NonNull String tableName) {
        mTableName = tableName;
        mContentBuilder = new StringBuilder();
    }

    public CreateTableSQLBuilder setTableContract(@NonNull String tableContract) {
        mTableContract = tableContract;
        return this;
    }

    public CreateTableSQLBuilder addColumn(String columnName, String signature) {
        if (mContentBuilder.length() > 0) {
            mContentBuilder.append(",");
        }
        mContentBuilder.append(columnName);
        mContentBuilder.append(" ");
        mContentBuilder.append(signature);
        return this;
    }

    @Nullable
    public String createSql() {
        String columnSql = mContentBuilder.toString();
        if (TextUtils.isEmpty(columnSql)) {
            Log.d(TAG, "columnSql is empty");
            return null;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE IF NOT EXISTS ");
        builder.append(mTableName);
        builder.append("(");
        builder.append(mContentBuilder.toString());
        if (!TextUtils.isEmpty(mTableContract)) {
            builder.append(",");
            builder.append(mTableContract);
        }
        builder.append(")");
        String sql = builder.toString();
        Log.d(TAG, "sql:" + sql);
        return sql;
    }

}
