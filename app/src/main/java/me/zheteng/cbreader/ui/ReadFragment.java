/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.ui;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ObservableWebView;
import com.github.ksoichiro.android.observablescrollview.ScrollState;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;
import me.zheteng.cbreader.BuildConfig;
import me.zheteng.cbreader.MainApplication;
import me.zheteng.cbreader.R;
import me.zheteng.cbreader.model.Article;
import me.zheteng.cbreader.model.NewsContent;
import me.zheteng.cbreader.ui.widget.MaterialProgressBar;
import me.zheteng.cbreader.utils.APIUtils;
import me.zheteng.cbreader.utils.volley.GsonRequest;

/**
 * 阅读详情页
 */
public class ReadFragment extends Fragment implements ObservableScrollViewCallbacks {
    public static final String ACTION_DATA_LOADED = "me.zheteng.cbreader.ReadActivity.DATA_LOADED";

    public static final String ARTICLE_SID_KEY = "sid";
    private static final String TAG = "ReadActivity";

    private int mSid;
    private ObservableWebView mWebView;
    private int mToolbarHeight;

    private int mPrevScrollY;
    private MenuItem mCommentsMenuItem;
    private boolean mIsMenuCreated = false;

    private DataLoadedReciever dataLoadedReciever;
    private NewsContent mNewsContent;

    private MaterialProgressBar mProgressBar;

    private Toolbar mToolbar;

    private ReadActivity mActivity;
    private int mStartY;
    private boolean mHasLoaded;

    public static ReadFragment newInstance(Article article) {
        ReadFragment fragment = new ReadFragment();

        Bundle bundle = new Bundle();
        bundle.putInt(ARTICLE_SID_KEY, article.sid);

        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mSid = args.getInt(ARTICLE_SID_KEY, 0);

        setHasOptionsMenu(false);

        dataLoadedReciever = new DataLoadedReciever();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_read, container, false);
        initView(view);
        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {
            if (!mHasLoaded) {
                requestData();
            } else {
                replaceCount();
            }

        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mActivity = ((ReadActivity) getActivity());
        mToolbarHeight = getToolbar().getHeight();
        super.onActivityCreated(savedInstanceState);
    }

    public void requestData() {
        MainApplication.requestQueue.add(new GsonRequest<NewsContent>(APIUtils.getNewsContentUrl(mSid),
                NewsContent.class, null, new Response.Listener<NewsContent>() {
            @Override
            public void onResponse(NewsContent newsContent) {
                mNewsContent = newsContent;
                mActivity.sendBroadcast(new Intent(ACTION_DATA_LOADED + mSid));
                mProgressBar.setVisibility(View.GONE);
                renderContent();
                mHasLoaded = true;
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(mActivity, "加载错误", Toast.LENGTH_SHORT).show();
            }
        }));
    }

    @Override
    public void onStart() {
        super.onStart();

        mActivity.registerReceiver(dataLoadedReciever, new IntentFilter(ACTION_DATA_LOADED + mSid));
    }

    @Override
    public void onStop() {
        super.onStop();

        mActivity.unregisterReceiver(dataLoadedReciever);
    }

    private void initView(View view) {
        mWebView = (ObservableWebView) view.findViewById(R.id.webview);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mWebView.getSettings().setMediaPlaybackRequiresUserGesture(true);
        }

        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.getSettings().setPluginState(WebSettings.PluginState.ON);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebChromeClient(new WebChromeClient() {

        });

        mWebView.setScrollViewCallbacks(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (BuildConfig.DEBUG) {
                WebView.setWebContentsDebuggingEnabled(true);
            }
        }

        mProgressBar = ((MaterialProgressBar) view.findViewById(R.id.loading_progress));

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mWebView.removeAllViews();
        mWebView.destroy();
    }

    public Toolbar getToolbar() {
        if (mActivity == null) {
            return null;
        }
        mToolbar = mActivity.getToolbar();
        return mToolbar;
    }

    public void renderContent() {
        if (mNewsContent != null) {
            String title = mNewsContent.title.replaceAll("\\$", "\\\\\\$");
            String source = mNewsContent.source.replaceAll("\\$", "\\\\\\$");
            String body = mNewsContent.bodytext.replaceAll("\\$", "\\\\\\$");
            String intro = mNewsContent.hometext.replaceAll("\\$", "\\\\\\$");
            String pubTime = mNewsContent.time.replaceAll("\\$", "\\\\\\$");

            String html = getHtmlTemplate();
            html = html.replaceAll("\\$\\{title\\}", title)
                    .replaceAll("\\$\\{time\\}", pubTime)
                    .replaceAll("\\$\\{source\\}", source)
                    .replaceAll("\\$\\{intro\\}", intro)
                    .replaceAll("\\$\\{content\\}", body);

            mWebView.loadData(html, "text/html; charset=UTF-8", null);

        }
    }

    private String getHtmlTemplate() {
        return mActivity.getHtmlTemplate();
    }

    public NewsContent getNewsContent() {
        return mNewsContent;
    }

    @Override
    public void onScrollChanged(int scrollY, boolean b, boolean b2) {
        ViewConfiguration vc = ViewConfiguration.get(mActivity);
        boolean scrollDown = mPrevScrollY < scrollY; // 页面向下滚动, 手指向上滑动, 页面的y值增大
        int slop = Math.abs(scrollY - mStartY);

        if (slop > vc.getScaledTouchSlop()) {
            if (scrollDown) {
                if (mActivity.isToolbarShow()) {
                    mActivity.hideToolbar();
                }
            } else {
                if (!mActivity.isToolbarShow() && scrollY < mToolbarHeight + 100) {
                    mActivity.showToolbar();
                }
            }
        }

        mPrevScrollY = scrollY;
    }

    @Override
    public void onDownMotionEvent() {
        mStartY = mWebView.getCurrentScrollY();
    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {

    }

    private class DataLoadedReciever extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            replaceCount();
        }
    }

    public void replaceCount() {
        mCommentsMenuItem = mActivity.getCommentMenuItem();
        if (mCommentsMenuItem != null && mNewsContent != null) {
            mCommentsMenuItem.setTitle(getString(R.string.action_view_comments) + " (" + mNewsContent.comments + ")");
        }
    }
}
