/*
 * Copyright (C) 2015 junyuecao@gmail.com All Rights Reserved.
 */
package me.zheteng.cbreader.ui;

import java.util.List;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.nineoldandroids.view.ViewHelper;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import me.zheteng.cbreader.R;
import me.zheteng.cbreader.model.Article;
import me.zheteng.cbreader.ui.widget.QuickReturnRecyclerView;
import me.zheteng.cbreader.utils.APIUtils;
import me.zheteng.cbreader.utils.UIUtils;

/**
 * TODO 记得添加注释
 */
public class NewsListFragment extends BaseListFragment implements ObservableScrollViewCallbacks,
        SharedPreferences.OnSharedPreferenceChangeListener {

    public static final int CURRENT_STATE_REQUEST = 1;

    public static final String TOOLBAR_HEIGHT_KEY = "toolbar_height";
    private QuickReturnRecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;

    private boolean mIsHeadbarShow = true;

    protected View mHeader;
    protected int mFlexibleSpaceImageHeight;
    protected View mHeaderBar;
    protected View mListBackgroundView;
    protected int mIntersectionHeight;

    private View mImageHolder; //placeHolderVIew
    private View mHeaderBackground;
    private int mPrevScrollY;
    private int mToolbarAlpha;
    private boolean mGapIsChanging;
    private boolean mGapHidden;
    private boolean mReady;


    private boolean mSelected;
    private TextView mNewTitle;

    private Toolbar mToolbar; // sticky

    private int mToolbarHeight;
    private int mStartY;
    private boolean mIsInTranslate2; // 是第一类变换还是第二类
    private SharedPreferences mPref;

    public static NewsListFragment newInstance(int toolbarHeight) {
        NewsListFragment fragment = new NewsListFragment();

        Bundle bundle = new Bundle();
        bundle.putInt(TOOLBAR_HEIGHT_KEY, toolbarHeight);

        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        mToolbarHeight = bundle.getInt(TOOLBAR_HEIGHT_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_newslist, container, false);

        initViews(view);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        mToolbar = mActivity.getToolbar();
        mToolbar.getBackground().setAlpha(0);
        mToolbarHeight = mToolbar.getHeight();
        mActivity.setTitle(null);
        mPref = PreferenceManager.getDefaultSharedPreferences(mActivity);

        setupRecyclerView();

        if (!loadCachedData()) {
            refreshData(APIUtils.getArticleListsUrl());
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        mPref.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPref.registerOnSharedPreferenceChangeListener(this);
        mAdapter.setIsShowThumb(mPref.getBoolean(getString(R.string.pref_autoload_image_in_list_key), true));
        mAdapter.notifyDataSetChanged();
    }

    private void initViews(View view) {

        mRecyclerView = (QuickReturnRecyclerView) view.findViewById(R.id.feed_list);

        mFlexibleSpaceImageHeight = getResources().getDimensionPixelSize(R.dimen.flexible_space_image_height);

        // Even when the top gap has began to change, header bar still can move
        // within mIntersectionHeight.
        mIntersectionHeight = getResources().getDimensionPixelSize(R.dimen.intersection_height);

        mImageHolder = view.findViewById(R.id.image_holder);
        mHeader = view.findViewById(R.id.header);
        mHeaderBar = view.findViewById(R.id.header_bar);
        mHeaderBackground = view.findViewById(R.id.header_background);
        mListBackgroundView = view.findViewById(R.id.list_background);
        mNewTitle = ((TextView) view.findViewById(R.id.title));
        mNewTitle.setText(R.string.app_name);

        ScrollUtils.addOnGlobalLayoutListener(mRecyclerView, new Runnable() {
            @Override
            public void run() {
                // mListBackgroundView makes ListView's background except header view.
                if (mListBackgroundView != null) {
                    final View contentView = mActivity.getWindow().getDecorView().findViewById
                            (android.R.id.content);
                    // mListBackgroundView's should fill its parent vertically
                    // but the height of the content view is 0 on 'onCreate'.
                    mListBackgroundView.getLayoutParams().height = contentView.getHeight();
                }

                mReady = true;
                updateViews(mRecyclerView.getCurrentScrollY(), false);
            }
        });

        trySetupSwipeRefresh(view);

    }

    public Toolbar getToolbar() {
        if (mActivity == null) {
            return null;
        }
        mToolbar = mActivity.getToolbar();
        return mToolbar;
    }

    private void updateViews(int scrollY, boolean animated) {
        // If it's ListView, onScrollChanged is called before ListView is laid out (onGlobalLayout).
        // This causes weird animation when onRestoreInstanceState occurred,
        // so we check if it's laid out already.
        if (!mReady) {
            return;
        }

        boolean scrollUp = mPrevScrollY < scrollY;
        // Translate image
        ViewHelper.setTranslationY(mImageHolder, -scrollY / 2);

        // Translate header
        if (ViewHelper.getTranslationY(mHeaderBar) > 0 || scrollY < mFlexibleSpaceImageHeight - mHeaderBar
                .getHeight()) {
            mHeaderBar.animate().cancel();
            mNewTitle.animate().cancel();
            ViewHelper.setTranslationY(mHeaderBar, getHeaderTranslationY(scrollY));
            ViewHelper.setTranslationX(mNewTitle, getTitleTranslationX(scrollY));
            mIsInTranslate2 = false;
        } else {
            if (!mIsInTranslate2 ) {
                mIsInTranslate2 = true;
            }
            if (scrollUp) {
                if (mActivity.isToolbarShow()) {
                    mActivity.hideToolbar();
                    hideHeadeBbar();
                }
            } else {
                if (!mActivity.isToolbarShow()) {
                    mActivity.showToolbar();
                    showHeaderBar();
                }
            }
        }

        // Show/hide gap
        final int headerHeight = mHeaderBar.getHeight();

        mPrevScrollY = scrollY;
        // Translate list background
        ViewHelper.setTranslationY(mListBackgroundView, ViewHelper.getTranslationY(mHeader));

        if (scrollY > mFlexibleSpaceImageHeight - mToolbarHeight) {
            mToolbarAlpha = 255;
        } else {
            mToolbarAlpha = (int) (((float) scrollY) / ((float) mFlexibleSpaceImageHeight - (float) mToolbarHeight) *
                                           255);
        }
        mHeaderBackground.getBackground().setAlpha(mToolbarAlpha);

    }

    protected void setupRecyclerView() {
        mRecyclerView.setScrollViewCallbacks(this);
        mLayoutManager = new LinearLayoutManager(mActivity);

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
                        String url = APIUtils
                                .getArticleListUrl(0, mAdapter.getData().get(mAdapter.getData().size() - 1).sid);

                        loadMoreArticles(url);
                    }

                    loading = true;
                }
            }
        });
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(false);


        mAdapter = new ArticleListAdapter(mActivity,
                null,
                true,
                mRecyclerView,
                mFlexibleSpaceImageHeight,
                mPref.getBoolean(getString(R.string.pref_autoload_image_in_list_key), true));
        mRecyclerView.setAdapter(mAdapter);
    }

    private void trySetupSwipeRefresh(View root) {
        mSwipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipe_refresh_layout);
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setColorSchemeResources(
                    R.color.refresh_progress_1,
                    R.color.refresh_progress_2,
                    R.color.refresh_progress_3);
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    refreshData(APIUtils.getArticleListsUrl());
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
        int top = mToolbarHeight;
        mSwipeRefreshLayout.setProgressViewOffset(false,
                top + progressBarStartMargin, top + progressBarEndMargin);

        mSwipeRefreshLayout.setProgressViewEndTarget(false, top + progressBarEndMargin + top);
    }

    @Override
    public void onScrollChanged(int scrollY, boolean b, boolean b2) {
        updateViews(scrollY, true);
    }

    @Override
    public void onDownMotionEvent() {
        mStartY = mRecyclerView.getCurrentScrollY();
    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {

    }
    protected  void showHeaderBar() {
        if (mHeaderBar == null) {
            return;
        }
        mHeaderBar.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();
        mHeaderBackground.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();
        mIsHeadbarShow = true;
    }

    protected void hideHeadeBbar() {
        if (mHeaderBar == null) {
            return;
        }
        mHeaderBar.animate().translationY(-mToolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
        mHeaderBackground.animate().translationY(-mToolbar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
        mIsHeadbarShow = false;
    }

    public boolean isToolbarShow() {
        return mIsHeadbarShow;
    }
    protected float getHeaderTranslationY(int scrollY) {
        return ScrollUtils
                .getFloat(-scrollY + mFlexibleSpaceImageHeight - mHeaderBar.getHeight(), 0,
                        Float.MAX_VALUE);
    }

    protected float getTitleTranslationX(int scrollY) {
        return ScrollUtils
                .getFloat(UIUtils.dpToPixels(mActivity, 32f) / ((float) mFlexibleSpaceImageHeight - (float) mHeaderBar
                                .getHeight()) *
                                (float) scrollY,
                        0,
                        UIUtils.dpToPixels(mActivity, 32f));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CURRENT_STATE_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                List<Article> list = data.getParcelableArrayListExtra(ReadActivity.KEY_RESULT_ARTICELS);
                int position = data.getIntExtra(ReadActivity.KEY_RESULT_POSITION, 0);
                mAdapter.swapData(list);
            }
        }
    }



    @Override
    public void onStart() {
        super.onStart();
        mHeaderBar.getBackground().setAlpha(mToolbarAlpha);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_autoload_image_in_list_key))) {
            boolean showThumb = sharedPreferences.getBoolean(key, true);
            mAdapter.setIsShowThumb(showThumb);
        }
    }
}
