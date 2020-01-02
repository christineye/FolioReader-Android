package com.folioreader.ui.base;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.folioreader.model.dictionary.Dictionary;
import com.folioreader.model.sqlite.AnnotationDictionaryTable;
import com.folioreader.network.TLSSocketFactory;
import com.folioreader.util.AppUtil;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.List;

/**
 * @author gautam chibde on 4/7/17.
 */

public class DictionaryTask extends AsyncTask<String, Void, List<AnnotationDictionaryTable.AnnotationDefinition>> {

    private static final String TAG = "DictionaryTask";

    private DictionaryCallBack callBack;
    private WeakReference<Context> context;

    public DictionaryTask(DictionaryCallBack callBack, Context context) {
        this.callBack = callBack;
        this.context = new WeakReference<>(context);
    }

    @Override
    protected List<AnnotationDictionaryTable.AnnotationDefinition> doInBackground(String... strings) {
        String strUrl = strings[0];
        try {


            AnnotationDictionaryTable table = new AnnotationDictionaryTable(context.get());

            return table.getDefinitionsForWordAndSubWords(strings[0]);


        } catch (Exception e) {
            Log.e(TAG, "DictionaryTask failed", e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<AnnotationDictionaryTable.AnnotationDefinition> dictionary) {
        super.onPostExecute(dictionary);
        if (dictionary != null) {
            callBack.onAnnotationListReceived(dictionary);
        } else {
            callBack.onError();
        }
        cancel(true);
    }
}
