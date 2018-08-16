package com.example.fillintheblankgame.main;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;


import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager;
import com.beloo.widget.chipslayoutmanager.SpacingItemDecoration;
import com.example.fillintheblankgame.R;
import com.example.fillintheblankgame.network.ApiManager;
import com.wiki.wikipediafillintheblankgame.eventbus.ItemSelectEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;
import java.util.StringTokenizer;

import butterknife.BindView;
import butterknife.ButterKnife;

import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchScreenFragment extends Fragment implements SearchView.OnQueryTextListener,DialogInterface.OnDismissListener{
    @BindView(R.id.recyclerView) RecyclerView recyclerView;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.search) SearchView searchView;
    @BindView(R.id.feed_loader) ContentLoadingProgressBar loader;
    @BindView(R.id.tv_error_message) TextView tvErrorMessage;
    @BindView(R.id.replay) Button replayButton;
    @BindView(R.id.submit) Button submitButton;
    @BindView(R.id.frameLayout) FrameLayout frameLayout;
    @BindView(R.id.scrollView) ScrollView scrollView;
    @BindView(R.id.updated) TextView updatedParagraph;
    @BindView(R.id.original) TextView originalParagraph;


    MissingItemAdapter missingItemAdapter;
    private String [] nameArray;
    private int MAX = 15, MIN = 0;
    private int MAX_VAL = 0, MIN_VAL = 0;
    private ArrayList<String> paragraph = new ArrayList<>();
    private ArrayList<String> missingWordsList = new ArrayList<>();
    private ArrayList<String> paragraphArrayList = new ArrayList<>();
    private ArrayList<String> originalWords = new ArrayList<>();
    private int updateIndexWord = -1, numberOfViewsShowInOneLine = 0;
    private int ten = 0, twenty = 0, thirty = 0;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.query_search_fragment, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            toolbar.setTitle(R.string.search);
        }

        if ((getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK) ==
                Configuration.SCREENLAYOUT_SIZE_LARGE) {
            // on a large screen device ...
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                numberOfViewsShowInOneLine = twenty;
            } else {
                numberOfViewsShowInOneLine = thirty;
            }

        }else {
            // on a small screen device ...
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                numberOfViewsShowInOneLine = ten;
            } else {
                numberOfViewsShowInOneLine = twenty;
            }
        }
        nameArray  = getResources().getStringArray(R.array.names);
        searchWiki(nameArray[getRandomNumber(MIN, MAX)]);

        //LayoutManager for showing text as a paragraph
        ChipsLayoutManager chipsLayoutManager = ChipsLayoutManager.newBuilder(getActivity())
                .setChildGravity(Gravity.TOP)
                .setScrollingEnabled(false)
                .setMaxViewsInRow(10)
                .setGravityResolver(position -> Gravity.CENTER)
                .setOrientation(ChipsLayoutManager.HORIZONTAL)
                .setRowStrategy(ChipsLayoutManager.STRATEGY_DEFAULT )
                .withLastRow(true)
                .build();

        recyclerView.setLayoutManager(chipsLayoutManager);
        Activity activity = getActivity();
        if (activity != null && !activity.isFinishing()){
            recyclerView.addItemDecoration(new SpacingItemDecoration(activity.getResources().getDimensionPixelOffset(R.dimen.nav_header_vertical_spacing),
                    activity.getResources().getDimensionPixelOffset(R.dimen.nav_header_vertical_spacing)));
        }
        ViewCompat.setLayoutDirection(recyclerView, ViewCompat.LAYOUT_DIRECTION_INHERIT);

        missingItemAdapter = new MissingItemAdapter(paragraph,getActivity());
        recyclerView.setAdapter(missingItemAdapter);
        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getActivity(), (view, position) -> {
                    String word = paragraph.get(position);
                    if(word.contains("___") || missingWordsList.contains(word)){
                        updateIndexWord = position;
                        SettingsManager.getInstance().itemPosition = position;
                        //Open Dialog Fragemnt for showing missing words
                        callDialogFragment();
                    }
                })
        );


        searchView.setOnQueryTextListener(this);
        searchView.setFocusable(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_search).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_clear:
                paragraph.clear();
                missingItemAdapter.updateAdapter(paragraph);
                break;
            case R.id.refresh:
                getAnotherParagraph();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        searchWiki(s);
        hideKeyboard(getActivity(), getView());
        return true;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        return true;
    }

    private void getAnotherParagraph(){
        MIN_VAL = 0; MAX_VAL = 0;
        SettingsManager.getInstance().itemName = null;
        SettingsManager.getInstance().itemPosition = -1;
        replayButton.setVisibility(View.GONE);
        submitButton.setVisibility(View.VISIBLE);
        frameLayout.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);
        searchWiki(nameArray[getRandomNumber(MIN, MAX)]);
    }

    @OnClick(R.id.submit)
    void openConfirmDialog(){
        confirmPostDialog();
    }

    @OnClick(R.id.replay)
    void playAnotherGame(){
        getAnotherParagraph();
    }

    //search in wiki
    private void searchWiki(String query){
        if(TextUtils.isEmpty(query) && getActivity() == null){
            return;
        }
        if(!isNetworkAvailable(getActivity())){
            loader.hide();
            showInternetErrorDialog();
            return;
        }

        loader.show();
        String url = ApiManager.getInstance().remainingUrl + query;
        Call<ResponseBody> request = ApiManager.getInstance().searchApi(url);
        request.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()){
                    loader.hide();
                    handleResponse(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                loader.hide();
            }
        });
    }

    private void showInternetErrorDialog(){
        if(getActivity() == null){
            return;
        }
        new AlertDialog.Builder(getActivity())
                .setMessage(getResources().getString(R.string.internet_error))
                .setPositiveButton("OK", null).show();
    }

    //Separate words from the string and add in ArrayList
    private void loadAdapter(String extract){
        if(extract.isEmpty()){
            tvErrorMessage.setVisibility(View.VISIBLE);
        }else {
            tvErrorMessage.setVisibility(View.GONE);
        }

        StringTokenizer tok = new StringTokenizer(extract);
        paragraph.clear();
        originalWords.clear();
        paragraphArrayList.clear();
        missingWordsList.clear();

        while (tok.hasMoreTokens()) {
            String word = tok.nextToken();
            originalWords.add(word);
            if(word.contains(".") || word.contains("!") || word.contains("?")){
                MAX_VAL = paragraph.size() - 1;
                int index = getRandomNumber(MIN_VAL, MAX_VAL);
                missingWordsList.add(paragraph.get(index));
                paragraph.set(index, "_____");
                paragraph.add(word);
                MIN_VAL = MAX_VAL;
                if(missingWordsList.size() == 10){
                    break;
                }

            }else {
                paragraph.add(word);
            }
        }
        paragraphArrayList.addAll(paragraph);
        missingItemAdapter.updateAdapter(paragraph);
    }

    private void handleResponse(ResponseBody body){
        try {
            if (body == null){
                return;
            }

            String jsonStr = body.string();
            JSONObject object = new JSONObject(jsonStr);
            JSONObject queryObject = object.optJSONObject("query");
            if (queryObject != null){
                JSONObject pages = queryObject.optJSONObject("pages");
                if (pages != null){
                    Iterator<String> keys = pages.keys();
                    while (keys.hasNext()) {
                        String userId = keys.next();
                        JSONObject eventToMessagesJson = pages.optJSONObject(userId);
                        loadAdapter(eventToMessagesJson.getString("extract"));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void hideKeyboard(Context context, View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    private int getRandomNumber(int min, int max){
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }


    private void callDialogFragment(){
        try {
            if (this.isAdded()){
                FragmentTransaction ft = getChildFragmentManager().beginTransaction();
                DialogFragment newFragment = MyBrandDialogFragment.newInstance(missingWordsList);
                newFragment.show(ft, "dialog");
            }
        } catch (IllegalStateException e){

        }
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {

    }

    public static class MyBrandDialogFragment extends DialogFragment {
        @BindView(R.id.recyclerView) RecyclerView mRecyclerView;
        private MissingWordAdapter missingWordAdapter;
        private ArrayList<String> missingWordList = new ArrayList<>();
        // this method create view for your Dialog
        public MyBrandDialogFragment() {

        }

        public static MyBrandDialogFragment newInstance(ArrayList<String> missingArrayList) {
            MyBrandDialogFragment myDialogFragment = new MyBrandDialogFragment();
            myDialogFragment.missingWordList = missingArrayList;
            return myDialogFragment;
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            //inflate layout with recycler view
            View v = inflater.inflate(R.layout.missing_item_layout, container, false);
            ButterKnife.bind(this,v);
            return v;
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            Collections.shuffle(missingWordList);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            missingWordAdapter = new MissingWordAdapter(missingWordList, getActivity());
            mRecyclerView.setAdapter(missingWordAdapter);
            mRecyclerView.addOnItemTouchListener(
                    new RecyclerItemClickListener(getActivity(), (view1, position) -> {
                        SettingsManager.getInstance().itemName = missingWordList.get(position);
                        getDialog().dismiss();
                    })
            );
        }

        @OnClick(R.id.cancel)
        void closeDialog(){
            getDialog().dismiss();
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);
            EventBus.getDefault().post(new ItemSelectEvent());

        }

        @Override
        public void onStart() {
            super.onStart();
            Dialog dialog = getDialog();
            if (dialog != null) {
                int width = ViewGroup.LayoutParams.MATCH_PARENT;
                int height = ViewGroup.LayoutParams.WRAP_CONTENT;
                dialog.getWindow().setLayout(width, height);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

            }
        }
    }

    //Listen Event Fire when click on any missing word
    @Subscribe
    public void updateAdapter(ItemSelectEvent itemSelectEvent){
        String word = SettingsManager.getInstance().itemName;
        if(!TextUtils.isEmpty(word)){
            paragraph.set(updateIndexWord, word);
            missingItemAdapter.updateSingleWord(word, updateIndexWord);
        }
    }

    private void calculateScore(){
        int score = 0;
        for(int index = 0; index < originalWords.size(); index++){
            if(!paragraph.get(index).equalsIgnoreCase(originalWords.get(index))){
                score++;
            }
        }
        String result = ""+(missingWordsList.size() - score) +"/" + missingWordsList.size() ;
        scorePostDialog(result);
    }


    private void confirmPostDialog(){
        if(getActivity() == null){
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getActivity().getString(R.string.confirm));
        builder.setCancelable(true);
        builder.setPositiveButton("YES", (dialog, which) -> {
            replayButton.setVisibility(View.VISIBLE);
            submitButton.setVisibility(View.GONE);
            calculateScore();
            dialog.dismiss();

        });

        builder.setNegativeButton("NO", (dialog, which) -> dialog.dismiss());
        final AlertDialog alert = builder.create();
        alert.setOnShowListener(arg0 -> {
            alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
            alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(getActivity(), R.color.gray));

        });
        alert.show();
        TextView messageView = alert.findViewById(android.R.id.message);
        assert messageView != null;
        messageView.setGravity(Gravity.CENTER);
        Button btnPositive = alert.getButton(AlertDialog.BUTTON_POSITIVE);
        Button btnNegative = alert.getButton(AlertDialog.BUTTON_NEGATIVE);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) btnPositive.getLayoutParams();
        layoutParams.weight = 10;
        btnPositive.setLayoutParams(layoutParams);
        btnNegative.setLayoutParams(layoutParams);
    }

    private void scorePostDialog(String result){
        if(getActivity() == null){
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getActivity().getString(R.string.score) + result);
        builder.setCancelable(true);
        builder.setPositiveButton("REVIEW", (dialog, which) -> {
            frameLayout.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);
            updatedParagraph.setText(getColorStringBuilder(paragraph,Color.RED));
            originalParagraph.setText(getColorStringBuilder(originalWords,Color.BLUE));

            dialog.dismiss();

        });

        builder.setNegativeButton("NO", (dialog, which) -> dialog.dismiss());
        final AlertDialog alert = builder.create();
        alert.setOnShowListener(arg0 -> {
            alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
            alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(getActivity(), R.color.gray));

        });
        alert.show();
        TextView messageView = alert.findViewById(android.R.id.message);
        assert messageView != null;
        messageView.setGravity(Gravity.CENTER);
        Button btnPositive = alert.getButton(AlertDialog.BUTTON_POSITIVE);
        Button btnNegative = alert.getButton(AlertDialog.BUTTON_NEGATIVE);

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) btnPositive.getLayoutParams();
        layoutParams.weight = 10;
        btnPositive.setLayoutParams(layoutParams);
        btnNegative.setLayoutParams(layoutParams);
    }

    private SpannableStringBuilder getColorStringBuilder(ArrayList<String> arrayList, int color){
        SpannableStringBuilder builder = new SpannableStringBuilder();
        int matchedcolor;
        for(int index = 0; index < paragraphArrayList.size(); index++){
            if(paragraphArrayList.get(index).contains("___")) {
                if(paragraph.get(index).equalsIgnoreCase(originalWords.get(index))){
                    matchedcolor = Color.BLUE;
                }else {
                    matchedcolor = color;
                }
                SpannableString redSpannable = new SpannableString(arrayList.get(index));
                redSpannable.setSpan(new ForegroundColorSpan(matchedcolor), 0, arrayList.get(index).length(), 0);
                builder.append(redSpannable);
                builder.append(" ");
            }else {
                builder.append(paragraphArrayList.get(index));
                builder.append(" ");
            }
        }
        return builder;

    }
    private static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
