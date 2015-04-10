/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.ui;

import java.util.ArrayList;
import java.util.Random;

import com.umeng.update.UmengUpdateAgent;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import me.zheteng.cbreader.MainApplication;
import me.zheteng.cbreader.R;
import me.zheteng.cbreader.ui.widget.ScrimInsetsScrollView;
import me.zheteng.cbreader.utils.PrefUtils;
import me.zheteng.cbreader.utils.UIUtils;
import me.zheteng.cbreader.utils.Utils;

public class MainActivity extends BaseActivity implements Palette.PaletteAsyncListener {
    public static final String TAG_NEWS_ARTICES = "news_artices";
    public static final String TAG_RECOMMEND_COMMENT = "recommend_comment";
    public static final String TAG_FAVORITE = "favorite";
    public static final String TAG_TOP = "top";
    public static final String TAG_TOPIC = "topic";

    protected static final int NAVDRAWER_ITEM_NEWS_ARTICES = 0;
    protected static final int NAVDRAWER_ITEM_RECOMMEND_COMMENT = 1;
    protected static final int NAVDRAWER_ITEM_TOP = 2;
    protected static final int NAVDRAWER_ITEM_TOPIC = 3;
    protected static final int NAVDRAWER_ITEM_NIGHT = 4;
    protected static final int NAVDRAWER_ITEM_SETTINGS = 5;
    protected static final int NAVDRAWER_ITEM_ABOUT = 6;
    protected static final int NAVDRAWER_ITEM_FAVORITE = 7;
    protected static final int NAVDRAWER_ITEM_INVALID = -1;
    protected static final int NAVDRAWER_ITEM_SEPARATOR = -2;
    protected static final int NAVDRAWER_ITEM_SEPARATOR_SPECIAL = -3;

    // titles for navdrawer items (indices must correspond to the above)
    private static final int[] NAVDRAWER_TITLE_RES_ID = new int[] {
            R.string.navdrawer_item_news_artices,
            R.string.navdrawer_item_recommend_comment,
            R.string.navdrawer_item_top,
            R.string.navdrawer_item_topic,
            R.string.navdrawer_item_night,
            R.string.navdrawer_item_settings,
            R.string.navdrawer_item_about,
            R.string.favorite
    };
    private static final int[] NAVDRAWER_ICON_RES_ID = new int[] {
            R.drawable.ic_comment_grey600_24dp,  // My Schedule
            R.drawable.ic_whatshot_grey600_24dp,  // Explore
            R.drawable.ic_equalizer_grey600_24dp, // Map
            R.drawable.ic_comment_grey600_24dp, // Social
            R.drawable.ic_brightness_3_grey600_24dp, // Night
            R.drawable.ic_settings_grey600_24dp, // Video Library
            R.drawable.ic_person_grey600_24dp, // Video Library
            R.drawable.ic_person_grey600_24dp, // Video Library
    };

    private static final int CURRENT_STATE_REQUEST = 1;
    private static final long NAVDRAWER_LAUNCH_DELAY = 250;
    private static final long MAIN_CONTENT_FADEOUT_DURATION = 150;
    private static final long MAIN_CONTENT_FADEIN_DURATION = 150;

    protected int mActionBarSize;
    protected int mIntersectionHeight;

    private DrawerLayout mDrawerLayout;
    private ArrayList<Integer> mNavDrawerItems = new ArrayList<Integer>();

    private boolean mLoadingData;
    private ViewGroup mDrawerItemsListContainer;
    private View[] mNavDrawerItemViews;
    private boolean mSelected;
    private boolean mDoubleBackToExitPressedOnce;
    private int mDrawerHeaderBg;
    private Handler mHandler;
    private View mNightDrawerItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Random random = new Random();
        mDrawerHeaderBg = MainApplication.DRAWER_HEADER_BACKGROUND[random.nextInt(MainApplication
                .DRAWER_HEADER_BACKGROUND.length)];

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(android.R.color.transparent));
        }
