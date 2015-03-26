/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.ui;

import com.astuetz.PagerSlidingTabStrip;

import android.graphics.Color;
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
public class TopicPagerFragment extends Fragment {

    private ViewPager mViewPager;

    private TopicPagerAdapter mAdapter;
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
        mAdapter = new TopicPagerAdapter(getChildFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setPageMargin((int) UIUtils.dpToPixels(mActivity, getResources().getDimension(R.dimen.viewpager_gap)));
        mViewPager.setPageMarginDrawable(R.drawable.viewpager_gap_drawable);


        mTabs.setViewPager(mViewPager);
        mTabs.setBackgroundColor(getResources().getColor(R.color.theme_primary));
        mTabs.setUnderlineHeight(0);
        mTabs.setTextColor(Color.WHITE);
        mTabs.setDividerColor(Color.TRANSPARENT);
        mTabs.setIndicatorColor(Color.WHITE);
        mTabs.setIndicatorHeight((int) UIUtils.dpToPixels(mActivity, 2));
        mTabs.setUnderlineColorResource(R.color.theme_primary);
        //        mTabs.setIndicatorColorResource(R.color.theme_primary);

    }

    private void initViews(View view) {
        mViewPager = (ViewPager) view.findViewById(R.id.top_pager);

        mTabs = (PagerSlidingTabStrip) view.findViewById(R.id.tabs);

    }

    private class TopicPagerAdapter extends FragmentPagerAdapter {

        public TopicPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public Fragment getItem(int position) {
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }
    }
}
