package com.folioreader.model.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ReadLocatorTable {

    private static final String TABLE_NAME = "read_locators";

    private static final String EPUB = "epub";
    private static final String READLOCATOR = "readlocator";

    private SQLiteDatabase database;

    public ReadLocatorTable(Context context) {
        FolioDatabaseHelper dbHelper = new FolioDatabaseHelper(context);
        database = dbHelper.getWritableDatabase();

        database.execSQL(SQL_CREATE);
    }

    public static final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ( "
            + EPUB + " TEXT PRIMARY KEY" + ","
            + READLOCATOR + " TEXT" + ")";


    public static final String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public boolean save(String epub, String readLocator) {

        ContentValues values = new ContentValues();
        values.put(EPUB, epub.trim().toLowerCase());
        values.put(READLOCATOR, readLocator);
        return database.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE) > 0;
    }

    public String getReadLocator(String epub)
    {
        Cursor c = database.rawQuery("SELECT " + READLOCATOR + " FROM "
                + TABLE_NAME +
                " WHERE " + EPUB + " = ?", new String[] { epub.trim().toLowerCase()});

        if (c.moveToNext()) {
            return c.getString(0);
        }

        return null;
    }
}
