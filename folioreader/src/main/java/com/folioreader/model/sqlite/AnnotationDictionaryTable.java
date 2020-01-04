package com.folioreader.model.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author gautam chibde on 28/6/17.
 */

public class AnnotationDictionaryTable {

    public class AnnotationDefinition
    {
        public String Romanization;
        public String Definition;
        public Boolean IsDefault;
        public Integer Learned;
        public String Word;
        public Integer DefinitionId;
        public Integer WordId;

        public AnnotationDefinition(Integer wordId, Integer defId, String word, String romanization, String definition, Boolean isDefault, Integer learned)
        {
            WordId = wordId;
            DefinitionId = defId;
            Word = word;
            Romanization = romanization;
            Definition = definition;
            IsDefault = isDefault;
            Learned = learned;
        }
    }


    private static final String TABLE_NAME = "annotation_dictionary_table";
    private static final String WORD_TABLE = "words";

    private static final String ID = "id";
    private static final String WORD_ID = "wordid";
    private static final String WORD = "word";
    private static final String ROMANIZATION = "romanization";
    private static final String MEANING = "meaning";
    private static final String ISDEFAULT = "isdefault";
    private static final String HIDDEN = "hidden";

    private static final String LEARNED = "learned";
    private static final String WORDLEN = "wordlen";
    private SQLiteDatabase database;

    public AnnotationDictionaryTable(Context context) {
        FolioDatabaseHelper dbHelper = new FolioDatabaseHelper(context);
        database = dbHelper.getWritableDatabase();

        database.execSQL(AnnotationDictionaryTable.SQL_CREATE);
        database.execSQL(AnnotationDictionaryTable.SQL_WORD_CREATE);
    }

    public static final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ( " + ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT" + ","
            + WORD + " TEXT" + ","
            + ROMANIZATION + " TEXT" + ","
            + ISDEFAULT + " integer" + ","
            + MEANING + " TEXT"
            + HIDDEN + "INTEGER" + ")";

    public static final String SQL_WORD_CREATE = "CREATE TABLE IF NOT EXISTS " + WORD_TABLE + " ( " + WORD_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT" + ","
            + WORD + " TEXT UNIQUE" + ","
            + WORDLEN + " integer" + ","
            + LEARNED + " integer)";

    public static final String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public boolean insertWord(String word, String meaning, String romanization, int isDefault) {

        ContentValues wordValues = new ContentValues();
        wordValues.put(WORD, word);
        wordValues.put(WORDLEN, word.length());
        database.insertWithOnConflict(WORD_TABLE, null, wordValues, SQLiteDatabase.CONFLICT_REPLACE);

        ContentValues values = new ContentValues();
        values.put(WORD, word);
        values.put(MEANING, meaning);
        values.put(ROMANIZATION, romanization);
        values.put(ISDEFAULT, isDefault);
        return database.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE) > 0;
    }

    public void setDefaultForWord(String word, String id)
    {
        database.beginTransaction();

        ContentValues values = new ContentValues();
        values.put(ISDEFAULT, 0);

        int result = database.update(TABLE_NAME, values, WORD  + " = ? and id != ?", new String[] { word, id });

        values.clear();
        values.put(ISDEFAULT, 1);

        result = database.update(TABLE_NAME, values, WORD  + " = ? and id = ?", new String[] { word, id});

        database.setTransactionSuccessful();
        database.endTransaction();
    }

    public void setLearnedForWord(String word, String learned)
    {
        database.beginTransaction();

        ContentValues values = new ContentValues();
        values.put(LEARNED, learned);

        database.update(WORD_TABLE, values, WORD  + " = ?", new String[] { word.trim()});
        database.setTransactionSuccessful();
        database.endTransaction();
    }

    public List<AnnotationDefinition> getDefinitionsForWord(String word) {
        Cursor c = database.rawQuery("SELECT distinct id, romanization, meaning, isdefault, learned, wordid FROM "
                + TABLE_NAME + " INNER JOIN " + WORD_TABLE + " using(word)"
                + " WHERE "  + TABLE_NAME + "." + WORD + " = ? and hidden != 1 group by id order by isdefault desc, id", new String[] { word.trim()});

        List<AnnotationDefinition> defs = new ArrayList<>();

        while (c.moveToNext()) {
            defs.add(new AnnotationDefinition(c.getInt(5), c.getInt(0), word, c.getString(1), c.getString(2), c.getInt(3) == 1, c.getInt(4)));
        }
        c.close();
        return defs;
    }

    public AnnotationDefinition getFirstDefinition(String word) {
        Cursor c = database.rawQuery("SELECT distinct id, romanization, meaning, isdefault, learned, wordid FROM "
                + TABLE_NAME + " INNER JOIN " + WORD_TABLE + " using(word)"
                + " WHERE "  + TABLE_NAME + "." + WORD + " = ? and hidden != 1 group by id order by isdefault desc, id", new String[] { word.trim()});

        AnnotationDefinition def = null;
        if (c.moveToNext()) {
             def = new AnnotationDefinition(c.getInt(5), c.getInt(0), word, c.getString(1), c.getString(2), c.getInt(3) == 1, c.getInt(4));
        }
        c.close();
        return def;
    }


