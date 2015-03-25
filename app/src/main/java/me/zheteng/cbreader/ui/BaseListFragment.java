/*
 * Copyright (C) 2015 junyuecao@gmail.com All Rights Reserved.
 */
package me.zheteng.cbreader.ui;

import java.util.List;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;

import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.widget.Toast;
import me.zheteng.cbreader.MainApplication;
import me.zheteng.cbreader.model.Article;
import me.zheteng.cbreader.utils.PrefUtils;
import me.zheteng.cbreader.utils.Utils;
import me.zheteng.cbreader.utils.volley.GsonRequest;

/**
 * TODO 记得添加注释
 */
public class BaseListFragment extends Fragment {

    protected ArticleListAdapter mAdapter;
    protected MainActivity mActivity;

    // ----- used for load more
    protected int previousTotal = 0;
    protected boolean loading = true;
    protected int visibleThreshold = 5;
    protected int firstVisibleItem, visibleItemCount, totalItemCount;
    protected boolean mLoadingData;
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    // ----- end  used for load more

    // 首页用的,其他要重写方法
    protected boolean loadCachedData() {
        mLoadingData = true;
        String json = PrefUtils.getCacheOfKey(getActivity(), PrefUtils.KEY_ARTICLES);

        if (TextUtils.isEmpty(json)) {
            return false;
        }
        Gson gson = new Gson();

        Article[] articles = gson.fromJson(json, Article[].class);
        List<Article> list = Utils.getListFromArray(articles);
        mAdapter.swapData(list);

        mLoadingData = false;
        return true;

    }

    protected void loadMoreArticles(String url) {
        mLoadingData = true;

        mAdapter.addItem(null); // 添加null多出进度条;

        MainApplication.requestQueue.add(new GsonRequest<Article[]>(url, Article[].class, null,
                new Response.Listener<Article[]>() {
                    @Override
                    public void onResponse(Article[] s) {
                        List<Article> articles = Utils.getListFromArray(s);
                        mAdapter.removeLast(); // 去掉最后的一个, 进度条
                        mAdapter.appendData(articles);
                        mLoadingData = false;
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                mSwipeRefreshLayout.setRefreshing(false);
                mLoadingData = false;
                Toast.makeText(mActivity, "加载错误", Toast.LENGTH_SHORT).show();
            }
        }));
    }

    protected void refreshData(String url) {
        mLoadingData = true;
        final GsonRequest request = new GsonRequest<Article[]>(url, Article[].class, null,
                new Response.Listener<Article[]>() {
                    @Override
                    public void onResponse(Article[] s) {
                        List<Article> articles = Utils.getListFromArray(s);
                        mAdapter.swapData(articles);
                        Gson gson = new Gson();
                        PrefUtils.saveCacheOfKey(mActivity, PrefUtils.KEY_ARTICLES, gson.toJson(s));
                        mSwipeRefreshLayout.setRefreshing(false);
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
}
