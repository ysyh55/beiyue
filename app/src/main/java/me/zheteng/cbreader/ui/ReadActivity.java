/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.ui;

import static me.zheteng.cbreader.data.CnBetaContract.FeedMessageEntry;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ObservableWebView;
import com.github.ksoichiro.android.observablescrollview.ScrollState;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import me.zheteng.cbreader.BuildConfig;
import me.zheteng.cbreader.R;
import me.zheteng.cbreader.model.FeedMessage;

/**
 * TODO 记得添加注释
 */
public class ReadActivity extends BaseActivity
        implements LoaderManager.LoaderCallbacks<Cursor>, ObservableScrollViewCallbacks {

    public static final String ACTION_MENU_CREATED = "me.zheteng.cbreader.ReadActivity.MENU_CREATED";
    public static final String ACTION_DATA_LOADED = "me.zheteng.cbreader.ReadActivity.DATA_LOADED";

    private static final int DETAIL_LOADER = 0;
    public static final String FEED_URI_KEY = "feed_uri";
    public static final String FEED_COMMENT_COUNT_KEY = "comment_count";

    public static final String[] FEED_PROJECTION = new String[] {
            FeedMessageEntry.COLUMN_TITLE,
            FeedMessageEntry.COLUMN_DESCRIPTION,
            FeedMessageEntry.COLUMN_PUB_DATE,
            FeedMessageEntry.COLUMN_LINK,
            FeedMessageEntry.COLUMN_COMMENT_COUNT,
            FeedMessageEntry.COLUMN_GUID,
    };
    static final int COL_TITLE = 0;
    static final int COL_DESCRIPTION = 1;
    static final int COL_PUB_DATE = 2;
    static final int COL_LINK = 3;
    static final int COL_COMMENT_COUNT = 4;
    static final int COL_GUID = 5;

    private Uri mUri;
    private ObservableWebView mWebView;
    private boolean loaded;
    private int mToolbarHeight;
    private int mCommentCount;

    private FeedMessage mMessage;
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
            + "        font-size: 15px;\n"
            + "        line-height: 18px;\n"
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
            + "        }"
            + "    </style>\n"
            + "</head>\n"
            + "<body>\n"
            + "<h1>";

    private static final String AFTER_TITLE_BEFORE_TIME = "</h1>\n"
            + "\n"
            + "<div id=\"time\">";

    public static final String AFTER_TIME_BEFORE_CONTENT = "</div>";

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_read);

        menuCreatedReciever = new MenuCreatedReciever();
        dataLoadedReciever = new DataLoadedReciever();

        initView();

        mUri = FeedMessageEntry.buildFeedUri(getIntent().getStringExtra(FEED_URI_KEY));
        mCommentCount = getIntent().getIntExtra(FEED_COMMENT_COUNT_KEY, 0);

        getSupportLoaderManager().initLoader(DETAIL_LOADER, null, this);
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
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(mMessage.getLink()));
                startActivity(intent);
                break;
            }
            case R.id.action_view_comments: {
                Intent intent = new Intent(this, CommentActivity.class);
                intent.putExtra(CommentActivity.FEED_URI_KEY, mMessage.getGuid());
                intent.putExtra(CommentActivity.FEED_TITLE_KEY, mMessage.getTitle());
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
        mWebView.setWebChromeClient(new WebChromeClient());

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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != mUri) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    this,
                    mUri,
                    FEED_PROJECTION,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (loaded) {
            return;
        }
        if (data.moveToFirst()) {
            String title = data.getString(COL_TITLE);
            String description = data.getString(COL_DESCRIPTION);

            //            description = " <div class=\"content\">\n"
            //                    + "\n"
            //                    + "<iframe allowfullscreen=\"allowfullscreen\" frameborder=\"0\" height=\"448\" " +
            //                    "src=\"http://player.youku.com/embed/XOTE2NDczMDg4\"
            // width=\"700\"></iframe></content>";
            long pubTime = data.getLong(COL_PUB_DATE);
            String time = fmt.print(pubTime);
            String html = BEFORE_TITLE + title + AFTER_TITLE_BEFORE_TIME + time + AFTER_TIME_BEFORE_CONTENT +
                    description + AFTER_CONTENT;

            if (mMessage == null) {
                mMessage = new FeedMessage();
            }

            mMessage.setTitle(title);
            mMessage.setDescription(description);
            mMessage.setPubDate(pubTime);
            mMessage.setReaded(true);
            mMessage.setGuid(data.getString(COL_GUID));
            mMessage.setLink(data.getString(COL_LINK));
            mMessage.setCommentCount(mCommentCount);
            mWebView.loadData(html, "text/html; charset=UTF-8", null);
            loaded = true;

            sendBroadcast(new Intent(ACTION_DATA_LOADED));

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

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
            mCommentsMenuItem.setTitle(getString(R.string.action_view_comments) + " (" + mCommentCount + ")");
        }
    }

}
