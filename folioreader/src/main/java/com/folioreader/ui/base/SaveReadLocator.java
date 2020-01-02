package com.folioreader.ui.base;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.folioreader.model.dictionary.Dictionary;
import com.folioreader.model.sqlite.AnnotationDictionaryTable;
import com.folioreader.model.sqlite.AppMetadataTable;
import com.folioreader.model.sqlite.ReadLocatorTable;
import com.folioreader.network.TLSSocketFactory;
import com.folioreader.util.AppUtil;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.List;


public class SaveReadLocator extends AsyncTask<String, Void, Void> {

    private static final String TAG = "DictionaryTask";

    private WeakReference<Context> context;

    public SaveReadLocator( Context context) {
        this.context = new WeakReference<>(context);
    }

    @Override
    protected Void doInBackground(String... strings) {
        try {

            ReadLocatorTable table = new ReadLocatorTable(context.get());

            table.save(strings[0], strings[1]);

            AppMetadataTable metadataTable = new AppMetadataTable(context.get());
            metadataTable.save(AppMetadataTable.LAST_EBOOK, strings[0]);


        } catch (Exception e) {
            Log.e(TAG, "SaveReadLocator failed", e);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void n) {
        super.onPostExecute(null);

        cancel(true);
    }
}
