package com.folioreader.ui.fragment;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.folioreader.Config;
import com.folioreader.Constants;
import com.folioreader.R;
import com.folioreader.model.dictionary.AnnotationChangeManager;
import com.folioreader.model.dictionary.Dictionary;
import com.folioreader.model.dictionary.Wikipedia;
import com.folioreader.model.sqlite.AnnotationDictionaryTable;
import com.folioreader.ui.adapter.AnnotatedDictionaryAdapter;
import com.folioreader.ui.adapter.DictionaryAdapter;
import com.folioreader.ui.base.DictionaryCallBack;
import com.folioreader.ui.base.DictionaryTask;
import com.folioreader.ui.base.SetDefaultDefinitionTask;
import com.folioreader.ui.base.SetLearnedTask;
import com.folioreader.ui.base.WikipediaCallBack;
import com.folioreader.ui.base.WikipediaTask;
import com.folioreader.util.AppUtil;
import com.folioreader.util.UiUtil;
import com.mcxiaoke.koi.Const;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

/**
 * @author gautam chibde on 4/7/17.
 */

public class DictionaryFragment extends DialogFragment
        implements DictionaryCallBack, WikipediaCallBack {

    private static final String TAG = "DictionaryFragment";

    private String word;
    private String wordContext;

    private MediaPlayer mediaPlayer;
    private RecyclerView dictResults;
    private TextView noNetwork, dictionary, wikipedia, wikiWord, def, wordTextView, isLearnedText;
    private ProgressBar progressBar;
    private Button googleSearch;
    private CheckBox isLearnedCheckbox;
    private LinearLayout wikiLayout;
    private WebView wikiWebView;
    private AnnotatedDictionaryAdapter mAdapter;
    private ImageView imageViewClose;

    public DictionaryFragment()
    {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, 0);
        word = getArguments().getString(Constants.SELECTED_WORD);
        wordContext = getArguments().getString(Constants.CONTEXT);
        mediaPlayer = new MediaPlayer();
    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_dictionary, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        noNetwork = (TextView) view.findViewById(R.id.no_network);
        wordTextView = (TextView) view.findViewById(R.id.dictionary_word);
        progressBar = (ProgressBar) view.findViewById(R.id.progress);
        dictResults = (RecyclerView) view.findViewById(R.id.rv_dict_results);

        isLearnedText = (TextView) view.findViewById(R.id.learned_text);
        isLearnedCheckbox = (CheckBox) view.findViewById(R.id.learned_checkbox);
        googleSearch = (Button) view.findViewById(R.id.btn_google_search);
        dictionary = (TextView) view.findViewById(R.id.btn_dictionary);
        wikipedia = (TextView) view.findViewById(R.id.btn_wikipedia);

        wikiLayout = (LinearLayout) view.findViewById(R.id.ll_wiki);
        wikiWord = (TextView) view.findViewById(R.id.tv_word);
        def = (TextView) view.findViewById(R.id.tv_def);
        wikiWebView = (WebView) view.findViewById(R.id.wv_wiki);
        wikiWebView.getSettings().setLoadsImagesAutomatically(true);
        wikiWebView.setWebViewClient(new WebViewClient());
        wikiWebView.getSettings().setJavaScriptEnabled(true);

        wikiWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        dictionary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadDictionary();
            }
        });

        wikipedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadWikipedia();
            }
        });

        googleSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
                intent.putExtra(SearchManager.QUERY, word);
                startActivity(intent);
            }
        });

        final DictionaryCallBack callback = this;
        isLearnedCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SetLearnedTask task = new SetLearnedTask(callback, getContext());
                task.execute(word, isLearnedCheckbox.isChecked() ? "1" : "0");
            }
        });

        imageViewClose = view.findViewById(R.id.btn_close);
        imageViewClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        dictResults.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new AnnotatedDictionaryAdapter(getActivity(), this);

        configureTheme(view);

        loadDictionary();
    }

    private void configureTheme(View view) {

        Config config = AppUtil.getSavedConfig(getContext());
        assert config != null;
        assert getContext() != null;
        final int themeColor = config.getThemeColor();

        UiUtil.setColorIntToDrawable(themeColor, imageViewClose.getDrawable());
        LinearLayout layoutHeader = view.findViewById(R.id.layout_header);
       // layoutHeader.setBackgroundDrawable(UiUtil.getShapeDrawable(themeColor));
        UiUtil.setColorIntToDrawable(themeColor, progressBar.getIndeterminateDrawable());
        UiUtil.setShapeColor(googleSearch, themeColor);

        if (config.isNightMode()) {
            view.findViewById(R.id.toolbar).setBackgroundColor(Color.BLACK);
            view.findViewById(R.id.contentView).setBackgroundColor(Color.BLACK);
            dictionary.setBackgroundDrawable(UiUtil.createStateDrawable(themeColor, Color.BLACK));
            wikipedia.setBackgroundDrawable(UiUtil.createStateDrawable(themeColor, Color.BLACK));
            dictionary.setTextColor(UiUtil.getColorList(Color.BLACK, themeColor));
            wikipedia.setTextColor(UiUtil.getColorList(Color.BLACK, themeColor));
            int nightTextColor = ContextCompat.getColor(getContext(), R.color.night_text_color);
            wikiWord.setTextColor(nightTextColor);
            wikiWord.setBackgroundColor(Color.BLACK);
            def.setTextColor(nightTextColor);
            def.setBackgroundColor(Color.BLACK);
            noNetwork.setTextColor(nightTextColor);
            isLearnedText.setTextColor(nightTextColor);

        } else {
            view.findViewById(R.id.contentView).setBackgroundColor(Color.WHITE);
            dictionary.setTextColor(UiUtil.getColorList(Color.WHITE, themeColor));
            wikipedia.setTextColor(UiUtil.getColorList(Color.WHITE, themeColor));
            dictionary.setBackgroundDrawable(UiUtil.createStateDrawable(themeColor, Color.WHITE));
            wikipedia.setBackgroundDrawable(UiUtil.createStateDrawable(themeColor, Color.WHITE));
            wikiWord.setBackgroundColor(Color.WHITE);
            def.setBackgroundColor(Color.WHITE);
            googleSearch.setTextColor(Color.WHITE);
            isLearnedText.setTextColor(Color.rgb(0,0,0));
        }
    }

    private void loadDictionary() {
        if (noNetwork.getVisibility() == View.VISIBLE || googleSearch.getVisibility() == View.VISIBLE) {
            noNetwork.setVisibility(View.GONE);
            googleSearch.setVisibility(View.GONE);
        }
        wikiWebView.loadUrl("about:blank");
        mAdapter.clear();
        dictionary.setSelected(true);
        wikipedia.setSelected(false);
        wikiLayout.setVisibility(View.GONE);
        dictResults.setVisibility(View.VISIBLE);
        DictionaryTask task = new DictionaryTask(this, getContext());
        task.execute(word, wordContext);
    }

    private void loadWikipedia() {
        if (noNetwork.getVisibility() == View.VISIBLE || googleSearch.getVisibility() == View.VISIBLE) {
            noNetwork.setVisibility(View.GONE);
            googleSearch.setVisibility(View.GONE);
        }
        wikiWebView.loadUrl("about:blank");
        mAdapter.clear();
        wikiLayout.setVisibility(View.VISIBLE);
        dictResults.setVisibility(View.GONE);
        dictionary.setSelected(false);
        wikipedia.setSelected(true);
        WikipediaTask task = new WikipediaTask(this);
        String urlString = null;
        try {
            urlString = Constants.WIKIPEDIA_API_URL + URLEncoder.encode(word, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "-> loadWikipedia", e);
        }
        task.execute(urlString);
    }

    @Override
    public void onError() {
        noNetwork.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        noNetwork.setText("offline");
        googleSearch.setVisibility(View.GONE);
    }

    @Override
    public void onDictionaryDataReceived(Dictionary dictionary) {
        progressBar.setVisibility(View.GONE);
        if (dictionary.getResults().isEmpty()) {
            noNetwork.setVisibility(View.VISIBLE);
            googleSearch.setVisibility(View.VISIBLE);
            noNetwork.setText("Word not found");
        } else {
           // mAdapter.setResults(dictionary.getResults());
            dictResults.setAdapter(mAdapter);
        }
    }

    @Override
    public void onAnnotationChange(AnnotationDictionaryTable.AnnotationDefinition def)
    {
        AnnotationChangeManager.Notify(def);
    }

    @Override
    public void closeRequested()
    {
        dismiss();
    }

    @Override
    public void onAnnotationListReceived(List<AnnotationDictionaryTable.AnnotationDefinition> defs)
    {
        progressBar.setVisibility(View.GONE);
        if (defs == null || defs.isEmpty()) {
            noNetwork.setVisibility(View.VISIBLE);
            googleSearch.setVisibility(View.VISIBLE);
            noNetwork.setText("Word not found");
        } else {
            wordTextView.setText(word);
            mAdapter.setResults(word, defs);
            dictResults.setAdapter(mAdapter);

            AnnotationDictionaryTable.AnnotationDefinition annotationDefinition = defs.get(0);
            isLearnedCheckbox.setChecked(annotationDefinition.Learned > 0);
        }
    }

    @Override
    public void setDefaultDefinitionForWord(String word, int id)
    {
        SetDefaultDefinitionTask task = new SetDefaultDefinitionTask(this, getContext());
        task.execute(word, Integer.toString(id));
    }


    @Override
    @SuppressWarnings("PMD.InefficientEmptyStringCheck")
    public void onWikipediaDataReceived(Wikipedia wikipedia) {
        wikiWord.setText(wikipedia.getWord());
        if (wikipedia.getDefinition().trim().isEmpty()) {
            def.setVisibility(View.GONE);
        } else {
            String definition = "\"" +
                    wikipedia.getDefinition() +
                    "\"";
            def.setText(definition);
        }
        wikiWebView.loadUrl(wikipedia.getLink());
    }

    //TODO
    @Override
    public void playMedia(String url) {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.setDataSource(url);
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e) {
                Log.e(TAG, "playMedia failed", e);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog d = getDialog();
        if (d != null) {
            d.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
}
