/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import me.zheteng.cbreader.MainApplication;
import me.zheteng.cbreader.R;
import me.zheteng.cbreader.model.Article;
import me.zheteng.cbreader.model.Topic;
import me.zheteng.cbreader.ui.widget.MaterialProgressBar;
import me.zheteng.cbreader.utils.APIUtils;
import me.zheteng.cbreader.utils.PrefUtils;
import me.zheteng.cbreader.utils.Utils;
import me.zheteng.cbreader.utils.volley.GsonRequest;

/**
 * 分类列表页
 */
public class TopicFragment extends BaseListFragment implements ObservableScrollViewCallbacks,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String KEY_TID = "key_tid";
    private static final String KEY_TITLE = "key_title";

    private MainActivity mActivity;
    private LinearLayoutManager mLayoutManager;
    private boolean mHasLoaded;
    private MaterialProgressBar mProgressBar;
    private TextView mNodataHint;
    private SharedPreferences mPref;

    public static TopicFragment newInstance(Topic topic) {
        String tid = topic.tid;
        String title = topic.title;

        Bundle args = new Bundle();
        args.putString(KEY_TID, tid);
        args.putString(KEY_TITLE, title);
        TopicFragment fragment = new TopicFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private String mTid;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ObservableRecyclerView mRecyclerView;
    private View mTabsContainer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mTid = args.getString(KEY_TID, "");
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
        trySetupSwipeRefresh();

        loadCachedData();
        if (mPref.getBoolean(mActivity.getString(R.string.pref_autoload_when_start_key), true)) {
            Log.d("junyue", "auto_load_data");
            refreshData(APIUtils.getTopicListUrl(mTid));
        }
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

    /**
     * 会自动根据当前的分类来进行缓存
     *
     * @return 是否获取到缓存数据
     */
    protected boolean loadCachedData() {
        mLoadingData = true;
        String json = PrefUtils.getCacheOfKey(getActivity(), PrefUtils.KEY_TOPIC + mTid);

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

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setScrollViewCallbacks(new MyShowHideToolbarListener(mActivity, mRecyclerView, mTabsContainer) {
            @Override
            public void onScrollChanged(int scrollY, boolean b, boolean b2) {
                super.onScrollChanged(scrollY, b, b2);
                visibleItemCount = mRecyclerView.getChildCount();
                totalItemCount = mLayoutManager.getItemCount();
                firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

                if (loading) {
                    if (totalItemCount > previousTotal) {
                        loading = false;
                        previousTotal = totalItemCount;
                    }
                }
                if (!loading && (totalItemCount - visibleItemCount)
                        <= (firstVisibleItem + visibleThreshold)) {
                    // End has been reached

                    Log.i("...", "end called");

                    if (!mLoadingData &&
                            mPref.getBoolean(mActivity.getString(R.string.pref_autoload_when_scroll_key), true)) {
                        String url = APIUtils
                                .getArticleListUrl(0, mAdapter.getData().get(mAdapter.getData().size() - 1)
                                        .sid);

                        loadMoreArticles(url);
                        loading = true;
                    }

                }
            }
        });

        mAdapter = new ArticleListAdapter(mActivity,
                null,
                mRecyclerView,
                mPref.getBoolean(mActivity.getString(R.string.pref_autoload_image_in_list_key), true), false);
        mAdapter.setItemClickable(true);
        String style = mPref.getString(mActivity.getString(R.string.pref_list_style_key),
                mActivity.getString(R.string.pref_card_style_value));
        mAdapter.setStyle(style);
        mAdapter.setOnLoadMoreListener(new ArticleListAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMoreButtonClicked() {
                if (mAdapter.getData().size() == 0) {
                    return;
                }
                String url = APIUtils
                        .getArticleListUrl(0, mAdapter.getData().get(mAdapter.getData().size() - 1).sid);
                loadMoreArticles(url);
            }
        });
        mAdapter.setLoadOnlyOneArticle(false);
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
                    refreshData(APIUtils.getTopicListUrl(mTid));
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
                refreshData(APIUtils.getTopicListUrl(mTid));
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
                        PrefUtils.saveCacheOfKey(mActivity, PrefUtils.KEY_TOPIC + mTid, gson.toJson(s));
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
        int top = mActivity.getToolbar().getHeight();
        mSwipeRefreshLayout.setProgressViewOffset(false,
                top + progressBarStartMargin, top + progressBarEndMargin);

        mSwipeRefreshLayout.setProgressViewEndTarget(false, top + progressBarEndMargin + top);
    }

    private void initViews(View view) {
        mRecyclerView = ((ObservableRecyclerView) view.findViewById(R.id.feed_list));
        mProgressBar = ((MaterialProgressBar) view.findViewById(R.id.loading_progress));

        mNodataHint = (TextView) view.findViewById(R.id.no_data_hint);
        mNodataHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNodataHint.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
                refreshData(APIUtils.getTopicListUrl(mTid));
            }
        });
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);

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
        if (key.equals(mActivity.getString(R.string.pref_autoload_image_in_list_key))) {
            mAdapter.setIsShowThumb(sharedPreferences.getBoolean(key, true));
        }
    }

    public View getTabs() {
        return mTabsContainer;
    }

    public void setTabs(View mTabs) {
        this.mTabsContainer = mTabs;
    }
}
