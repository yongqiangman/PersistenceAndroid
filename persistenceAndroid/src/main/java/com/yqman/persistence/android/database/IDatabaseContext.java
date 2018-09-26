package com.yqman.persistence.android.database;

/**
 * Created by manyongqiang on 2017/12/15.
 */

public interface IDatabaseContext {

    void create(IDatabaseOperation databaseOperation);

    void upgrade(IDatabaseOperation databaseOperation, int oldVersion, int newVersion);

    void open(IDatabaseOperation databaseOperation);

    IDatabaseOperation getDatabase(boolean writable);
}
