/*
 * Copyright (C) 2015 junyuecao@gmail.com All Rights Reserved.
 */
package me.zheteng.cbreader.ui;

import java.util.List;

import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import me.zheteng.cbreader.R;
import me.zheteng.cbreader.model.Article;
import me.zheteng.cbreader.utils.APIUtils;
import me.zheteng.cbreader.utils.UIUtils;

/**
 * TODO 记得添加注释
 */
public class NewsListFragment extends BaseListFragment implements ObservableScrollViewCallbacks,
        SharedPreferences.OnSharedPreferenceChangeListener {

    public static final int CURRENT_STATE_REQUEST = 1;

    public static final String TOOLBAR_HEIGHT_KEY = "toolbar_height";
    private ObservableRecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;

    private Toolbar mToolbar; // sticky

    private int mToolbarHeight;
    private SharedPreferences mPref;

    public static NewsListFragment newInstance(int toolbarHeight) {
        NewsListFragment fragment = new NewsListFragment();

        Bundle bundle = new Bundle();
        bundle.putInt(TOOLBAR_HEIGHT_KEY, toolbarHeight);

        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        mToolbarHeight = bundle.getInt(TOOLBAR_HEIGHT_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_newslist, container, false);

        initViews(view);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        mToolbar = mActivity.getToolbar();
        mToolbar.getBackground().setAlpha(255);
        mActivity.showToolbar();
        mToolbarHeight = mToolbar.getHeight();
        mActivity.setTitle(R.string.app_name);
        mPref = PreferenceManager.getDefaultSharedPreferences(mActivity);

        setupRecyclerView();
        trySetupSwipeRefresh();

        loadCachedData();
        if (mPref.getBoolean(getString(R.string.pref_autoload_when_start_key), true)) {
            Log.d("junyue", "auto_load_data");
            refreshData(APIUtils.getArticleListsUrl());
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        mPref.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPref.registerOnSharedPreferenceChangeListener(this);
        mAdapter.setIsShowThumb(mPref.getBoolean(getString(R.string.pref_autoload_image_in_list_key), true));
        String value =
                mPref.getString(getString(R.string.pref_list_style_key), getString(R.string.pref_card_style_value));
        mAdapter.setStyle(value);
        mAdapter.notifyDataSetChanged();

    }

    private void initViews(View view) {

        mRecyclerView = (ObservableRecyclerView) view.findViewById(R.id.feed_list);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);

    }

    public Toolbar getToolbar() {
        if (mActivity == null) {
            return null;
        }
        mToolbar = mActivity.getToolbar();
        return mToolbar;
    }


    protected void setupRecyclerView() {
        mRecyclerView.setScrollViewCallbacks(this);
        mLayoutManager = new LinearLayoutManager(mActivity);

        //        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
        //
        //            @Override
        //            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        //                super.onScrolled(recyclerView, dx, dy);
        //
        //                visibleItemCount = mRecyclerView.getChildCount();
        //                totalItemCount = mLayoutManager.getItemCount();
        //                firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
        //
        //                if (loading) {
        //                    if (totalItemCount > previousTotal) {
        //                        loading = false;
        //                        previousTotal = totalItemCount;
        //                    }
        //                }
        //                if (!loading && (totalItemCount - visibleItemCount)
        //                        <= (firstVisibleItem + visibleThreshold)) {
        //                    // End has been reached
        //
        //                    Log.i("...", "end called");
        //
        //                    if (!mLoadingData) {
        //                        String url = APIUtils
        //                                .getArticleListUrl(0, mAdapter.getData().get(mAdapter.getData().size() - 1)
        // .sid);
        //
        //                        loadMoreArticles(url);
        //                    }
        //
        //                    loading = true;
        //                }
        //            }
        //        });

        mRecyclerView.setScrollViewCallbacks(new ShowHideToolbarListener(mActivity, mRecyclerView));
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(false);


        mAdapter = new ArticleListAdapter(mActivity,
                null,
                mRecyclerView,
                mPref.getBoolean(getString(R.string.pref_autoload_image_in_list_key), true), false);
        String style = mPref.getString(mActivity.getString(R.string.pref_list_style_key),
                mActivity.getString(R.string.pref_card_style_value));
        mAdapter.setStyle(style);

        mAdapter.setOnLoadMoreListener(new ArticleListAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                String url = APIUtils
                        .getArticleListUrl(0, mAdapter.getData().get(mAdapter.getData().size() - 1).sid);
                loadMoreArticles(url);
            }
        });
        mRecyclerView.setAdapter(mAdapter);
    }

    private void trySetupSwipeRefresh() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setColorSchemeResources(
                    R.color.refresh_progress_1,
                    R.color.refresh_progress_2,
                    R.color.refresh_progress_3);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    refreshData(APIUtils.getArticleListsUrl());
                }
            });
            updateSwipeRefreshProgressBarTop();
        }
    }

    private void updateSwipeRefreshProgressBarTop() {
        if (mSwipeRefreshLayout == null) {
            return;
        }

        int progressBarStartMargin = getResources().getDimensionPixelSize(
                R.dimen.swipe_refresh_progress_bar_start_margin);
        int progressBarEndMargin = getResources().getDimensionPixelSize(
                R.dimen.swipe_refresh_progress_bar_end_margin);
        int top = (int) UIUtils.dpToPixels(mActivity, 54);
        mSwipeRefreshLayout.setProgressViewOffset(false,
                top + progressBarStartMargin, top + progressBarEndMargin);

        mSwipeRefreshLayout.setProgressViewEndTarget(false, top + progressBarEndMargin + top);
    }

    @Override
    public void onScrollChanged(int scrollY, boolean b, boolean b2) {
    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CURRENT_STATE_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                List<Article> list = data.getParcelableArrayListExtra(ReadActivity.KEY_RESULT_ARTICELS);
                int position = data.getIntExtra(ReadActivity.KEY_RESULT_POSITION, 0);
                mAdapter.swapData(list);
            }
        }
    }



    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_autoload_image_in_list_key))) {
            boolean showThumb = sharedPreferences.getBoolean(key, true);
            mAdapter.setIsShowThumb(showThumb);
        }
    }
}
