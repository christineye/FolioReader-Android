package com.folioreader.model.dictionary;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.folioreader.model.sqlite.AnnotationDictionaryTable;
import com.folioreader.model.sqlite.FolioDatabaseHelper;

import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class U8Parser {

    // returns next word from string starting from start index
    private static String GetNextWord(String input, int start)
    {

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i + start < input.length(); i++)
        {
            int thisChar  = input.codePointAt(start + i);

            if (thisChar == ' ')
            {
                break;
            }

            builder.appendCodePoint(thisChar);
        }

        return builder.toString();
    }

    public static void Init(Context context)
    {
        try {

            AnnotationDictionaryTable databaseHelper = new AnnotationDictionaryTable(context);

            InputStream stream = context.getAssets().open("dict/cedict_ts.u8");
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));

            int i = 0;
            String st;
            while ((st = br.readLine()) != null) {

                if (st.startsWith("#"))
                {
                    continue;
                }

                String traditional = GetNextWord(st, 0);
                String simplified = GetNextWord(st, traditional.length() + 1);
                String pinyin = st.substring(st.indexOf("[") + 1, st.indexOf("]"));
                String definition = st.substring(st.indexOf("/") + 1);

                if (definition.endsWith("/"))
                {
                    definition = definition.substring(0, definition.length() - 1);
                }

                int isDefault = 0;
                if (definition.startsWith("variant of") || definition.startsWith("surname") || definition.startsWith("see ")
                        || definition.startsWith("abbr.") || definition.startsWith("old variant"))
                {
                    isDefault = -1;
                }

                databaseHelper.insertWord(simplified, definition, pinyin, isDefault);

                if (i % 10 == 0)
                {
                    Log.v("U8Parser", i + ": " + simplified);
                }

                i++;
            }
        }
        catch (Exception e) {
            Log.e("MYAPP", "exception", e);
        }
    }
}
