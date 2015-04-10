/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.ui;

import static me.zheteng.cbreader.data.CnBetaContract.FavoriteEntry;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Toast;
import me.zheteng.cbreader.BuildConfig;
import me.zheteng.cbreader.MainApplication;
import me.zheteng.cbreader.R;
import me.zheteng.cbreader.model.Article;
import me.zheteng.cbreader.model.NewsContent;
import me.zheteng.cbreader.ui.widget.HackyViewPager;
import me.zheteng.cbreader.utils.APIUtils;
import me.zheteng.cbreader.utils.PrefUtils;
import me.zheteng.cbreader.utils.UIUtils;
import me.zheteng.cbreader.utils.Utils;
import me.zheteng.cbreader.utils.volley.GsonRequest;

/**
 * 阅读页
 */
public class ReadActivity extends SwipeBackActionBarActivity {

    public static final String ARTICLE_ARTICLE_KEY = "article_article_key";
    public static final String ARTICLE_POSITON_KEY = "position";
    public static final String KEY_RESULT_POSITION = "result_position";
    public static final String KEY_RESULT_ARTICELS = "result_articles";
    private static final String TAG = "ReadActivity";
    public static final String TOP_COMMENT_SID_KEY = "top_comment_sid";
    public static final String FROM_TOP_COMMENT_KEY = "from_top_comment_key"; //TopComment是热门评论页
    public static final String DISABLE_LOAD_MORE_KEY = "from_top_key"; // Top是指排行榜

    private int mToolbarHeight;

    private Article mArticle;
    private HackyViewPager mViewPager;
    private ReadFragmentPagerAdapter mReadFragmentAdapter;
    private String mHtmlTemplate;
    private String mCommentsTemplate;
    private String mCommentsItemTemplate;

    /**
     * 刚进来时的第一个sid
     */
    private int mCurrentSid;
    private ReadFragment mReadFragment;
    private CommentFragment mCommentFragment;
    private MenuItem mShareMenuItem;
    private ShareActionProvider mShareActionProvider;
    private boolean mIsFromTopComment;
    private MenuItem mImageToggleMenuItem;

    private SharedPreferences mPref;
    private OnBackPressedListener mOnBackPressedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

