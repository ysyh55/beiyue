/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Toast;
import me.zheteng.cbreader.BuildConfig;
import me.zheteng.cbreader.MainApplication;
import me.zheteng.cbreader.R;
import me.zheteng.cbreader.model.Article;
import me.zheteng.cbreader.model.NewsContent;
import me.zheteng.cbreader.utils.APIUtils;
import me.zheteng.cbreader.utils.UIUtils;
import me.zheteng.cbreader.utils.Utils;
import me.zheteng.cbreader.utils.volley.GsonRequest;

/**
 * TODO 记得添加注释
 */
public class ReadActivity extends BaseActivity {

    public static final String ARTICLE_ARTICLES_KEY = "sid";
    public static final String ARTICLE_POSITON_KEY = "position";
    public static final String KEY_RESULT_POSITION = "result_position";
    public static final String KEY_RESULT_ARTICELS = "result_articles";
    private static final String TAG = "ReadActivity";
    public static final String TOP_COMMENT_SID_KEY = "top_comment_sid";

    private int mToolbarHeight;

    private ArrayList<Article> mArticles;
    private int mPosition;
    private ViewPager mViewPager;
    private ReadFragmentPagerAdapter mReadFragmentAdapter;
    private String mHtmlTemplate;
    private boolean mIsLoadingMoreData;
    private MenuItem mCommentMenuItem;

    private Intent resultIntent = new Intent();
    /**
     * 刚进来时的第一个sid
     */
    private int mCurrentSid;
    private ReadFragment mCurrentFragment;
    private int mCurrentPosition;
    private MenuItem mShareMenuItem;
    private ShareActionProvider mShareActionProvider;
    private boolean mIsFromTopComment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_read);
        mArticles = getIntent().getParcelableArrayListExtra(ARTICLE_ARTICLES_KEY);
        mPosition = getIntent().getIntExtra(ARTICLE_POSITON_KEY, 0);
        mCurrentPosition = mPosition;
        int sid = getIntent().getIntExtra(TOP_COMMENT_SID_KEY, -1);
        mIsFromTopComment = sid != -1;
        if (mArticles == null) {
            //从热门评论过来
            mCurrentSid = sid;
            mArticles = new ArrayList<Article>();
            Article article = new Article();
            article.sid = sid;
            mArticles.add(article);
        } else {
            mCurrentSid = mArticles.get(mPosition).sid;
        }
        initView();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_read, menu);
        mCommentMenuItem = menu.findItem(R.id.action_view_comments);
        mShareMenuItem = menu.findItem(R.id.action_share);

        // Get its ShareActionProvider
        if (mShareActionProvider == null) {
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(mShareMenuItem);
        }
        // Fetch and store ShareActionProvider
        return true;
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

        mShareActionProvider.setShareIntent(mShareIntent);
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

        mViewPager = (ViewPager) findViewById(R.id.article_pager);

        mReadFragmentAdapter =
                new ReadFragmentPagerAdapter(
                        getSupportFragmentManager());
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setPageMargin((int) UIUtils.dpToPixels(this, getResources().getDimension(R.dimen.viewpager_gap)));
        mViewPager.setPageMarginDrawable(R.drawable.viewpager_gap_drawable);
        mViewPager.setAdapter(mReadFragmentAdapter);
        mViewPager.setCurrentItem(mPosition);
        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                showToolbar();
                mCurrentPosition = position;
                mCurrentSid = mArticles.get(position).sid;
                mCurrentFragment = mReadFragmentAdapter.getRegisteredFragment(position);
                if (position == mReadFragmentAdapter.getCount() - 1 && !mIsLoadingMoreData) {
                    loadMoreData();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        if (mIsFromTopComment) {
            mViewPager.setEnabled(false);
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
            case R.id.action_view_comments: {
                if (mReadFragmentAdapter.getRegisteredFragment(mCurrentPosition).getNewsContent() == null) {
                    return true;
                }
                Intent intent = new Intent(this, CommentActivity.class);
                intent.putExtra(CommentActivity.ARTICLE_SID_KEY, mCurrentSid);
                intent.putExtra(CommentActivity.ARTICLE_COUNTD_KEY,
                        mReadFragmentAdapter.getRegisteredFragment(mCurrentPosition)
                                .getNewsContent()
                                .comments);
                startActivity(intent);
                return true;
            }
            case android.R.id.home:
                onBackPressed();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 获取index.html的内容,用于渲染
     *
     * @return index.html
     */
    public String getHtmlTemplate() {
        if (mHtmlTemplate == null) {
            mHtmlTemplate = loadAssetTextAsString(this, "index.html");
        }
        return mHtmlTemplate;
    }

    private String loadAssetTextAsString(Context context, String name) {
        BufferedReader in = null;
        try {
            StringBuilder buf = new StringBuilder();
            InputStream is = context.getAssets().open(name);
            in = new BufferedReader(new InputStreamReader(is));

            String str;
            boolean isFirst = true;
            while ( (str = in.readLine()) != null ) {
                if (isFirst)
                    isFirst = false;
                else
                    buf.append('\n');
                buf.append(str);
            }
            return buf.toString();
        } catch (IOException e) {
            Log.e(TAG, "Error opening asset " + name);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing asset " + name);
                }
            }
        }

        return null;
    }

    @Override
    public void finish() {
        resultIntent.putParcelableArrayListExtra(KEY_RESULT_ARTICELS, mArticles);
        resultIntent.putExtra(KEY_RESULT_POSITION, mPosition);

        setResult(RESULT_OK, resultIntent);
        super.finish();
    }


    private void loadMoreData() {
        mIsLoadingMoreData = true;
        String url = APIUtils.getArticleListUrl(0, mCurrentSid);

        MainApplication.requestQueue.add(new GsonRequest<Article[]>(url, Article[].class, null,
                new Response.Listener<Article[]>() {
                    @Override
                    public void onResponse(Article[] s) {
                        List<Article> articles = Utils.getListFromArray(s);
                        appendData(articles);
                        mIsLoadingMoreData = false;
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                mIsLoadingMoreData = false;
                Toast.makeText(ReadActivity.this, "加载错误", Toast.LENGTH_SHORT).show();
            }
        }));
    }

    public void appendData(List<Article> articles) {
        mArticles.addAll(articles);
        mReadFragmentAdapter.notifyDataSetChanged();
    }

    public MenuItem getCommentMenuItem() {
        return mCommentMenuItem;
    }

    public class ReadFragmentPagerAdapter extends FragmentStatePagerAdapter {
        SparseArray<ReadFragment> registeredFragments = new SparseArray<ReadFragment>();

        public ReadFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public ReadFragment getItem(int i) {
            return ReadFragment.newInstance(mArticles.get(i));
        }

        @Override
        public ReadFragment instantiateItem(ViewGroup container, int position) {
            ReadFragment fragment = (ReadFragment) super.instantiateItem(container, position);
            registeredFragments.put(position, fragment);
            return fragment;
        }

        @Override
        public int getCount() {
            return mArticles.size();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        public ReadFragment getRegisteredFragment(int position) {
            return registeredFragments.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "查看文章" + mArticles.get(position);
        }

    }
}
