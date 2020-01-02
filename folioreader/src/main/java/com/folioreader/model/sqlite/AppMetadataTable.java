package com.folioreader.model.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class AppMetadataTable {

    public static final String TABLE_NAME = "app_metadata";

    public static final String KEY = "keyname";
    public static final String VALUE = "value";

    public static final String LAST_EBOOK = "lastebook";

    private SQLiteDatabase database;

    public AppMetadataTable(Context context) {
        FolioDatabaseHelper dbHelper = new FolioDatabaseHelper(context);
        database = dbHelper.getWritableDatabase();

        database.execSQL(SQL_CREATE);
    }

    public static final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ( "
            + KEY + " TEXT PRIMARY KEY" + ","
            + VALUE + " TEXT" + ")";


    public static final String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public boolean save(String key, String value) {

        ContentValues values = new ContentValues();
        values.put(KEY, key);
        values.put(VALUE, value);
        return database.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE) > 0;
    }

    public String getValue(String key)
    {
        Cursor c = database.rawQuery("SELECT " + VALUE + " FROM "
                + TABLE_NAME +
                " WHERE " + KEY + " = ?", new String[] { key.trim() });

        if (c.moveToNext()) {
            return c.getString(0);
        }

        return null;
    }



}
