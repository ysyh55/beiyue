/*
 * Copyright (C) 2015 junyuecao@gmail.com All Rights Reserved.
 */
package me.zheteng.cbreader.ui;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import me.zheteng.cbreader.R;

/**
 * 设置界面
 */
public class SettingsActivity extends BaseActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        initViews();
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(R.id.container, new SettingsFragment())
                .commit();
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
}
