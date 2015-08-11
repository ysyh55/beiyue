/*
 * Copyright (C) 2015 junyuecao@gmail.com All Rights Reserved.
 */
package me.zheteng.cbreader.ui;

import java.util.List;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.google.gson.Gson;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import me.zheteng.cbreader.MainApplication;
import me.zheteng.cbreader.R;
import me.zheteng.cbreader.model.TopComment;
import me.zheteng.cbreader.ui.widget.MaterialProgressBar;
import me.zheteng.cbreader.utils.APIUtils;
import me.zheteng.cbreader.utils.PrefUtils;
import me.zheteng.cbreader.utils.Utils;
import me.zheteng.cbreader.utils.volley.GsonRequest;

/**
 * 显示热门评论
 */
public class TopCommentsFragment extends Fragment {

    protected SwipeRefreshLayout mSwipeRefreshLayout;
    protected ObservableRecyclerView mRecyclerView;
    protected MaterialProgressBar mProgressBar;
    private TextView mNoDataHint;
    private MainActivity mActivity;
    private Toolbar mToolbar;
    private int mToolbarHeight;
    private LinearLayoutManager mLayoutManager;
    private TopCommentsListAdapter mAdapter;
    private boolean mLoadingData;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_top_comments, container, false);

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
        mActivity.setTitle(R.string.top_comments_fragment_title);

        setupRecyclerView();
        trySetupSwipeRefresh();


        if (!loadCachedData()) {
            refreshData(APIUtils.getRecommendCommentUrl());
        }
    }

    protected boolean loadCachedData() {
        mLoadingData = true;
        String json = PrefUtils.getCacheOfKey(getActivity(), PrefUtils.KEY_TOP_COMMENTS);

        if (TextUtils.isEmpty(json)) {
            return false;
        }
        Gson gson = new Gson();

        TopComment[] articles = gson.fromJson(json, TopComment[].class);
        List<TopComment> list = Utils.getListFromArray(articles);
        mAdapter.swapData(list);
        mProgressBar.setVisibility(View.GONE);

        mLoadingData = false;
        return true;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        if (isVisibleToUser) {

        }
    }

    protected void refreshData(String url) {
        mLoadingData = true;
        final GsonRequest request = new GsonRequest<TopComment[]>(url, TopComment[].class, null,
                new Response.Listener<TopComment[]>() {
                    @Override
                    public void onResponse(TopComment[] s) {
                        List<TopComment> comments = Utils.getListFromArray(s);
                        mAdapter.swapData(comments);
                        Gson gson = new Gson();
                        PrefUtils.saveCacheOfKey(mActivity, PrefUtils.KEY_TOP_COMMENTS, gson.toJson(s));
                        mSwipeRefreshLayout.setRefreshing(false);
                        mProgressBar.setVisibility(View.GONE);
                        mLoadingData = false;
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                mLoadingData = false;
                mSwipeRefreshLayout.setRefreshing(false);
                Toast.makeText(mActivity, "加载错误", Toast.LENGTH_SHORT).show();
            }
        });

        MainApplication.requestQueue.add(request);
    }

    protected void setupRecyclerView() {
        mLayoutManager = new LinearLayoutManager(mActivity);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(false);

        mAdapter = new TopCommentsListAdapter(mActivity, null);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setScrollViewCallbacks(new ShowHideToolbarListener(mActivity, mRecyclerView));
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
                    refreshData(APIUtils.getRecommendCommentUrl());
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
        int top = mActivity.getToolbar().getHeight();
        mSwipeRefreshLayout.setProgressViewOffset(false,
                top + progressBarStartMargin, top + progressBarEndMargin);

        mSwipeRefreshLayout.setProgressViewEndTarget(false, top + progressBarEndMargin + top);
    }

    private void initViews(View view) {
        mRecyclerView = ((ObservableRecyclerView) view.findViewById(R.id.top_comments_list));

        mProgressBar = (MaterialProgressBar) view.findViewById(R.id.loading_progress);
        mNoDataHint = (TextView) view.findViewById(R.id.no_data_hint);
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
    }

    private class TopCommentsListAdapter extends RecyclerView.Adapter implements View.OnClickListener {

        private List<TopComment> mData;
        private MainActivity mContext;

        public TopCommentsListAdapter(MainActivity mActivity, List<TopComment> data) {
            mContext = mActivity;
            mData = data;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.top_comment_list_item, parent, false);

            TopCommentViewHolder viewHolder = new TopCommentViewHolder(view);
            view.setOnClickListener(this);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            TopCommentViewHolder holder1 = (TopCommentViewHolder) holder;
            TopComment comment = mData.get(position);
            holder1.mName.setText(comment.getUsername());
            holder1.mContent.setText(comment.comment);
            holder1.mSubject.setText(comment.subject);
        }

        public List<TopComment> swapData(List<TopComment> articles) {
            if (mData == articles) {
                return null;
            }
            List<TopComment> oldData = mData;
            mData = articles;
            if (articles != null) {
                this.notifyDataSetChanged();
            }
            return oldData;
        }

        @Override
        public int getItemCount() {
            return mData == null ? 0 : mData.size();
        }

        @Override
        public void onClick(View v) {
            int position = mRecyclerView.getChildPosition(v);
            if (mData != null) {
                Intent intent = new Intent(mContext, ReadActivity.class);
                intent.putExtra(ReadActivity.TOP_COMMENT_SID_KEY, Integer.parseInt(mData.get(position).sid));
                intent.putExtra(ReadActivity.FROM_TOP_COMMENT_KEY, true);

                mContext.startActivity(intent);
            }
        }

        public class TopCommentViewHolder extends RecyclerView.ViewHolder {

            private TextView mSubject;
            private TextView mName;
            private TextView mContent;

            public TopCommentViewHolder(View itemView) {
                super(itemView);
                mContent = (TextView) itemView.findViewById(R.id.content);
                mName = (TextView) itemView.findViewById(R.id.name);
                mSubject = (TextView) itemView.findViewById(R.id.subject);

            }
        }
    }
}
