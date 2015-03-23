/*
 * Copyright (C) 2015 junyuecao@gmail.com All Rights Reserved.
 */
package me.zheteng.cbreader.ui;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import me.zheteng.cbreader.R;

/**
 * 设置界面
 */
public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);
    }
}
