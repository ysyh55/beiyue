/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.ui;

import static android.widget.AdapterView.AdapterContextMenuInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.ksoichiro.android.observablescrollview.ObservableWebView;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import android.animation.IntEvaluator;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import me.zheteng.cbreader.BuildConfig;
import me.zheteng.cbreader.MainApplication;
import me.zheteng.cbreader.R;
import me.zheteng.cbreader.model.Article;
import me.zheteng.cbreader.model.NewsComment;
import me.zheteng.cbreader.model.NewsContent;
import me.zheteng.cbreader.ui.widget.HackyViewPager;
import me.zheteng.cbreader.ui.widget.MaterialProgressBar;
import me.zheteng.cbreader.utils.APIUtils;
import me.zheteng.cbreader.utils.PrefUtils;
import me.zheteng.cbreader.utils.Utils;
import me.zheteng.cbreader.utils.volley.GsonRequest;
import me.zheteng.cbreader.utils.volley.StringRequest;
import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * 阅读详情页
 */
public class ReadFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String ACTION_DATA_LOADED = "me.zheteng.cbreader.ReadActivity.DATA_LOADED";

    public static final String ARTICLE_SID_KEY = "sid";
    public static final String ARTICLE_COMMENTS_KEY = "comments";
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
    private TextView mNoDatHint;
    private Intent mShareIntent;
    private SharedPreferences mPref;
    private List<String> mAllImgUrls = new ArrayList<>();
    private HackyViewPager mViewPager;
    private PhotoViewPagerAdapter mAdapter;
    private TextView mPhotoCounter;
    private ImageButton mDownloadPhoto;
    private ViewGroup mPhotoViewContainer;
    private boolean mIsPhotoViewShow;
    private String mCommentsHtml;
    private int mCommentsCount;

    public static ReadFragment newInstance(Article article) {
        ReadFragment fragment = new ReadFragment();

        Bundle bundle = new Bundle();
        bundle.putInt(ARTICLE_SID_KEY, article.sid);
        bundle.putInt(ARTICLE_COMMENTS_KEY, article.comments);

        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mSid = args.getInt(ARTICLE_SID_KEY, 0);
        mCommentsCount = args.getInt(ARTICLE_COMMENTS_KEY, 0);

        setHasOptionsMenu(true);

        dataLoadedReciever = new DataLoadedReciever();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_read, container, false);
        initView(view);
        return view;
    }

    /**
     * 切换到当前页的时候,要嘛加载页面,要嘛替换TOOLBAR的评论数
     *
     * @param isVisibleToUser
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser) {

        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = ((ReadActivity) getActivity());

        mToolbarHeight = getToolbar().getHeight();
        mPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
        TypedArray ta = mActivity.obtainStyledAttributes(new int[] {
                R.attr.nav_bg_color
        });
        int color = ta.getColor(0, getResources().getColor(R.color.night_nav_item_bg));
        ta.recycle();
        mWebView.setBackgroundColor(color);
        mWebView.setScrollViewCallbacks(new ShowHideToolbarListener(mActivity, mWebView));

        mAdapter = new PhotoViewPagerAdapter();
        mViewPager.setAdapter(mAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mPhotoCounter.setText("" + (position + 1) + " / " + mAllImgUrls.size());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        //        registerForContextMenu(mViewPager);
        mDownloadPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AsyncTask<Void, Void, String>() {

                    @Override
                    protected String doInBackground(Void... params) {
                        return savePhoto(mAllImgUrls.get(mViewPager.getCurrentItem()));
                    }

                    @Override
                    protected void onPostExecute(String filename) {
                        Toast.makeText(mActivity, "图片保存在" + filename, Toast.LENGTH_LONG).show();
                    }
                }.execute();

            }
        });

        if (!mHasLoaded) {
            requestData();
        } else {
            replaceCount();
            //setShareIntent();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment_read, menu);
        mCommentsMenuItem = menu.findItem(R.id.action_view_comments);
        replaceCount();
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_view_comments:

                return false;
        }
        return super.onOptionsItemSelected(item);
    }

    public void requestData() {
        if (!mPref.getBoolean(getString(R.string.pref_use_web_data_key), true)) {
            // 这部分是通过API接口获取数据的，因为API获取不到很多视频，所以改成取网页拉数据
            MainApplication.requestQueue.add(new GsonRequest<>(APIUtils.getNewsContentUrl(mSid),
                    NewsContent.class, null, new Response.Listener<NewsContent>() {
                @Override
                public void onResponse(NewsContent newsContent) {
                    mNewsContent = newsContent;
                    //                setShareIntent();
                    mActivity.getShareMenuItem().setVisible(true);
                    mActivity.sendBroadcast(new Intent(ACTION_DATA_LOADED + mSid));
                    mProgressBar.setVisibility(View.GONE);
                    requestComments(1);
                    renderContent();
                    replaceCount();
                    mHasLoaded = true;
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    if (mNoDatHint != null) {
                        mNoDatHint.setVisibility(View.VISIBLE);
                    }
                    mProgressBar.setVisibility(View.GONE);
                }
            }));
        } else {
            MainApplication.requestQueue.add(new StringRequest("http://www.cnbeta.com/articles/" + mSid + ".htm",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String html) {
                            mNewsContent = parseDocument(html);
                            mActivity.getShareMenuItem().setVisible(true);
                            mActivity.sendBroadcast(new Intent(ACTION_DATA_LOADED + mSid));
                            mProgressBar.setVisibility(View.GONE);
                            requestComments(1);
                            renderContent();
                            replaceCount();
                            mHasLoaded = true;
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    if (mNoDatHint != null) {
                        mNoDatHint.setVisibility(View.VISIBLE);
                    }
                    mProgressBar.setVisibility(View.GONE);
                }
            }));
        }

    }

    private NewsContent parseDocument(String document) {
        Document newsDocument = Jsoup.parse(document);

        NewsContent newsContent = new NewsContent();

        newsContent.title = newsDocument.select("#news_title").text();
        newsContent.source = newsDocument.select(".where a").html();
        newsContent.bodytext = newsDocument.select(".content").html();
        newsContent.hometext = newsDocument.select(".introduction p").html();
        newsContent.time = newsDocument.select(".date").text();
        newsContent.comments = mCommentsCount;
        //        newsContent.good = newsDocument.select("#good_num").text();

        return newsContent;
    }

    public void requestComments(int page) {
        MainApplication.requestQueue.add(new GsonRequest<NewsComment[]>(
                APIUtils.getCommentListWithPageUrl(mSid, page),
                NewsComment[].class, null, new Response.Listener<NewsComment[]>() {
            @Override
            public void onResponse(NewsComment[] newsComment) {
                List<NewsComment> list = Utils.getListFromArray(newsComment);
                Collections.sort(list, new Comparator<NewsComment>() {
                    @Override
                    public int compare(NewsComment lhs, NewsComment rhs) {
                        int l = Integer.parseInt(lhs.support) + Integer.parseInt(lhs.against);
                        int r = Integer.parseInt(rhs.support) + Integer.parseInt(rhs.against);
                        return r - l;
                    }
                });
                appendHotComments(list);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }));
    }

    private void appendHotComments(List<NewsComment> list) {
        if (list.size() == 0) {
            return;
        }

        String tpl = mActivity.getCommentsTemplate();
        String itemTpl = mActivity.getCommentsItemTemplate();
        StringBuilder builder = new StringBuilder();
        try {
            for (int i = 0; i < list.size(); i++) {
                NewsComment comment = list.get(i);
                String content = comment.content.replaceAll("\\$", "\\\\\\$");
                String name = comment.getUsername().replaceAll("\\$", "\\\\\\$");

                String itemHtml = itemTpl.replaceAll("\\$\\{content\\}", content)
                        .replaceAll("\\$\\{name\\}", name)
                        .replaceAll("\\$\\{support\\}", comment.support)
                        .replaceAll("\\$\\{against\\}", comment.against);
                builder.append(itemHtml);
            }

            String result = tpl.replaceAll("\\$\\{comments\\}", builder.toString())
                    .replaceAll("\\n", " ");

            mWebView.loadUrl("javascript:appendComments('" + result + "')");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setShareIntent(){
        mActivity.setShareIntent(mNewsContent);

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

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(mActivity).registerOnSharedPreferenceChangeListener(this);
        mWebView.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(mActivity).unregisterOnSharedPreferenceChangeListener(this);
        mWebView.onPause();

    }

    private void initView(View view) {
        mWebView = (ObservableWebView) view.findViewById(R.id.webview);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mWebView.getSettings().setMediaPlaybackRequiresUserGesture(true);
        }

        CookieManager.getInstance().setAcceptCookie(true);
        mWebView.getSettings().setAllowFileAccess(true);
        mWebView.getSettings().setPluginState(WebSettings.PluginState.ON);
        mWebView.getSettings().setUserAgentString(
                "Mozilla/5.0 (Linux; Android 4.2.1; en-us; Nexus 5 Build/JOP40D) AppleWebKit/535.19 (KHTML, like "
                        + "Gecko) Chrome/18.0.1025.166 Mobile Safari/535.19");
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebChromeClient(new WebChromeClient() {
        });
        mWebView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
                    view.getContext().startActivity(
                            new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                // hack 搜狐视频
                if (url.contains("http://tv.sohu.com/upload/touch/css/albumPlayerPage.min")) {
                    try {
                        WebResourceResponse resourceResponse =
                                new WebResourceResponse("text/css", "utf-8", mActivity.getAssets().open("inject.css"));
                        return resourceResponse;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return super.shouldInterceptRequest(view, url);
            }
        });
        mWebView.addJavascriptInterface(new MyJsInterface(), "cbreader");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (BuildConfig.DEBUG) {
                WebView.setWebContentsDebuggingEnabled(true);
            }
        }

        mProgressBar = ((MaterialProgressBar) view.findViewById(R.id.loading_progress));

        mNoDatHint = (TextView) view.findViewById(R.id.no_data_hint);
        mNoDatHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNoDatHint.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
                requestData();
            }
        });

        mViewPager = (HackyViewPager) view.findViewById(R.id.photo_viewpager);
        mPhotoCounter = (TextView) view.findViewById(R.id.photo_counter);
        mDownloadPhoto = (ImageButton) view.findViewById(R.id.download_photo_button);
        mPhotoViewContainer = (ViewGroup) view.findViewById(R.id.photoview_container);
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
            String pubTime = mNewsContent.time;
            String comments = String.valueOf(mNewsContent.comments);
            String darkClass = PrefUtils.isNightMode(mActivity) ? "dark" : "";

            body = body.replaceAll(VideoRegExps.TUDOU, VideoPlaceHolders.TUDOU)
                    .replaceAll(VideoRegExps.YOUKU_JS, VideoPlaceHolders.YOUKU)
                    .replaceAll(VideoRegExps.YOUKU_EMBED, VideoPlaceHolders.YOUKU)
                    .replaceAll(VideoRegExps.SOHU, VideoPlaceHolders.SOHU);

            String html = getHtmlTemplate();
            html = html.replaceAll("\\$\\{title\\}", title)
                    .replaceAll("\\$\\{time\\}", pubTime)
                    .replaceAll("\\$\\{darkClass\\}", darkClass)
                    .replaceAll("\\$\\{source\\}", source)
                    .replaceAll("\\$\\{intro\\}", intro)
                    .replaceAll("\\$\\{content\\}", body)
                            //                    .replaceAll("\\$\\{good\\}", mNewsContent.good)
                    .replaceAll("\\$\\{comments\\}", comments)
                    .replaceAll("\\$\\{font\\}", String.valueOf(PrefUtils.getFontSize(mActivity)));
            //                    .replaceAll("\\$\\{score\\}", mNewsContent.score);

            if (!Utils.isConnectedWifi(mActivity)) {
                if (!mPref.getBoolean(mActivity.getString(R.string.pref_autoload_image_in_webview_key), true)) {
                    mWebView.getSettings().setLoadsImagesAutomatically(false);
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mWebView.loadDataWithBaseURL("http://www.baidu.com/index.html", html, "text/html; charset=UTF-8",
                        "utf-8",
                        null);
            } else {
                mWebView.loadData(html, "text/html;charset=UTF-8", "utf-8");
            }
        }
    }

    private String getHtmlTemplate() {
        return mActivity.getHtmlTemplate();
    }

    public NewsContent getNewsContent() {
        return mNewsContent;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(mActivity.getString(R.string.pref_autoload_image_in_webview_key))) {
            if (!Utils.isConnectedWifi(mActivity)) {
                mWebView.getSettings().setLoadsImagesAutomatically(sharedPreferences.getBoolean(key, true));
                mWebView.reload();
            }
        } else if (key.equals(PrefUtils.KEY_FONT_SIZE)) {
            mWebView.loadUrl("javascript:setFontSize(" + sharedPreferences.getInt(key, 16) + ")");
        }
    }

    private class DataLoadedReciever extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            replaceCount();
        }
    }

    public void replaceCount() {
        if (mCommentsMenuItem != null && mNewsContent != null) {
            mCommentsMenuItem.setTitle(mActivity.getString(R.string.action_view_comments) + " (" + mNewsContent.comments + ")");
        }
    }

    private class MyJsInterface {

        @JavascriptInterface
        public void showComments() {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mActivity.getFragmentViewPager().setCurrentItem(1);
                }
            });
        }

        @JavascriptInterface
        public void setImgUrls(String json) {
            Gson gson = new Gson();
            mAllImgUrls.addAll(Arrays.asList(gson.fromJson(json, String[].class)));
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.notifyDataSetChanged();
                }
            });
        }

        @JavascriptInterface
        public void showImgSlide(final String index) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showSlide(Integer.parseInt(index));
                }
            });
        }

        @JavascriptInterface
        public void playFullScreen(String type, String data) {
            FullscreenVideoActivity.startNewInstance(mActivity, type, data);
        }
    }

    protected void showSlide(int i) {

        mViewPager.setCurrentItem(i, false);
        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(150);
        mPhotoViewContainer.startAnimation(animation);
        mPhotoViewContainer.setVisibility(View.VISIBLE);

        mPhotoCounter.setText("" + (i + 1) + " / " + mAllImgUrls.size());

        mActivity.getFragmentViewPager().toggleLock();

        if (!mActivity.isToolbarShow()) {
            mActivity.showToolbar();
        }

        Drawable bg = mToolbar.getBackground();
        ObjectAnimator animator = ObjectAnimator.ofInt(bg, "alpha", 255, 0).setDuration(150);
        animator.setEvaluator(new IntEvaluator());
        animator.start();
        //        mToolbar.getBackground().setAlpha(0);

        mIsPhotoViewShow = true;

        mActivity.setOnBackPressedListener(new ReadActivity.OnBackPressedListener() {
            @Override
            public boolean doBack() {
                if (mIsPhotoViewShow) {
                    hideSlide();
                    return true;
                }
                return false;
            }
        });
    }

    protected void hideSlide() {
        Animation animation = new AlphaAnimation(1.0f, 0.0f);
        animation.setDuration(150);
        mPhotoViewContainer.startAnimation(animation);

        mPhotoViewContainer.setVisibility(View.GONE);

        mActivity.getFragmentViewPager().toggleLock();

        mToolbar.getBackground().setAlpha(255);
        mIsPhotoViewShow = false;
        mActivity.setOnBackPressedListener(null);

    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        mActivity.getMenuInflater().inflate(R.menu.photo_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.save:
                //                savePhoto(info.id);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private String savePhoto(String url) {
        File filename;
        try {
            Bitmap bitmap = Picasso.with(mActivity).load(url).get();

            filename = new File(Utils.getSaveImageDir().getAbsolutePath() +"/" +  Utils.getFileNameFromURL(url));

            FileOutputStream out = new FileOutputStream(filename);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

//            MediaStore.Images.Media.insertImage(mActivity.getContentResolver(), filename.getAbsolutePath(),
//                    filename.getName(), filename.getName());

            return filename.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return "文件保存不成功";
    }

    class PhotoViewPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mAllImgUrls.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public View instantiateItem(ViewGroup container, int position) {
            View view = mActivity.getLayoutInflater().inflate(R.layout.photoview_item, container, false);

            final PhotoView photoView = (PhotoView) view.findViewById(R.id.photoview);
            Picasso.with(mActivity).load(mAllImgUrls.get(position)).into(photoView);
            photoView.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
                @Override
                public void onViewTap(View view, float v, float v2) {
                    hideSlide();
                }
            });
            photoView.setOnPhotoTapListener(new PhotoViewAttacher.OnPhotoTapListener() {
                @Override
                public void onPhotoTap(View view, float v, float v2) {
                    Log.d(TAG, "图片点击");

                }
            });
            photoView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Log.d(TAG, "Long clicked");
                    //                    mActivity.openContextMenu(mViewPager);
                    return true;
                }
            });

            // Now just add PhotoView to ViewPager and return it
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    public class VideoRegExps {
        public static final String TUDOU = "(?s)<embed[^>]*src=\"http://www.tudou.com/v/([a-zA-Z0-9_\\-]*)/[^>]*/?>";
        public static final String YOUKU_JS =
                "(?s)<div\\s+id=\"youkuplayer\"\\s+.*vid:\\s*'([a-zA-Z0-9_=]*)'.*</script>";
        public static final String YOUKU_EMBED =
                "(?s)<embed.*http://player.youku.com/player.php/sid/([a-zA-Z0-9_=]*)[^>]*/?>";
        public static final String SOHU = "<embed[^>]*src=\"http://share\\.vrs\\.sohu\\.com/my/v\\.swf&amp;"
                + "topBar=1&amp;id=(\\d+)[^>]*/?>";
    }

    public class VideoPlaceHolders {
        public static final String TUDOU = "<div style=\"position:relative;\"> <iframe \n"
                + "src=\"http://www.tudou.com/programs/view/html5embed"
                + ".action?code=$1&autoPlay=false&playType=AUTO\" "
                + "width=\"100%\" "
                + "height=\"524px\" "
                + "frameborder=\"0\" "
                + "scrolling=\"no\" "
                + "></iframe>"
                + "<div style=\"width: 100%;height: 100%;position: absolute;left: 0;top: 0;\" class=\"full\"  "
                + "onclick=\"playFullScreen('tudou', '$1')\"></div>"
                + "</div>";

        public static final String YOUKU = "<div style=\"position:relative;\"> "
                + "<iframe height=498 width=510 src=\"http://player.youku"
                + ".com/embed/$1\" frameborder=0 allowfullscreen></iframe> "
                + "<div style=\"width: 100%;height: 100%;position: absolute;left: 0;top: 0;\" class=\"full\"  "
                + "onclick=\"playFullScreen('youku', '$1')\"></div>"
                + "</div>";
        public static final String SOHU = "<div style=\"position:relative;\"> "
                + "<iframe src=\"http://m.tv.sohu.com/v$1.shtml\" class=\"open_video_button\" "
                + "frameborder=0 allowfullscreen></iframe>"
                + "<div style=\"width: 100%;height: 100%;position: absolute;left: 0;top: 0;\" class=\"full\" "
                + "onclick=\"playFullScreen('sohu', '$1')\"></div>"
                + "</div>";
    }
}
