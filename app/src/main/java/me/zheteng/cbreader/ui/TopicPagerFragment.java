/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.ui;

import java.util.ArrayList;
import java.util.List;

import com.astuetz.PagerSlidingTabStrip;

import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.DecelerateInterpolator;
import me.zheteng.cbreader.R;
import me.zheteng.cbreader.model.Topic;
import me.zheteng.cbreader.utils.PrefUtils;
import me.zheteng.cbreader.utils.UIUtils;

/**
 * 主题Pager
 */
public class TopicPagerFragment extends Fragment {

    private ViewPager mViewPager;

    private TopicPagerAdapter mAdapter;
    private PagerSlidingTabStrip mTabs;
    private MainActivity mActivity;
    private ViewGroup mTabsContainer;

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
        mActivity.setTitle(R.string.topic_title);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mActivity.getToolbar().setElevation(UIUtils.dpToPixels(mActivity, 0));
        }
        mActivity.showToolbar();
        initDatas();
    }

    private void initDatas() {


        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setPageMargin((int) UIUtils.dpToPixels(mActivity, getResources().getDimension(R.dimen.viewpager_gap)));
        boolean isNight = PrefUtils.isNightMode(mActivity);
        int d = isNight ? R.drawable.viewpager_gap_drawable_dark :
                R.drawable.viewpager_gap_drawable;
        mViewPager.setPageMarginDrawable(d);

        TypedArray ta = mActivity.obtainStyledAttributes(new int[] {
                R.attr.color_primary
        });
        int colorPrimary = ta.getColor(0, R.color.theme_primary);
        ta.recycle();
        mTabs.setBackgroundColor(colorPrimary);
        mTabs.setUnderlineHeight(0);
        mTabs.setTextColor(Color.WHITE);
        mTabs.setDividerColor(Color.TRANSPARENT);
        mTabs.setIndicatorColor(Color.WHITE);
        mTabs.setIndicatorHeight((int) UIUtils.dpToPixels(mActivity, 2));
        mTabs.setUnderlineColorResource(R.color.theme_primary);
        //        mTabs.setIndicatorColorResource(R.color.theme_primary);
        mTabs.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (!mActivity.isToolbarShow()) {
                    mActivity.showToolbar();
                    ViewPropertyAnimator animator1 = mTabsContainer.animate().translationY(0).setInterpolator(new
                            DecelerateInterpolator());
                    animator1.start();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        new AsyncTask<Void, Void, List<Topic>>() {

            @Override
            protected List<Topic> doInBackground(Void... params) {
                return PrefUtils.getTopicSubscriptions(mActivity);
            }

            @Override
            protected void onPostExecute(List<Topic> topics) {
                mAdapter = new TopicPagerAdapter(getChildFragmentManager(), topics);
                mViewPager.setAdapter(mAdapter);
                mTabs.setViewPager(mViewPager);
            }
        }.execute();

    }

    private void initViews(View view) {
        mViewPager = (ViewPager) view.findViewById(R.id.top_pager);

        mTabs = (PagerSlidingTabStrip) view.findViewById(R.id.tabs);
        mTabsContainer = (ViewGroup) view.findViewById(R.id.tabs_container);
    }

    private class TopicPagerAdapter extends FragmentPagerAdapter {
        List<Topic> mData = new ArrayList<>(1);

        public TopicPagerAdapter(FragmentManager fm, List<Topic> data) {
            super(fm);
            mData = data;
        }

        public void setData(List<Topic> mData) {
            this.mData = mData;
            notifyDataSetChanged();
            mTabs.notifyDataSetChanged();
        }

        public void swapData(List<Topic> list) {
            if (list == null || list.size() == 0) {
                return;
            }
            mData = list;
            notifyDataSetChanged();
            mTabs.notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mData == null ? 0 : mData.size();
        }

        @Override
        public Fragment getItem(int position) {
            TopicFragment fragment = TopicFragment.newInstance(mData.get(position));
            fragment.setTabs(mTabsContainer);
            return fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mData.get(position).title;
        }
    }
}
