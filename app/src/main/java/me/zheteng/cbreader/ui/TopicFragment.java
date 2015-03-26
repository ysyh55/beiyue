/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.ui;

import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import me.zheteng.cbreader.R;

/**
 * 分类列表页
 */
public class TopicFragment extends Fragment {

    protected ObservableRecyclerView mRecyclerView;

    public static Fragment newInstance() {
        return new TopicFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.topic, container, false);
        findViews();
        return view;
    }

    private void findViews() {

    }
}
