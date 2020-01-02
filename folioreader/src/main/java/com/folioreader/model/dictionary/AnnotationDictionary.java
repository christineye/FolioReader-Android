package com.folioreader.model.dictionary;

import android.content.Context;

import com.folioreader.model.sqlite.AnnotationDictionaryTable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnotationDictionary {

    private Map<String, List<AnnotationDictionaryTable.AnnotationDefinition>> mMap = new HashMap<>();

    public void InsertWord(String word, List<AnnotationDictionaryTable.AnnotationDefinition> definitions)
    {
        mMap.put(word, definitions);
    }


}
