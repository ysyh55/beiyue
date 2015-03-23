/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.ui;

import java.util.ArrayList;
import java.util.List;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.google.gson.Gson;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.FrameLayout;
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

public class MainActivity extends BaseActivity {

    private static final int CURRENT_STATE_REQUEST = 1;
    private ObservableRecyclerView mRecyclerView;
    private ArticleListAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    protected View mHeader;
    protected int mFlexibleSpaceImageHeight;
    protected View mHeaderBar;
    protected View mListBackgroundView;
    protected int mActionBarSize;
    protected int mIntersectionHeight;

    private View mImageHolder;
    private View mHeaderBackground;
    private int mPrevScrollY;
    private int mToolbarAlpha;
    private boolean mGapIsChanging;
    private boolean mGapHidden;
    private boolean mReady;


    // ----- used for load more
    private int previousTotal = 0;
    private boolean loading = true;
    private int visibleThreshold = 5;
    int firstVisibleItem, visibleItemCount, totalItemCount;
    private boolean mLoadingData;

    // ----- end  used for load more
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        if (!loadCachedData()) {
            refreshData();
        }
    }

    private boolean loadCachedData() {
        mLoadingData = true;
        String json = PrefUtils.getCacheOfKey(this, PrefUtils.KEY_ARTICLES);

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

    private int getActionBarSize() {
        return mToolbar.getHeight();
    }

    private void initViews() {
        mToolbar = (Toolbar) findViewById(R.id.actionbar_toolbar);
        setSupportActionBar(mToolbar);

        mRecyclerView = (ObservableRecyclerView) findViewById(R.id.feed_list);

        mFlexibleSpaceImageHeight = getResources().getDimensionPixelSize(R.dimen.flexible_space_image_height);
        mActionBarSize = getActionBarSize();

        setupRecyclerView();

        // Even when the top gap has began to change, header bar still can move
        // within mIntersectionHeight.
        mIntersectionHeight = getResources().getDimensionPixelSize(R.dimen.intersection_height);

        mImageHolder = findViewById(R.id.image_holder);
        mHeader = findViewById(R.id.header);
        mHeaderBar = findViewById(R.id.header_bar);
        mHeaderBackground = findViewById(R.id.header_background);
        mListBackgroundView = findViewById(R.id.list_background);

        ((TextView) findViewById(R.id.title)).setText(getTitle());
        setTitle(null);

        ScrollUtils.addOnGlobalLayoutListener(mRecyclerView, new Runnable() {
            @Override
            public void run() {
                // mListBackgroundView makes ListView's background except header view.
                if (mListBackgroundView != null) {
                    final View contentView = getWindow().getDecorView().findViewById(android.R.id.content);
                    // mListBackgroundView's should fill its parent vertically
                    // but the height of the content view is 0 on 'onCreate'.
                    mListBackgroundView.getLayoutParams().height = contentView.getHeight();
                }

                mReady = true;
                updateViews(mRecyclerView.getCurrentScrollY(), false);
            }
        });

        trySetupSwipeRefresh();
    }

    private void updateViews(int scrollY, boolean animated) {
        // If it's ListView, onScrollChanged is called before ListView is laid out (onGlobalLayout).
        // This causes weird animation when onRestoreInstanceState occurred,
        // so we check if it's laid out already.
        if (!mReady) {
            return;
        }
        // Translate image
        ViewHelper.setTranslationY(mImageHolder, -scrollY / 2);

        // Translate header
        ViewHelper.setTranslationY(mHeader, getHeaderTranslationY(scrollY));

        // Show/hide gap
        final int headerHeight = mHeaderBar.getHeight();
        boolean scrollUp = mPrevScrollY < scrollY;
        if (scrollUp) {
            if (mFlexibleSpaceImageHeight - headerHeight - mActionBarSize <= scrollY) {
                changeHeaderBackgroundHeightAnimated(false, animated);
            }
        } else {
            if (scrollY <= mFlexibleSpaceImageHeight - headerHeight - mActionBarSize) {
                changeHeaderBackgroundHeightAnimated(true, animated);
            }
        }
        mPrevScrollY = scrollY;
        // Translate list background
        ViewHelper.setTranslationY(mListBackgroundView, ViewHelper.getTranslationY(mHeader));


        if (scrollY > mFlexibleSpaceImageHeight - mActionBarSize) {
            mToolbarAlpha = 255;
        } else {
            mToolbarAlpha = (int) ((float) scrollY / ((float) mFlexibleSpaceImageHeight - mActionBarSize) * 255);
        }
        mToolbar.getBackground().setAlpha(mToolbarAlpha);
    }

    private void changeHeaderBackgroundHeightAnimated(boolean shouldShowGap, boolean animated) {
        if (mGapIsChanging) {
            return;
        }
        final int heightOnGapShown = mHeaderBar.getHeight();
        final int heightOnGapHidden = mHeaderBar.getHeight() + mActionBarSize;
        final float from = mHeaderBackground.getLayoutParams().height;
        final float to;
        if (shouldShowGap) {
            if (!mGapHidden) {
                // Already shown
                return;
            }
            to = heightOnGapShown;
        } else {
            if (mGapHidden) {
                // Already hidden
                return;
            }
            to = heightOnGapHidden;
        }
        if (animated) {
            ViewPropertyAnimator.animate(mHeaderBackground).cancel();
            ValueAnimator a = ValueAnimator.ofFloat(from, to);
            a.setDuration(100);
            a.setInterpolator(new AccelerateDecelerateInterpolator());
            a.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float height = (float) animation.getAnimatedValue();
                    changeHeaderBackgroundHeight(height, to, heightOnGapHidden);
                }
            });
            a.start();
        } else {
            changeHeaderBackgroundHeight(to, to, heightOnGapHidden);
        }
    }

    private void changeHeaderBackgroundHeight(float height, float to, float heightOnGapHidden) {
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mHeaderBackground.getLayoutParams();
        lp.height = (int) height;
        lp.topMargin = (int) (mHeaderBar.getHeight() - height);
        mHeaderBackground.requestLayout();
        mGapIsChanging = (height != to);
        if (!mGapIsChanging) {
            mGapHidden = (height == heightOnGapHidden);
        }
    }

    protected void setupRecyclerView() {
        mRecyclerView.setScrollViewCallbacks(listScrollListener);
        mLayoutManager = new LinearLayoutManager(this);

        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

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

                    if (!mLoadingData) {
                        loadMoreArticles();
                    }

                    loading = true;
                }
            }
        });
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(false);

        View headerView = new View(this);
        headerView.setLayoutParams(
                new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, mFlexibleSpaceImageHeight));
        headerView.setMinimumHeight(mFlexibleSpaceImageHeight);
        // This is required to disable header's list selector effect
        headerView.setClickable(true);
        mAdapter = new ArticleListAdapter(this, headerView);
        mRecyclerView.setAdapter(mAdapter);
    }



    private void trySetupSwipeRefresh() {
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setColorSchemeResources(
                    R.color.refresh_progress_1,
                    R.color.refresh_progress_2,
                    R.color.refresh_progress_3);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    refreshData();
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
        int top = mToolbar.getHeight();
        mSwipeRefreshLayout.setProgressViewOffset(false,
                top + progressBarStartMargin, top + progressBarEndMargin);

        mSwipeRefreshLayout.setProgressViewEndTarget(false, top + progressBarEndMargin + top);
    }

    private ObservableScrollViewCallbacks listScrollListener = new ObservableScrollViewCallbacks() {
        @Override
        public void onScrollChanged(int scrollY, boolean b, boolean b2) {
            updateViews(scrollY, true);
        }

        @Override
        public void onDownMotionEvent() {

        }

        @Override
        public void onUpOrCancelMotionEvent(ScrollState scrollState) {

        }
    };

    protected float getHeaderTranslationY(int scrollY) {
        return ScrollUtils.getFloat(-scrollY + mFlexibleSpaceImageHeight - mHeaderBar.getHeight(), 0, Float.MAX_VALUE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CURRENT_STATE_REQUEST) {
            if (resultCode == RESULT_OK) {
                List<Article> list = data.getParcelableArrayListExtra(ReadActivity.KEY_RESULT_ARTICELS);
                int position = data.getIntExtra(ReadActivity.KEY_RESULT_POSITION, 0);
                mAdapter.swapData(list);
            }
        }
    }

    private void refreshData() {
        mLoadingData = true;
        String url = APIUtils.getArticleListsUrl();
        final GsonRequest request = new GsonRequest<Article[]>(url, Article[].class, null,
                new Response.Listener<Article[]>() {
                    @Override
                    public void onResponse(Article[] s) {
                        List<Article> articles = Utils.getListFromArray(s);
                        mAdapter.swapData(articles);
                        Gson gson = new Gson();
                        PrefUtils.saveCacheOfKey(MainActivity.this, PrefUtils.KEY_ARTICLES, gson.toJson(s));
                        mSwipeRefreshLayout.setRefreshing(false);
                        mLoadingData = false;
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                mLoadingData = false;
                Toast.makeText(MainActivity.this, "加载错误", Toast.LENGTH_SHORT).show();
            }
        });

        MainApplication.requestQueue.add(request);
    }

    private void loadMoreArticles() {
        mLoadingData = true;
        String url = APIUtils.getArticleListUrl(0, mAdapter.mData.get(mAdapter.mData.size() - 1).sid);

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
                mLoadingData = false;
                Toast.makeText(MainActivity.this, "加载错误", Toast.LENGTH_SHORT).show();
            }
        }));
    }
    @Override
    protected void onStart() {
        super.onStart();
        mToolbar.getBackground().setAlpha(mToolbarAlpha);
    }

    public class ArticleListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
            implements View.OnClickListener {
        private static final int VIEW_TYPE_HEADER = 0;
        private static final int VIEW_TYPE_ITEM = 1;
        private static final int VIEW_TYPE_PROG = 2;

        private List<Article> mData;

        private View mHeaderView;
        private Context mContext;

        public ArticleListAdapter(Context context, List<Article> data, View headerView) {
            mContext = context;
            mData = data == null ? new ArrayList<Article>() : data;
            mHeaderView = headerView;
        }

        public ArticleListAdapter(Context context, View headerView) {
            this(context, null, headerView);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case VIEW_TYPE_HEADER:
                    return new HeaderViewHolder(mHeaderView);
                case VIEW_TYPE_ITEM:
                    View view = LayoutInflater.from(mContext).inflate(R.layout.feed_list_item, parent, false);

                    ArticleItemViewHolder viewHolder = new ArticleItemViewHolder(view);
                    view.setOnClickListener(this);
                    return viewHolder;
                case VIEW_TYPE_PROG:
                    return new ProgressViewHolder(LayoutInflater.from(mContext).inflate(R.layout.progress_item,
                            parent, false));
                default:
                    throw new UnsupportedOperationException("没有这个ViewType");
            }

        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof ArticleItemViewHolder) {
                ArticleItemViewHolder holder1 = (ArticleItemViewHolder) holder;

                Article article = getItem(position);
                holder1.mTitleView.setText(article.title);
                holder1.mDescriptionView.setText(article.summary);
                holder1.mTimeView.setText(article.getReadableTime());
                holder1.mCommentCountView.setText("" + article.comments);
            }
        }

        private Article getItem(int position) {
            if (mHeaderView != null) {
                position = position - 1;
            }

            return mData.get(position);

        }

        @Override
        public int getItemViewType(int position) {
            if (mHeaderView == null) {
                return VIEW_TYPE_ITEM;
            } else {
                if (position == 0) {
                    return VIEW_TYPE_HEADER;
                } else {
                    return getItem(position) != null ? VIEW_TYPE_ITEM : VIEW_TYPE_PROG;
                }
            }
        }

        @Override
        public int getItemCount() {
            int itemCount = 0;
            if (mData == null) {
                itemCount = 0;
            } else {
                itemCount = mData.size();
            }

            if (mHeaderView == null) {
                return itemCount;
            } else {
                return itemCount + 1;
            }
        }

        private int[] getAllSid() {
            if (mData == null) {
                return new int[0];
            }

            int[] sids = new int[mData.size()];
            int i = 0;
            for (Article article : mData) {
                sids[i++] = article.sid;
            }
            return sids;
        }

        public List<Article> appendData(List<Article> articles) {
            List<Article> newList = new ArrayList<>(mData.size() + articles.size());
            newList.addAll(mData);
            newList.addAll(articles);

            mData = newList;
            this.notifyDataSetChanged();
            return mData;
        }

        public List<Article> swapData(List<Article> articles) {
            if (mData == articles) {
                return null;
            }
            List<Article> oldData = mData;
            this.mData = articles;
            if (articles != null) {
                this.notifyDataSetChanged();
            }
            return oldData;
        }

        public void addItem(Article article) {
            if (mData == null) {
                return;
            }
            mData.add(article);
            notifyDataSetChanged();
        }

        public void removeLast() {
            if (mData == null) {
                return;
            }
            mData.remove(mData.size() - 1);
            notifyDataSetChanged();
        }

        @Override
        public void onClick(View v) {
            int position = mRecyclerView.getChildPosition(v);
            if (mData != null) {
                Intent intent = new Intent(MainActivity.this, ReadActivity.class);
                intent.putParcelableArrayListExtra(ReadActivity.ARTICLE_ARTICLES_KEY,
                        (ArrayList<? extends android.os.Parcelable>) mData);
                intent.putExtra(ReadActivity.ARTICLE_POSITON_KEY, mData.indexOf(mAdapter.getItem(position)));

                startActivityForResult(intent, CURRENT_STATE_REQUEST);
            }

        }

        public class ArticleItemViewHolder extends RecyclerView.ViewHolder {
            public ArticleItemViewHolder(View itemView) {
                super(itemView);

                mTitleView = ((TextView) itemView.findViewById(R.id.title));
                mDescriptionView = ((TextView) itemView.findViewById(R.id.description));
                mTimeView = ((TextView) itemView.findViewById(R.id.time));
                mCommentCountView = ((TextView) itemView.findViewById(R.id.comment_count));
                mContainer = (ViewGroup) itemView.findViewById(R.id.container);
            }

            // each data item is just a string in this case
            public TextView mTitleView;
            public TextView mDescriptionView;
            public TextView mTimeView;
            public TextView mCommentCountView;

            public ViewGroup mContainer;
        }

        public class HeaderViewHolder extends RecyclerView.ViewHolder {

            public HeaderViewHolder(View itemView) {
                super(itemView);
            }
        }

        public class ProgressViewHolder extends RecyclerView.ViewHolder {
            public MaterialProgressBar mProgressBar;

            public ProgressViewHolder(View v) {
                super(v);
                mProgressBar = (MaterialProgressBar) v.findViewById(R.id.list_progress);
            }
        }
    }

}
