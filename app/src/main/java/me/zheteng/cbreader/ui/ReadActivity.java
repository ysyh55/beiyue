/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.ui;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ObservableWebView;
import com.github.ksoichiro.android.observablescrollview.ScrollState;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;
import me.zheteng.cbreader.BuildConfig;
import me.zheteng.cbreader.MainApplication;
import me.zheteng.cbreader.R;
import me.zheteng.cbreader.model.NewsContent;
import me.zheteng.cbreader.utils.APIUtils;
import me.zheteng.cbreader.utils.volley.GsonRequest;

/**
 * TODO 记得添加注释
 */
public class ReadActivity extends BaseActivity implements ObservableScrollViewCallbacks {

    public static final String ACTION_MENU_CREATED = "me.zheteng.cbreader.ReadActivity.MENU_CREATED";
    public static final String ACTION_DATA_LOADED = "me.zheteng.cbreader.ReadActivity.DATA_LOADED";

    public static final String ARTICLE_SID_KEY = "sid";

    private int mSid;
    private ObservableWebView mWebView;
    private int mToolbarHeight;

    private static final String BEFORE_TITLE = "<html>\n"
            + "<head>\n"
            + "    <meta content=\"width=device-width,initial-scale=1.0\" name=\"viewport\">\n"
            + "    <style type=\"text/css\">\n"
            + "        body {\n"
            + "        margin: 0;\n"
            + "        margin-top: 50px;\n"
            + "        margin-bottom: 50px;\n"
            + "        padding: 12px;\n"
            + "        }\n"
            + "        h1 {\n"
            + "        font-size: 24px;\n"
            + "        font-weight: 500;\n"
            + "        margin-top: 50px;\n"
            + "        mari\n"
            + "        }\n"
            + "        p {\n"
            + "        color: #242424;\n"
            + "        font-size: 16px;\n"
            + "        line-height: 22px;\n"
            + "        }\n"
            + "        img {\n"
            + "        width: 100%;\n"
            + "        }\n"
            + "        .content {\n"
            + "        margin-top: 48px;\n"
            + "        }\n"
            + "        #time {\n"
            + "        color: #616161;\n"
            + "        font-size: 13px;\n"
            + "        }\n"
            + "        iframe,embed, video,object {\n"
            + "            width: 100%;\n"
            + "            height: 215px;\n"
            + "        }\n"
            + "        #intro{\n"
            + "            margin-top:1em;\n"
            + "            margin-bottom:1em;\n"
            + "            font-size: 14px;\n"
            + "            background: #F5F5F5;\n"
            + "            padding: 10px;"
            + "        }\n" + "#intro p {\n"
            + "font-size: 15px;\n"
            + "margin: 0;\n"
            + "}\n"
            + "    </style>\n"
            + "</head>\n"
            + "<body>\n"
            + "<h1>";

    private static final String AFTER_TITLE_BEFORE_TIME = "</h1>\n"
            + "\n"
            + "<div id=\"time\">";

    public static final String AFTER_TIME_BEFORE_INTRO = "</div><div id=\"intro\">";

    public static final String AFTER_INTRO_BEFORE_CONTENT = "</div>";

    public static final String AFTER_CONTENT = "</body>\n"
            + "</html>";

    private DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
    private int mPrevScrollY;
    private boolean mIsToolbarShow;
    private MenuItem mCommentsMenuItem;
    private boolean mIsMenuCreated = false;
    private boolean mIsLoaded = false;

    private MenuCreatedReciever menuCreatedReciever;
    private DataLoadedReciever dataLoadedReciever;
    private NewsContent mNewsContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_read);

        menuCreatedReciever = new MenuCreatedReciever();
        dataLoadedReciever = new DataLoadedReciever();
        mSid = getIntent().getIntExtra(ARTICLE_SID_KEY, -1);

        initView();

        requestData();

    }

    private void requestData() {
        MainApplication.requestQueue.add(new GsonRequest<NewsContent>(APIUtils.getNewsContentUrl(mSid),
                NewsContent.class, null, new Response.Listener<NewsContent>() {
            @Override
            public void onResponse(NewsContent newsContent) {
                mNewsContent = newsContent;
                sendBroadcast(new Intent(ACTION_DATA_LOADED));
                renderContent();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(ReadActivity.this, "加载错误", Toast.LENGTH_SHORT).show();
            }
        }));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_read, menu);
        mCommentsMenuItem = menu.findItem(R.id.action_view_comments);
        sendBroadcast(new Intent(ACTION_MENU_CREATED));
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        registerReceiver(menuCreatedReciever, new IntentFilter(ACTION_MENU_CREATED));
        registerReceiver(dataLoadedReciever, new IntentFilter(ACTION_DATA_LOADED));
    }

    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(menuCreatedReciever);
        unregisterReceiver(dataLoadedReciever);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_view_in_browser: {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://www.cnbeta.com/articles/" + mNewsContent.sid + ".htm"));
                startActivity(intent);
                break;
            }
            case R.id.action_view_comments: {
                Intent intent = new Intent(this, CommentActivity.class);
                intent.putExtra(CommentActivity.ARTICLE_SID_KEY, mSid);
                startActivity(intent);
                break;
            }
            case android.R.id.home:
                onBackPressed();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        mWebView = (ObservableWebView) findViewById(R.id.webview);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mWebView.getSettings().setMediaPlaybackRequiresUserGesture(true);
        }

        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.getSettings().setPluginState(WebSettings.PluginState.ON);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebChromeClient(new WebChromeClient() {

        });

        mWebView.setScrollViewCallbacks(this);

        mToolbar = (Toolbar) findViewById(R.id.actionbar_toolbar);
        mToolbar.getBackground().setAlpha(255);
        mToolbarHeight = mToolbar.getHeight();
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (BuildConfig.DEBUG) {
                WebView.setWebContentsDebuggingEnabled(true);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebView.removeAllViews();
        mWebView.destroy();
    }

    public void renderContent() {
        if (mNewsContent != null) {
            String title = mNewsContent.title;
            String body = mNewsContent.bodytext;
            String intro = mNewsContent.hometext;
            String pubTime = mNewsContent.time;
            String html = BEFORE_TITLE + title + AFTER_TITLE_BEFORE_TIME + pubTime +
                    AFTER_TIME_BEFORE_INTRO + intro +
                    AFTER_INTRO_BEFORE_CONTENT + body + AFTER_CONTENT;
            mWebView.loadData(html, "text/html; charset=UTF-8", null);

        }
    }


    @Override
    public void onScrollChanged(int scrollY, boolean b, boolean b2) {
        boolean scrollUp = mPrevScrollY < scrollY;

        if (scrollUp) {
            if (!mIsToolbarShow) {
                showToolbar();
                mIsToolbarShow = true;
            }
        } else {
            if (mIsToolbarShow && scrollY < mToolbarHeight + 100) {
                hideToolbar();
                mIsToolbarShow = false;
            }
        }

        mPrevScrollY = scrollY;
    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {

    }

    private class MenuCreatedReciever extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            mIsMenuCreated = true;
            if (mIsLoaded) {
                replaceCount();
            }
        }
    }

    private class DataLoadedReciever extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            mIsLoaded = true;
            if (mIsMenuCreated) {
                replaceCount();
            }
        }
    }

    public void replaceCount() {
        if (mCommentsMenuItem != null) {
            mCommentsMenuItem.setTitle(getString(R.string.action_view_comments) + " (" + mNewsContent.comments + ")");
        }
    }

}
