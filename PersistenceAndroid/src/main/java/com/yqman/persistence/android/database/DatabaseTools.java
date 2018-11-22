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


import android.content.ContentValues;
import android.database.DatabaseUtils;

public class DatabaseTools {
    public static String buildInsertSqlString(String tableName, ContentValues[] values) {
        if (values.length <= 0) {
            return "";
        }
        final ContentValues firstValue = values[0];
        final StringBuilder sb = new StringBuilder().append("INSERT INTO ").append(tableName).append("(");
        int i = 0;
        for (String colName : firstValue.keySet()) {
            sb.append((i > 0) ? "," : "");
            sb.append(colName);
            i++;
        }
        sb.append(") VALUES");
        int index = 0;
        for (ContentValues value: values) {
            sb.append("(");
            i = 0;
            for (String colName : firstValue.keySet()) {
                sb.append((i > 0) ? "," : "");
                sb.append(getString(value.get(colName)));
                i++;
            }
            sb.append(")");
            index++;
            if (index < values.length) sb.append(",");
        }
        return sb.toString();
    }

    private static String getString(Object value) {
        if (value == null) {
            return "";
        } else if (value instanceof String) {
            return DatabaseUtils.sqlEscapeString((String)value);
        } else {
            return value.toString();
        }
    }
}
