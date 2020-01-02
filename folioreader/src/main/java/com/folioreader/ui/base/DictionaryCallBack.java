package com.folioreader.ui.base;

import com.folioreader.model.dictionary.Dictionary;
import com.folioreader.model.sqlite.AnnotationDictionaryTable;

import java.util.List;

/**
 * @author gautam chibde on 4/7/17.
 */

public interface DictionaryCallBack extends BaseMvpView {

    void onDictionaryDataReceived(Dictionary dictionary);

    void onAnnotationListReceived(List<AnnotationDictionaryTable.AnnotationDefinition> defs);

    void setDefaultDefinitionForWord(String word, int id);

    void onAnnotationChange(AnnotationDictionaryTable.AnnotationDefinition def);

    void closeRequested();

    //TODO
    void playMedia(String url);
}
