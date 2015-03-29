/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.ui;

import static me.zheteng.cbreader.data.CnBetaContract.FavoriteEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.bignerdranch.android.multiselector.MultiSelector;
import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.google.gson.Gson;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import me.zheteng.actionabletoastbar.ActionableToastBar;
import me.zheteng.cbreader.R;
import me.zheteng.cbreader.model.Article;

/**
 * 收藏界面
 */
public class FavoriteFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Article>>,
        View.OnTouchListener {
    public static final int LOADER_ID = 0;
    private static final String[] FAVORITE_PROJECTION = new String[] {
            FavoriteEntry.COLUMN_SID,
            FavoriteEntry.COLUMN_ARTICLE,
            FavoriteEntry.COLUMN_NEWSCONTENT,
            FavoriteEntry.COLUMN_COMMENT,
            FavoriteEntry.COLUMN_CREATE_TIME,
    };

    static final int COL_SID = 0;
    static final int COL_ARTICLE = 1;
    static final int COL_NEWSCONTENT = 2;
    static final int COL_COMMENT = 3;
    static final int COL_CREATE_TIME = 4;

    private ObservableRecyclerView mRecyclerView;
    private ArticleListAdapter mAdapter;
    private MainActivity mActivity;
    private SharedPreferences mPref;

    private LinearLayoutManager mLayoutManager;
    private ActionMode mActionMode;

    private ActionableToastBar mUndoBar;
    private View mUndoFrame;
    private List<Article> mToDeleteList;

    private MultiSelector mMultiSelector = new MultiSelector();
    private boolean mUndoBarShow;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);
        initVIew(view);
        return view;
    }

    private void initVIew(View view) {
        mRecyclerView = ((ObservableRecyclerView) view.findViewById(R.id.favorite_list));
        mUndoBar = ((ActionableToastBar) view.findViewById(R.id.undo_bar));
        mUndoFrame = view.findViewById(R.id.undo_frame);
        mUndoFrame.setOnTouchListener(this);
    }

    protected void setupRecyclerView() {
        mLayoutManager = new LinearLayoutManager(mActivity);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setScrollViewCallbacks(new ShowHideToolbarListener(mActivity, mRecyclerView));

        mAdapter = new ArticleListAdapter(mActivity, null, mRecyclerView,
                mPref.getBoolean(getString(R.string.pref_autoload_image_in_list_key), true), true);
        String style = mPref.getString(mActivity.getString(R.string.pref_list_style_key),
                mActivity.getString(R.string.pref_card_style_value));
        mAdapter.setStyle(style);
        mAdapter.setLoadOnlyOneArticle(true);
        mAdapter.setOnItemDissmissListener(new ArticleListAdapter.OnItemDismissListener() {
            @Override
            public void onDismiss(RecyclerView recyclerView, final int[] reverseSortedPositions) {
                mToDeleteList = new ArrayList<Article>(reverseSortedPositions.length);
                for (int position : reverseSortedPositions) {
                    mToDeleteList.add(mAdapter.getData().remove(position));
                }
                // do not call notifyItemRemoved for every item, it will cause gaps on deleting items
                mAdapter.notifyDataSetChanged();

                mUndoFrame.setVisibility(View.VISIBLE);
                mUndoBar.show(new ActionableToastBar.ActionClickedListener() {
                    @Override
                    public void onActionClicked() {
                        for (int i = reverseSortedPositions.length - 1; i >= 0; i--) {
                            mAdapter.getData()
                                    .add(reverseSortedPositions[i], mToDeleteList.remove(mToDeleteList.size() - 1));
                        }
                        mAdapter.notifyDataSetChanged();
                        mUndoBarShow = false;
                    }
                }, 0, "收藏已删除.", true, R.string.alarm_undo, true);
                mUndoBarShow = true;
            }
        });

        mRecyclerView.setAdapter(mAdapter);
    }

    private void hideUndoBar(boolean animate, MotionEvent event) {
        if (mUndoBar != null && mUndoBarShow) {
            mUndoFrame.setVisibility(View.GONE);
            if (event != null && mUndoBar.isEventInToastBar(event)) {
                // Avoid touches inside the undo bar.
                return;
            }
            doDelete();
            mUndoBar.hide(animate);
            mUndoBarShow = false;
        }

        //        mDeletedAlarm = null;
        //        mUndoShowing = false;
    }

    private void doDelete() {
        if (mToDeleteList == null || mToDeleteList.size() == 0) {
            return;
        }
        String where = FavoriteEntry.COLUMN_SID + " in ( ? )";
        StringBuilder argument = new StringBuilder();
        for (int i = 0; i < mToDeleteList.size(); i++) {
            argument.append(mToDeleteList.get(i).sid);
            if (i != mToDeleteList.size() - 1) {
                argument.append(", ");
            }
        }
        mActivity.getContentResolver().delete(FavoriteEntry.CONTENT_URI, where, new String[] {argument.toString()});
        mToDeleteList = null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        mPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
        mActivity.showToolbar();
        mActivity.getToolbar().getBackground().setAlpha(255);
        mActivity.getToolbar().setTitle(R.string.favorite);

        setupRecyclerView();

        getLoaderManager().initLoader(LOADER_ID, null, this);

    }

    @Override
    public Loader<List<Article>> onCreateLoader(int id, Bundle args) {
        Uri baseUri = FavoriteEntry.CONTENT_URI;

        return new ArticleListLoader(mActivity, baseUri, FAVORITE_PROJECTION, null, null,
                FavoriteEntry.COLUMN_CREATE_TIME + " DESC " + " LIMIT 100");
    }

    @Override
    public void onLoadFinished(Loader<List<Article>> loader, List<Article> data) {
        mAdapter.swapData(data);
    }

    @Override
    public void onLoaderReset(Loader<List<Article>> loader) {
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        hideUndoBar(true, event);
        return false;
    }

    public static class ArticleListLoader extends ListCursorLoader<Article> {

        Gson mGson;

        public ArticleListLoader(Context context, Uri uri, String[] projection, String selection,
                                 String[] selectionArgs, String sortOrder) {
            super(context, uri, projection, selection, selectionArgs, sortOrder);
            mGson = new Gson();
        }

        @Override
        protected List<Article> getListFromCursor(Cursor cursor) {
            if (cursor == null) {
                return Collections.emptyList();
            }
            List<Article> list = new ArrayList<>(cursor.getCount());
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                String json = cursor.getString(COL_ARTICLE);
                Article article = mGson.fromJson(json, Article.class);
                list.add(article);
            }
            return list;
        }
    }

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_favorite_context, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    //                    deleteSelected();
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
        }
    };

}
