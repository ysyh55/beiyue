/*
 * Copyright (C) 2015 junyuecao@gmail.com All Rights Reserved.
 */
package me.zheteng.cbreader.ui;

import com.astuetz.PagerSlidingTabStrip;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import me.zheteng.cbreader.R;
import me.zheteng.cbreader.utils.UIUtils;

/**
 * TODO 记得添加注释
 */
public class TopPagerFragment extends Fragment {
    public static final int COUNTER = 0;
    public static final int COMMENTS = 1;
    public static final int DIG = 2;
    public static final int TOP10 = 3;
    public static int[] TOP_TYPES = {
            COUNTER,
            COMMENTS,
            DIG,
            TOP10
    };
    //    protected static final int NAVDRAWER_ITEM_TOP_DIG = 6;
    //    protected static final int NAVDRAWER_ITEM_TOP_COMMENTS = 2;
    //    protected static final int NAVDRAWER_ITEM_TOP_COUNTER = 3;
    //    protected static final int NAVDRAWER_ITEM_TOP10 = 4;

    //http://api.cnbeta.com/capi?app_key=10000&format=json&method=Article.TodayRank&timestamp=1427129230&type=counter
    //http://api.cnbeta.com/capi?app_key=10000&format=json&method=Article.TodayRank&timestamp=1427129249&type=comments
    //http://api.cnbeta.com/capi?app_key=10000&format=json&method=Article.TodayRank&timestamp=1427129266&type=dig
    //http://api.cnbeta.com/capi?app_key=10000&format=json&method=Article.Top10&timestamp=1427129318

    private ViewPager mViewPager;

    private TopPagerAdapter mAdapter;
    private PagerSlidingTabStrip mTabs;
    private MainActivity mActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_top_pager, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = ((MainActivity) getActivity());
        mActivity.setTitle(R.string.top_title);
        mActivity.showToolbar();
        initDatas();
    }

    private void initDatas() {
        mAdapter = new TopPagerAdapter(getChildFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setPageMargin((int) UIUtils.dpToPixels(mActivity, getResources().getDimension(R.dimen.viewpager_gap)));
        mViewPager.setPageMarginDrawable(R.drawable.viewpager_gap_drawable);


        mTabs.setViewPager(mViewPager);
        mTabs.setUnderlineColorResource(R.color.theme_primary);
        mTabs.setIndicatorColorResource(R.color.theme_primary);

    }

    private void initViews(View view) {
        mViewPager = (ViewPager) view.findViewById(R.id.top_pager);

        mTabs = (PagerSlidingTabStrip) view.findViewById(R.id.tabs);

    }

    private class TopPagerAdapter extends FragmentPagerAdapter {

        public final int[] TITLES = {
                R.string.top_counter,
                R.string.top_comments,
                R.string.top_dig,
                R.string.top_top10,
        };

        public TopPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return TOP_TYPES.length;
        }

        @Override
        public Fragment getItem(int position) {
            return TopFragment.newInstance(TOP_TYPES[position]);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getActivity().getString(TITLES[position]);
        }
    }

}
