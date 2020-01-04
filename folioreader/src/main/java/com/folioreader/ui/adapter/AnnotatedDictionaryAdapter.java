package com.folioreader.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.folioreader.Config;
import com.folioreader.R;
import com.folioreader.model.sqlite.AnnotationDictionaryTable;
import com.folioreader.ui.base.DictionaryCallBack;
import com.folioreader.util.AppUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author gautam chibde on 4/7/17.
 */

public class AnnotatedDictionaryAdapter extends RecyclerView.Adapter<AnnotatedDictionaryAdapter.DictionaryHolder> {

    private List<AnnotationDictionaryTable.AnnotationDefinition> results;
    private Context context;
    private DictionaryCallBack callBack;
    private static Config config;
    private String word;

    public AnnotatedDictionaryAdapter(Context context, DictionaryCallBack callBack) {
        this.results = new ArrayList<>();
        this.context = context;
        this.callBack = callBack;
        config = AppUtil.getSavedConfig(context);
    }

    @Override
    public DictionaryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DictionaryHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dictionary_definition, parent, false));
    }

    @Override
    @SuppressWarnings("PMD.InefficientEmptyStringCheck")
    public void onBindViewHolder(DictionaryHolder holder, int position) {
        final AnnotationDictionaryTable.AnnotationDefinition res = results.get(position);

        if (res.Word.equalsIgnoreCase(word) && res.IsDefault)
        {
            holder.name.setTextSize(20);
            holder.pinyin.setTextSize(20);
            holder.definition.setTextSize(20);

            if (config.isNightMode()) {
                holder.rootView.setBackgroundColor(Color.rgb(36, 116, 153));
            }
            else
            {
                holder.rootView.setBackgroundColor(Color.rgb(98, 188, 229));
            }
        }
        else if (!word.contains(res.Word))
        {
            if (config.isNightMode())
            {
                holder.rootView.setBackgroundColor(Color.rgb(49, 89, 55));
            }
            else
            {
                holder.rootView.setBackgroundColor(Color.rgb(98, 229, 118));
            }
        }

        holder.name.setTypeface(Typeface.DEFAULT_BOLD);
        holder.name.setText(res.Word);

        holder.pinyin.setText(res.Romanization);

        holder.definition.setText(res.Definition);

        if (this.word.equalsIgnoreCase(res.Word)) {
            holder.radioButton.setChecked(res.IsDefault);


            holder.radioButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callBack.setDefaultDefinitionForWord(res.Word, res.DefinitionId);
                }
            });
        }
        else
        {
            holder.radioButton.setVisibility(View.INVISIBLE);
        }
    }



    public void setResults(String word, List<AnnotationDictionaryTable.AnnotationDefinition> resultsList) {
        this.word = word;
        if (resultsList != null && !resultsList.isEmpty()) {
            results.clear();
            results.addAll(resultsList);
            notifyDataSetChanged();
        }
    }

    public void clear() {
        results.clear();
        notifyItemRangeRemoved(0, results.size());
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    public static class DictionaryHolder extends RecyclerView.ViewHolder {
        private TextView name, definition, pinyin;
        private RadioButton radioButton;
        private View rootView;
        //TODO private ImageButton sound;

        public DictionaryHolder(View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.definition_word);
            pinyin = (TextView) itemView.findViewById(R.id.pinyin);
            radioButton = (RadioButton) itemView.findViewById(R.id.radioButton);
            definition = (TextView) itemView.findViewById(R.id.definition);


            rootView = itemView.findViewById(R.id.root_view);

            if (config.isNightMode()) {
                rootView.setBackgroundColor(Color.BLACK);
                int nightTextColor = ContextCompat.getColor(itemView.getContext(),
                        R.color.night_text_color);
                name.setTextColor(nightTextColor);
                pinyin.setTextColor(nightTextColor);
                definition.setTextColor(nightTextColor);
            } else {
                rootView.setBackgroundColor(Color.WHITE);
            }
        }
    }
}
