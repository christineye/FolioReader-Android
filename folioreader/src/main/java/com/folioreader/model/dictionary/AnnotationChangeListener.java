package com.folioreader.model.dictionary;

import com.folioreader.model.sqlite.AnnotationDictionaryTable;

public interface AnnotationChangeListener
{
    void handleAnnotationChange(AnnotationDictionaryTable.AnnotationDefinition definition);
}