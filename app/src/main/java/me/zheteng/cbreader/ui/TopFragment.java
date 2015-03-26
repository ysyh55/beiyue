/*
 * Copyright (C) 2015 junyuecao@gmail.com All Rights Reserved.
 */
package me.zheteng.cbreader.ui;

import java.util.List;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.google.gson.Gson;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import me.zheteng.cbreader.MainApplication;
import me.zheteng.cbreader.R;
import me.zheteng.cbreader.model.Article;
import me.zheteng.cbreader.ui.widget.MaterialProgressBar;
import me.zheteng.cbreader.utils.APIUtils;
import me.zheteng.cbreader.utils.PrefUtils;
import me.zheteng.cbreader.utils.Utils;
import me.zheteng.cbreader.utils.volley.GsonRequest;

/**
 * TODO 记得添加注释
 */
public class TopFragment extends BaseListFragment implements ObservableScrollViewCallbacks,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private MainActivity mActivity;
    private ArticleListAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private boolean mHasLoaded;
    private MaterialProgressBar mProgressBar;
    private TextView mNodataHint;
    private SharedPreferences mPref;

    public static TopFragment newInstance(int i) {
        TopFragment fragment = new TopFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("top_type", i);
        fragment.setArguments(bundle);
        return fragment;
    }

    private int mType;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ObservableRecyclerView mRecyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mType = args.getInt("top_type", 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_top, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        mActivity.getToolbar().getBackground().setAlpha(255);
        mPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
        setupRecyclerView();


    }

    @Override
    public void onResume() {
        super.onResume();
        mPref.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mPref.unregisterOnSharedPreferenceChangeListener(this);
    }

    protected boolean loadCachedData() {
        mLoadingData = true;
        String json = PrefUtils.getCacheOfKey(getActivity(), getCacheKey());

        if (TextUtils.isEmpty(json)) {
            return false;
        }
        Gson gson = new Gson();

        Article[] articles = gson.fromJson(json, Article[].class);
        if (articles.length == 0) {
            return false;
        }
        List<Article> list = Utils.getListFromArray(articles);
        mAdapter.swapData(list);

        mProgressBar.setVisibility(View.GONE);
        mLoadingData = false;
        return true;

    }

    private void setupRecyclerView() {
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
        //                        loadMoreArticles(mApiUrl);
        //                    }
        //
        //                    loading = true;
        //                }
        //            }
        //        });
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(false);


        mAdapter = new ArticleListAdapter(mActivity,
                null,
                false,
                mRecyclerView,
                0,
                mPref.getBoolean(getString(R.string.pref_autoload_image_in_list_key), true));
        mAdapter.setIsFromTopFragment(true);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void trySetupSwipeRefresh(View root) {
        mSwipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipe_refresh_layout);
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setColorSchemeResources(
                    R.color.refresh_progress_1,
                    R.color.refresh_progress_2,
                    R.color.refresh_progress_3);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    refreshData(getAPIUrl());
                }
            });
            updateSwipeRefreshProgressBarTop();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            if (!mHasLoaded) {
                refreshData(getAPIUrl());
            }
        }
    }

    @Override
    protected void refreshData(String url) {
        mLoadingData = true;
        final GsonRequest request = new GsonRequest<Article[]>(url, Article[].class, null,
                new Response.Listener<Article[]>() {
                    @Override
                    public void onResponse(Article[] s) {
                        List<Article> articles = Utils.getListFromArray(s);
                        mAdapter.swapData(articles);
                        Gson gson = new Gson();
                        PrefUtils.saveCacheOfKey(mActivity, getCacheKey(), gson.toJson(s));
                        mSwipeRefreshLayout.setRefreshing(false);
                        mLoadingData = false;
                        mProgressBar.setVisibility(View.GONE);
                        if (articles.size() == 0) {
                            mNodataHint.setText(R.string.empty_data);
                            mNodataHint.setVisibility(View.VISIBLE);
                            mNodataHint.setOnClickListener(null);
                        }
                        mHasLoaded = true;
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                mSwipeRefreshLayout.setRefreshing(false);
                mLoadingData = false;
                mNodataHint.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
                Toast.makeText(mActivity, "加载错误", Toast.LENGTH_SHORT).show();
            }
        });

        MainApplication.requestQueue.add(request);
    }

    private void updateSwipeRefreshProgressBarTop() {
        if (mSwipeRefreshLayout == null) {
            return;
        }

        int progressBarStartMargin = getResources().getDimensionPixelSize(
                R.dimen.swipe_refresh_progress_bar_start_margin);
        int progressBarEndMargin = getResources().getDimensionPixelSize(
                R.dimen.swipe_refresh_progress_bar_end_margin);
        int top = 0;
        mSwipeRefreshLayout.setProgressViewOffset(false,
                top + progressBarStartMargin, top + progressBarEndMargin);

        mSwipeRefreshLayout.setProgressViewEndTarget(false, top + progressBarEndMargin + top);
    }

    private void initViews(View view) {
        trySetupSwipeRefresh(view);
        mRecyclerView = ((ObservableRecyclerView) view.findViewById(R.id.feed_list));
        mProgressBar = ((MaterialProgressBar) view.findViewById(R.id.loading_progress));

        mNodataHint = (TextView) view.findViewById(R.id.no_data_hint);
        mNodataHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNodataHint.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
                refreshData(getAPIUrl());
            }
        });
    }

    private String getAPIUrl() {
        switch (mType) {
            case TopPagerFragment.COUNTER:
                return APIUtils.getTodayRankUrl(APIUtils.TOP_TYPE_COUNTER);
            case TopPagerFragment.COMMENTS:
                return APIUtils.getTodayRankUrl(APIUtils.TOP_TYPE_COMMENTS);
            case TopPagerFragment.DIG:
                return APIUtils.getTodayRankUrl(APIUtils.TOP_TYPE_DIG);
            case TopPagerFragment.TOP10:
                return APIUtils.getTop10Url();
            default:
                throw new UnsupportedOperationException("无效链接");
        }
    }

    private String getCacheKey() {
        switch (mType) {
            case TopPagerFragment.COUNTER:
                return PrefUtils.KEY_TOP_COUNTER;
            case TopPagerFragment.COMMENTS:
                return PrefUtils.KEY_TOP_COMMENT;
            case TopPagerFragment.DIG:
                return PrefUtils.KEY_TOP_DIG;
            case TopPagerFragment.TOP10:
                return PrefUtils.KEY_TOP_10;
            default:
                throw new UnsupportedOperationException("无效Key");
        }
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
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_autoload_image_in_list_key))){
            mAdapter.setIsShowThumb(sharedPreferences.getBoolean(key,true));
        }
    }
}