        mArticle = getIntent().getParcelableExtra(ARTICLE_ARTICLE_KEY);
        int sid = getIntent().getIntExtra(TOP_COMMENT_SID_KEY, -1);
        mIsFromTopComment = getIntent().getBooleanExtra(FROM_TOP_COMMENT_KEY, false);
        if (mArticle == null) {
            //从热门评论过来
            mCurrentSid = sid;
            Article article = new Article();
            article.sid = sid;
            mArticle = article;
        } else {
            mCurrentSid = mArticle.sid;
        }
        initView();
        mPref = PreferenceManager.getDefaultSharedPreferences(this);

    }

    @Override
    public void onBackPressed() {
        if (mOnBackPressedListener != null) {
            if (mOnBackPressedListener.doBack()) {
                return;
            }
        }
        if (mViewPager.getCurrentItem() == 1) {
            mViewPager.setCurrentItem(0);
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_read, menu);
        mShareMenuItem = menu.findItem(R.id.action_share);
        mImageToggleMenuItem = menu.findItem(R.id.action_toggle_image);

        // Get its ShareActionProvider
        if (mShareActionProvider == null) {
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(mShareMenuItem);
        }

        // Fetch and store ShareActionProvider
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mImageToggleMenuItem.setChecked(!mPref.getBoolean(getString(R.string.pref_autoload_image_in_webview_key),
                true));
        return super.onPrepareOptionsMenu(menu);
    }

    public MenuItem getShareMenuItem() {
        return mShareMenuItem;
    }

    public void setShareIntent(NewsContent newsContent) {
        Intent mShareIntent = new Intent();
        mShareIntent.setAction(Intent.ACTION_SEND);
        mShareIntent.setType("text/plain");
        mShareIntent.putExtra(Intent.EXTRA_TEXT, newsContent.title + " http://www.cnbeta.com/articles/" +
                newsContent.sid + ".htm");

        mShareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(Intent.createChooser(mShareIntent, getString(R.string.menu_share)));
        //        mShareActionProvider.setShareIntent(mShareIntent);
    }


    private void initView() {
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

        mViewPager = (HackyViewPager) findViewById(R.id.article_pager);

        mReadFragmentAdapter =
                new ReadFragmentPagerAdapter(
                        getSupportFragmentManager());
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setPageMargin((int) UIUtils.dpToPixels(this, getResources().getDimension(R.dimen.viewpager_gap)));
        boolean isNight = PrefUtils.isNightMode(this);
        int d = isNight ? R.drawable.viewpager_gap_drawable_dark :
                R.drawable.viewpager_gap_drawable;
        mViewPager.setPageMarginDrawable(d);
        mViewPager.setAdapter(mReadFragmentAdapter);
        mViewPager.setCurrentItem(0);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //                Log.d(TAG, "拉动Pager" + positionOffsetPixels);

            }

            @Override
            public void onPageSelected(int position) {
                showToolbar();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        if (mIsFromTopComment) {
            mViewPager.setEnabled(false);
        }
    }

    public ReadFragment getReadFragment() {
        if (mReadFragment == null) {
            mReadFragment = (ReadFragment) mReadFragmentAdapter.getItem(0);
            return mReadFragment;
        } else {
            return mReadFragment;
        }
    }

    public CommentFragment getCommentFragment() {
        if (mCommentFragment == null) {
            mCommentFragment = (CommentFragment) mReadFragmentAdapter.getItem(1);
            return mCommentFragment;
        } else {
            return mCommentFragment;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_view_in_browser: {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://www.cnbeta.com/articles/" + mCurrentSid + ".htm"));
                startActivity(intent);
                return true;
            }
            case R.id.action_share:
                if (getReadFragment().getNewsContent() == null) {
                    Toast.makeText(this, R.string.share_fail, Toast.LENGTH_SHORT).show();
                    return true;
                }
                setShareIntent(getReadFragment().getNewsContent());
                return true;
            case R.id.action_favorite:
                if (mIsFromTopComment) {
                    getArticleInfoAndAddFavorite();
                } else {
                    addFavorite(this, getReadFragment().getNewsContent(), mArticle);
                }
                return true;
            case R.id.action_view_comments: {
                mViewPager.setCurrentItem(1);
                return true;
            }
            case R.id.action_font_big:
                PrefUtils.setFontSize(this, PrefUtils.getFontSize(this) + 1);
                break;
            case R.id.action_font_small:
                PrefUtils.setFontSize(this, PrefUtils.getFontSize(this) - 1);
                break;
            case R.id.action_toggle_image:
                mPref.edit().putBoolean(getString(R.string.pref_autoload_image_in_webview_key),
                        mImageToggleMenuItem.isChecked()).apply();
                break;
            case android.R.id.home:
                onBackPressed();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    private void getArticleInfoAndAddFavorite() {
        MainApplication.requestQueue.add(new GsonRequest<NewsContent>(APIUtils.getNewsContentUrl(mCurrentSid),
                NewsContent.class, null, new Response.Listener<NewsContent>() {
            @Override
            public void onResponse(NewsContent newsContent) {
                mArticle.readFromNewsContent(newsContent);
                addFavorite(ReadActivity.this, newsContent, mArticle);

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(ReadActivity.this, "拉取信息错误,请检查网络", Toast.LENGTH_SHORT).show();
            }
        }));
        mArticle.readFromNewsContent(getReadFragment().getNewsContent());

    }

    private void addFavorite(ReadActivity context, NewsContent newsContent, Article article) {
        Gson gson = new Gson();
        ContentValues cv = new ContentValues();
        cv.put(FavoriteEntry.COLUMN_SID, mCurrentSid);
        cv.put(FavoriteEntry.COLUMN_ARTICLE, gson.toJson(article));
        cv.put(FavoriteEntry.COLUMN_NEWSCONTENT, gson.toJson(newsContent));
        cv.put(FavoriteEntry.COLUMN_CREATE_TIME, System.currentTimeMillis());
        Uri uri = context.getContentResolver().insert(FavoriteEntry.CONTENT_URI, cv);
        if (uri != null) {
            Toast.makeText(this, R.string.favorite_success, Toast.LENGTH_SHORT).show();
        }
    }

    public HackyViewPager getFragmentViewPager() {
        return mViewPager;
    }

    /**
     * 获取index.html的内容,用于渲染
     *
     * @return index.html
     */
    public String getHtmlTemplate() {
        if (mHtmlTemplate == null) {
            mHtmlTemplate = Utils.loadAssetTextAsString(this, "index.html");
        }
        return mHtmlTemplate;
    }



    @Override
    public void finish() {
        super.finish();
    }

    public OnBackPressedListener getmOnBackPressedListener() {
        return mOnBackPressedListener;
    }

    public void setOnBackPressedListener(OnBackPressedListener mOnBackPressedListener) {
        this.mOnBackPressedListener = mOnBackPressedListener;
    }

    public String getCommentsTemplate() {
        if (mCommentsTemplate == null) {
            mCommentsTemplate = Utils.loadAssetTextAsString(this, "comments.html");
        }
        return mCommentsTemplate;
    }

    public String getCommentsItemTemplate() {
        if (mCommentsItemTemplate == null) {
            mCommentsItemTemplate = Utils.loadAssetTextAsString(this, "commentsitem.html");
        }
        return mCommentsItemTemplate;
    }

    public class ReadFragmentPagerAdapter extends FragmentPagerAdapter {

        public ReadFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment;
            if (position == 0) {
                fragment = ReadFragment.newInstance(mArticle);
                mReadFragment = (ReadFragment)fragment;
            } else {
                fragment = CommentFragment.newInstance(mArticle);
                mCommentFragment = (CommentFragment)fragment;
            }
            return fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return position == 0 ? "查看文章" : "评论";
        }

    }

    public interface OnBackPressedListener {
        boolean doBack();
    }


}
