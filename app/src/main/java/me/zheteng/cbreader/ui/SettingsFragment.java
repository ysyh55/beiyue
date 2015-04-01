/*
 * Copyright (C) 2015 junyuecao@gmail.com All Rights Reserved.
 */
package me.zheteng.cbreader.ui;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import me.zheteng.cbreader.R;

/**
 * 设置界面
 */
public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

    ListPreference lp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        lp = (ListPreference) findPreference(getString(R.string.pref_list_style_key));
        lp.setOnPreferenceChangeListener(this);
        lp.setSummary(lp.getEntry());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference instanceof ListPreference) {
            // 把 preference 这个 Preference 强制转化为 ListPreference 类型
            ListPreference listPreference = (ListPreference) preference;
            // 获取 ListPreference 中的实体内容
            CharSequence[] entries = listPreference.getEntries();
            // 获取 ListPreference 中的实体内容的下标值
            int index = listPreference.findIndexOfValue((String) newValue);
            // 把 listPreference 中的摘要显示为当前 ListPreference 的实体内容中选择的那个项目
            listPreference.setSummary(entries[index]);
        }
        return true;
    }

}