package com.example.fillintheblankgame.main;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.fillintheblankgame.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MissingItemAdapter extends RecyclerView.Adapter<MissingItemAdapter.ViewHolder> {

    private ArrayList<String> wordsArrayList = new ArrayList<>();
    private LayoutInflater mInflater;
    private Context context;


    // data is passed into the constructor
    public MissingItemAdapter(ArrayList<String> wordsArrayList, Context context) {
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
        updateAdapter(wordsArrayList);
    }

    public void updateAdapter(ArrayList<String> wordsList){
        wordsArrayList.clear();
        wordsArrayList.addAll(wordsList);
        notifyDataSetChanged();
    }

    public void updateSingleWord(String word, int position){
        wordsArrayList.set(position, word);
        notifyItemChanged(position);

    }

    // inflates the row layout from xml when needed
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.words_item_list, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the textview in each row
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String word = wordsArrayList.get(position);
        holder.myTextView.setText(word);
        if(position == SettingsManager.getInstance().itemPosition){
            holder.myTextView.setTextColor(ContextCompat.getColor(context, R.color.colorAccent));
        }

        }

    // total number of rows
    @Override
    public int getItemCount() {
        return  wordsArrayList.size() ;
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.text) TextView myTextView;
        private ViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}

