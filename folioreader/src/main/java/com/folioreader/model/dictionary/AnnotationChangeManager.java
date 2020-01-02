package com.folioreader.model.dictionary;

import com.folioreader.model.sqlite.AnnotationDictionaryTable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class AnnotationChangeManager {
    private static List<WeakReference<AnnotationChangeListener>> listeners = new ArrayList<>();

    public static void AddListener(AnnotationChangeListener listener)
    {
        listeners.add(new WeakReference<>(listener));
    }

    public static void Notify(AnnotationDictionaryTable.AnnotationDefinition change)
    {
        for (int i = listeners.size() - 1; i >= 0; i--)
        {
            AnnotationChangeListener listener = listeners.get(i).get();

            if (listener == null)
            {
                listeners.remove(i);
            }
            else
            {
                listener.handleAnnotationChange(change);
            }
        }
    }
}
