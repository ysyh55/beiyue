/*
 * Copyright (C) 2015 junyuecao@gmail.com All Rights Reserved.
 */
package me.zheteng.cbreader.ui;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import me.zheteng.cbreader.R;

/**
 * 设置界面
 */
public class SettingsActivity extends SwipeBackActionBarActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private SharedPreferences mPref;
    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.activity_settings);
        initViews();
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(R.id.container, new SettingsFragment())
                .commit();

        if (mPref.getBoolean(getString(R.string.pref_show_ad_in_settings_key), true)) {
            loadGoogleAd();
        }
    }

    private void loadGoogleAd() {
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mAdView.setVisibility(View.VISIBLE);
            }
        });
        mAdView.loadAd(adRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    private void initViews() {
        mToolbar = ((Toolbar) findViewById(R.id.actionbar_toolbar));
        mToolbar.getBackground().setAlpha(255);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_show_ad_in_settings_key))) {
            if (sharedPreferences.getBoolean(key, true)) {
                if (mAdView == null) {
                    loadGoogleAd();
                } else {
                    mAdView.setVisibility(View.VISIBLE);
                }
            } else {
                mAdView.setVisibility(View.GONE);
            }
        }
    }
}