    public List<AnnotationDefinition> getDefinitionsForWordAndSubWords(String word, String context) {
        List<AnnotationDefinition> defs = getDefinitionsForWord(word);

        if (!defs.isEmpty())
        {
            String firstDef = defs.get(0).Definition;

            if (firstDef.startsWith("see "))
            {
                String termToLookUp = firstDef.substring(firstDef.indexOf("|" )+1, firstDef.indexOf("["));
                AnnotationDefinition seeDef = getFirstDefinition(termToLookUp);

                if (seeDef != null)
                {
                    defs.add(seeDef);
                }
            }
        }

        // First, find a longer phrase from context if possible
        StringBuilder builder = new StringBuilder();
        String[] params = new String[context.trim().length() - word.trim().length()];

        int index = 0;
        for (int i = context.length(); i > word.length(); i--) {
            params[index] = context.substring(0, i);

            if (index > 0)
            {
                builder.append(",");
            }

            index++;
            builder.append("?");
        }

        Cursor c = database.rawQuery("SELECT distinct id, romanization, meaning, isdefault, learned, wordid, word FROM "
                + WORD_TABLE + " INNER JOIN " + TABLE_NAME + " using(word)"
                + " WHERE " + TABLE_NAME + "." + WORD + " IN (" + builder.toString() + ") and hidden != 1"
                + " group by id order by wordlen desc, isdefault desc, id", params);

        if (c.moveToFirst()) {
            defs.add(new AnnotationDefinition(c.getInt(5), c.getInt(0), c.getString(6), c.getString(1), c.getString(2), c.getInt(3) == 1, c.getInt(4)));
        }

        c.close();

        if (word.length() > 1) {
            builder = new StringBuilder();
            params = new String[word.trim().length()];
            for (int i = 0; i < word.trim().length(); i++) {
                params[i] = word.substring(i, i + 1);

                if (i > 0)
                {
                    builder.append(",");
                }
                builder.append("?");
            }

            c = database.rawQuery("SELECT distinct id, romanization, meaning, isdefault, learned, wordid, word FROM "
                    + TABLE_NAME + " INNER JOIN " + WORD_TABLE + " using(word)"
                    + " WHERE " + TABLE_NAME + "." + WORD + " IN (" + builder.toString() + ") and hidden != 1"
                    + " group by id order by isdefault desc, id", params);

            while (c.moveToNext()) {
                defs.add(new AnnotationDefinition(c.getInt(5), c.getInt(0), c.getString(6), c.getString(1), c.getString(2), c.getInt(3) == 1, c.getInt(4)));
            }
            c.close();
        }
        return defs;
    }

    public Map<String, List<AnnotationDefinition>> GetAllWords(int size)
    {
        Map<String, List<AnnotationDefinition>> result = new HashMap<>();

        Cursor c = database.rawQuery("SELECT  distinct id, " + TABLE_NAME + ".word, romanization, meaning, isdefault, learned, wordid FROM "
                + WORD_TABLE + " INNER JOIN " + TABLE_NAME + " using(word)"
                + " where hidden != 1 and wordlen <= ?"
                + " group by id ORDER BY " + TABLE_NAME + ".word, isdefault desc, id", new String[] {Integer.toString(size)});

        while (c.moveToNext()) {
            String word = c.getString(1);

            AnnotationDefinition def = new AnnotationDefinition(c.getInt(6), c.getInt(0), word, c.getString(2), c.getString(3), c.getInt(4) == 1, c.getInt(5));

            if (result.containsKey(word))
            {
                result.get(word).add(def);
            }
            else
            {
                List<AnnotationDefinition> defs = new ArrayList<>();
                defs.add(def);
                result.put(word, defs);
            }
        }

        c.close();
        return result;
    }


    public Map<String, List<AnnotationDefinition>> GetAllWords()
    {
        Map<String, List<AnnotationDefinition>> result = new HashMap<>();

        Cursor c = database.rawQuery("SELECT  distinct id, " + TABLE_NAME + ".word, romanization, meaning, isdefault, learned, wordid FROM "
                  + TABLE_NAME + " INNER JOIN " + WORD_TABLE + " using(word)"
                + " where hidden != 1"
                + " group by id ORDER BY " + TABLE_NAME + ".word, isdefault desc, id", null);

        while (c.moveToNext()) {
            String word = c.getString(1);

            AnnotationDefinition def = new AnnotationDefinition(c.getInt(6), c.getInt(0), word, c.getString(2), c.getString(3), c.getInt(4) == 1, c.getInt(5));

            if (result.containsKey(word))
            {
                result.get(word).add(def);
            }
            else
            {
                List<AnnotationDefinition> defs = new ArrayList<>();
                defs.add(def);
                result.put(word, defs);
            }
        }

        c.close();
        return result;
    }

    public boolean hasWords()
    {
        Cursor c = database.rawQuery("SELECT * FROM "
                + TABLE_NAME, null);


        if (c.moveToFirst()) {
            c.close();
            return true;
        }

        c.close();
        return false;
    }
}
