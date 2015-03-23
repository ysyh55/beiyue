/*
 * Copyright (C) 2015 junyuecao@gmail.com All Rights Reserved.
 */
package me.zheteng.cbreader.ui;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import me.zheteng.cbreader.R;

/**
 * 关于界面
 */
public class AboutActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        initViews();
    }

    private void initViews() {
        mToolbar = (Toolbar) findViewById(R.id.actionbar_toolbar);
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
