package com.example.sqliteprocessor;

import android.database.sqlite.SQLiteDatabase;

import com.jeppeman.sqliteprocessor.ForeignKey;
import com.jeppeman.sqliteprocessor.OnCreate;
import com.jeppeman.sqliteprocessor.OnUpgrade;
import com.jeppeman.sqliteprocessor.PrimaryKey;
import com.jeppeman.sqliteprocessor.SQLiteField;
import com.jeppeman.sqliteprocessor.SQLiteFieldType;
import com.jeppeman.sqliteprocessor.SQLiteTable;

import java.util.List;

/**
 * @author jesper
 */
@SQLiteTable(tableName = "myLittleTable")
public class MyLittleClass {

    @OnCreate
    public static void onCreate(SQLiteDatabase database) {
        int x = 9;
    }

    @OnUpgrade
    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {

    }

    @SQLiteField
    @PrimaryKey(autoIncrement = true)
    int id;

    @SQLiteField
    String name;

    @SQLiteField(fieldType = SQLiteFieldType.REAL)
    Short shortz;

    @SQLiteField("someOtherNameeps")
    List<String> nameList;

    @SQLiteField
    Boolean yo;

    @SQLiteField
    @ForeignKey(table = "test", fieldReference = "id")
    int foreign;
}