//
//        Drawable drawable = getResources().getDrawable(mDrawerHeaderBg);
//        if (drawable instanceof BitmapDrawable) {
//            Palette.generateAsync(((BitmapDrawable) drawable).getBitmap(), this);
//        }

        initViews();
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                NewsListFragment.newInstance(mActionBarSize),
                TAG_NEWS_ARTICES).commit();
        mHandler = new Handler();

        UmengUpdateAgent.update(this);
    }

    private int getActionBarSize() {
        return mToolbar.getHeight();
    }

    private void initViews() {
        mToolbar = (Toolbar) findViewById(R.id.actionbar_toolbar);
        setSupportActionBar(mToolbar);

        setTitle(null);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        setupNavDrawer();
    }

    private void setupNavDrawer() {
        TypedArray ta = obtainStyledAttributes(new int[] {
                R.attr.color_primary_dark
        });
        int colorPrimaryDark = ta.getColor(0, R.color.theme_primary_dark);
        ta.recycle();
        mDrawerLayout.setStatusBarBackgroundColor(colorPrimaryDark);

        ScrimInsetsScrollView navDrawer = (ScrimInsetsScrollView)
                mDrawerLayout.findViewById(R.id.navdrawer);

        View drawerHeader = navDrawer.findViewById(R.id.drawer_header);

        drawerHeader.setBackgroundResource(mDrawerHeaderBg);

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {
            }

            @Override
            public void onDrawerStateChanged(int newState) {
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerToggle.syncState();
        populateNavDrawer();

        setSelectedNavDrawerItem(NAVDRAWER_ITEM_NEWS_ARTICES);
        // When the user runs the app for the first time, we want to land them with the
        // navigation drawer open. But just the first time.
        if (!PrefUtils.isWelcomeDone(this)) {
            // first run of the app starts with the nav drawer open
            PrefUtils.markWelcomeDone(this);
            mDrawerLayout.openDrawer(Gravity.START);
        }
    }

    private void populateNavDrawer() {
        mNavDrawerItems.clear();
        mNavDrawerItems.add(NAVDRAWER_ITEM_NEWS_ARTICES);
        mNavDrawerItems.add(NAVDRAWER_ITEM_RECOMMEND_COMMENT);
        mNavDrawerItems.add(NAVDRAWER_ITEM_TOP);
        mNavDrawerItems.add(NAVDRAWER_ITEM_TOPIC);
        mNavDrawerItems.add(NAVDRAWER_ITEM_FAVORITE);
        mNavDrawerItems.add(NAVDRAWER_ITEM_SEPARATOR);
        mNavDrawerItems.add(NAVDRAWER_ITEM_SETTINGS);
        mNavDrawerItems.add(NAVDRAWER_ITEM_ABOUT);
        mNavDrawerItems.add(NAVDRAWER_ITEM_NIGHT);

        createNavDrawerItems();
    }

    private void createNavDrawerItems() {
        mDrawerItemsListContainer = (ViewGroup) findViewById(R.id.navdrawer_items_list);
        if (mDrawerItemsListContainer == null) {
            return;
        }

        mNavDrawerItemViews = new View[mNavDrawerItems.size()];
        mDrawerItemsListContainer.removeAllViews();
        int i = 0;
        for (int itemId : mNavDrawerItems) {
            mNavDrawerItemViews[i] = makeNavDrawerItem(itemId, mDrawerItemsListContainer);
            mDrawerItemsListContainer.addView(mNavDrawerItemViews[i]);
            ++i;
        }
    }

    private View makeNavDrawerItem(final int itemId, ViewGroup container) {
        boolean selected = mSelected;
        int layoutToInflate = 0;
        if (itemId == NAVDRAWER_ITEM_SEPARATOR) {
            layoutToInflate = R.layout.navdrawer_separator;
        } else if (itemId == NAVDRAWER_ITEM_SEPARATOR_SPECIAL) {
            layoutToInflate = R.layout.navdrawer_separator;
        } else {
            layoutToInflate = R.layout.navdrawer_item;
        }
        View view = getLayoutInflater().inflate(layoutToInflate, container, false);

        if (isSeparator(itemId)) {
            // we are done
            UIUtils.setAccessibilityIgnore(view);
            return view;
        }

        ImageView iconView = (ImageView) view.findViewById(R.id.icon);
        TextView titleView = (TextView) view.findViewById(R.id.title);
        int iconId = itemId >= 0 && itemId < NAVDRAWER_ICON_RES_ID.length ?
                NAVDRAWER_ICON_RES_ID[itemId] : 0;
        int titleId = itemId >= 0 && itemId < NAVDRAWER_TITLE_RES_ID.length ?
                NAVDRAWER_TITLE_RES_ID[itemId] : 0;

        // set icon and text
        iconView.setVisibility(iconId > 0 ? View.VISIBLE : View.GONE);
        if (iconId > 0) {
            iconView.setImageResource(iconId);
        }
        titleView.setText(getString(titleId));

        formatNavDrawerItem(view, itemId, selected);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNavDrawerItemClicked(itemId);
            }
        });

        if (itemId == NAVDRAWER_ITEM_NIGHT) {
            mNightDrawerItem = view;
        }
        return view;
    }

    private void onNavDrawerItemClicked(final int itemId) {
        // launch the target Activity after a short delay, to allow the close animation to play
        if (itemId != NAVDRAWER_ITEM_ABOUT && itemId != NAVDRAWER_ITEM_SETTINGS && itemId != NAVDRAWER_ITEM_NIGHT) {
            mDrawerLayout.closeDrawer(Gravity.START);
            setSelectedNavDrawerItem(itemId);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    goToNavDrawerItem(itemId);
                }
            }, NAVDRAWER_LAUNCH_DELAY);
        } else {
            goToNavDrawerItem(itemId);
        }

    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    private void formatNavDrawerItem(View view, int itemId, boolean selected) {
        if (isSeparator(itemId)) {
            // not applicable
            return;
        }

        ImageView iconView = (ImageView) view.findViewById(R.id.icon);
        TextView titleView = (TextView) view.findViewById(R.id.title);

        boolean isNight = PrefUtils.isNightMode(this);
        if (selected) {
            int drawable = isNight ? R.drawable.selected_navdrawer_item_background_dark :
                    R.drawable.selected_navdrawer_item_background;
            view.setBackgroundResource(drawable);
        } else {
            view.setBackgroundResource(R.drawable.screen_background_light_transparent);
        }

        int[] attrs = new int[] {
                R.attr.nav_item_text_color,
                R.attr.nav_item_text_color_selected
        };
        TypedArray ta = obtainStyledAttributes(attrs);
        int color = ta.getColor(0, R.color.navdrawer_text_color);
        int colorSelected = ta.getColor(1, R.color.navdrawer_text_color_selected);

        ta.recycle();
        // configure its appearance according to whether or not it's selected
        titleView.setTextColor(selected ?
                colorSelected :
                color);
        iconView.setColorFilter(selected ?
                colorSelected : color);
    }

    private void setSelectedNavDrawerItem(int itemId) {
        if (mNavDrawerItemViews != null) {
            for (int i = 0; i < mNavDrawerItemViews.length; i++) {
                if (i < mNavDrawerItems.size()) {
                    int thisItemId = mNavDrawerItems.get(i);
                    formatNavDrawerItem(mNavDrawerItemViews[i], thisItemId, itemId == thisItemId);
                }
            }
        }
    }

    private boolean isSeparator(int itemId) {
        return itemId == NAVDRAWER_ITEM_SEPARATOR || itemId == NAVDRAWER_ITEM_SEPARATOR_SPECIAL;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mDoubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.mDoubleBackToExitPressedOnce = true;
        Toast.makeText(this, R.string.press_again, Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                mDoubleBackToExitPressedOnce =false;
            }
        }, 2000);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //        mToolbar.getBackground().setAlpha(mToolbarAlpha);
    }

    private void goToNavDrawerItem(int item) {
        Intent intent;
        Fragment fragment;
        switch (item) {
            case NAVDRAWER_ITEM_NEWS_ARTICES:
                fragment = getSupportFragmentManager().findFragmentByTag(TAG_NEWS_ARTICES);
                if (fragment == null) {
                    fragment = NewsListFragment.newInstance(mActionBarSize);
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        fragment, TAG_NEWS_ARTICES).commit();
                break;
            case NAVDRAWER_ITEM_RECOMMEND_COMMENT:
                fragment = getSupportFragmentManager().findFragmentByTag(TAG_RECOMMEND_COMMENT);
                if (fragment == null) {
                    fragment = new TopCommentsFragment();
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        fragment, TAG_RECOMMEND_COMMENT).commit();
                break;
            case NAVDRAWER_ITEM_FAVORITE:
                fragment = getSupportFragmentManager().findFragmentByTag(TAG_FAVORITE);
                if (fragment == null) {
                    fragment = new FavoriteFragment();
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        fragment, TAG_FAVORITE).commit();
                break;
            case NAVDRAWER_ITEM_TOP:
                fragment = getSupportFragmentManager().findFragmentByTag(TAG_TOP);
                if (fragment == null) {
                    fragment = new TopPagerFragment();
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        fragment, TAG_TOP).commit();
                break;
            case NAVDRAWER_ITEM_TOPIC:
                fragment = getSupportFragmentManager().findFragmentByTag(TAG_TOPIC);
                if (fragment == null) {
                    fragment = new TopicPagerFragment();
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        fragment, TAG_TOPIC).commit();
                break;
            case NAVDRAWER_ITEM_NIGHT:
                int theme = PrefUtils.isNightMode(this) ? R.style.AppTheme : R.style.AppThemeDark;
                PrefUtils.toggleNightMode(this);
                Utils.changeToTheme(this, theme);
                break;
            case NAVDRAWER_ITEM_SETTINGS:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            case NAVDRAWER_ITEM_ABOUT:
                intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                break;
        }
    }


    @Override
    public void onGenerated(Palette palette) {
        if (palette != null) {

            Palette.Swatch vibrantSwatch = palette
                    .getVibrantSwatch();

            Palette.Swatch darkVibrantSwatch = palette
                    .getDarkVibrantSwatch();

            Palette.Swatch lightSwatch = palette
                    .getLightVibrantSwatch();

            if (lightSwatch != null) {

//                vibrantSwatch.
//                暂时没啥用
//                获取一个图片的主要颜色
//                ((ImageView) findViewById(R.id.image)).setColorFilter(vibrantSwatch.getRgb(), PorterDuff.Mode.OVERLAY);

            }
        }
    }
}
