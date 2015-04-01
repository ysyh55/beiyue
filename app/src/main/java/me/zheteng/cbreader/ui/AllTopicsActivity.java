/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.ui;

import static me.zheteng.cbreader.data.CnBetaContract.TopicEntry;

import com.woozzu.android.util.StringMatcher;
import com.woozzu.android.widget.IndexableListView;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.SectionIndexer;
import android.widget.TextView;
import me.zheteng.cbreader.R;
import me.zheteng.cbreader.model.Topic;

/**
 * 添加专题
 */
public class AllTopicsActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ID = 0;
    private static final String[] TOPIC_PROJECTION = new String[] {
            TopicEntry.COLUMN_TID,
            TopicEntry.COLUMN_TITLE,
            TopicEntry.COLUMN_THUMB,
            TopicEntry.COLUMN_LETTER,
            TopicEntry.COLUMN_CHECKED,
            TopicEntry._ID
    };

    static final int COL_TID = 0;
    static final int COL_TITLE = 1;
    static final int COL_THUMB = 2;
    static final int COL_LETTER = 3;
    static final int COL_CHECKED = 4;
    static final int COL_ID = 5;
    IndexableListView mTopicList;
    private MyAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_topic);

        initViews();
        initTopicList();
    }

    private void initViews() {
        mTopicList = ((IndexableListView) findViewById(R.id.topic_list));
    }

    private void initTopicList() {
        mAdapter = new MyAdapter(this);
        mTopicList.setAdapter(mAdapter);
        mTopicList.setFastScrollEnabled(true);
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, TopicEntry.CONTENT_URI, TOPIC_PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public class MyAdapter extends CursorAdapter implements SectionIndexer {
        LayoutInflater mInflater;
        private String mSections = "#ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        public MyAdapter(Context context) {
            super(context, null, false);
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return mCursor == null ? 0 : mCursor.getCount();
        }

        @Override
        public Topic getItem(int position) {
            mCursor.moveToPosition(position);
            Topic topic = new Topic();
            topic.tid = mCursor.getString(COL_TID);
            topic.title = mCursor.getString(COL_TITLE);
            topic.letter = mCursor.getString(COL_LETTER);
            topic.thumb = mCursor.getString(COL_THUMB);
            topic.checked = mCursor.getInt(COL_CHECKED);

            return topic;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            final View view = mInflater.inflate(R.layout.topic_list_item, parent, false);
            ViewHolder holder = new ViewHolder();
            holder.titleView = (TextView) view.findViewById(R.id.topic_item_title);
            holder.checkBox = (CheckBox) view.findViewById(R.id.topic_item_checkebox);
            holder.headerView = (TextView) view.findViewById(R.id.topic_list_item_header);

            view.setTag(holder);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.titleView.setText(cursor.getString(COL_TITLE));
            holder.checkBox.setChecked(cursor.getInt(COL_CHECKED) == 1);
            String letter = cursor.getString(COL_LETTER);
            if (cursor.moveToPrevious()) {
                if (!cursor.getString(COL_LETTER).equals(letter)) {
                    holder.headerView.setText(letter);
                    holder.headerView.setVisibility(View.VISIBLE);
                } else {
                    holder.headerView.setVisibility(View.GONE);
                }
            } else {
                holder.headerView.setText(letter);
                holder.headerView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public Object[] getSections() {
            String[] sections = new String[mSections.length()];
            for (int i = 0; i < mSections.length(); i++) {
                sections[i] = String.valueOf(mSections.charAt(i));
            }
            return sections;
        }

        @Override
        public int getPositionForSection(int section) {
            // If there is no item for current section, previous section will be selected
            for (int i = section; i >= 0; i--) {
                for (int j = 0; j < getCount(); j++) {
                    if (i == 0) {
                        // For numeric section
                        for (int k = 0; k <= 9; k++) {
                            String letter = getItem(j).letter;
                            if (StringMatcher.match(letter, String.valueOf(k))) {
                                return j;
                            }
                        }
                    } else {
                        if (StringMatcher.match(getItem(j).letter, String.valueOf(mSections.charAt(i)))) {
                            return j;
                        }
                    }
                }
            }
            return 0;
        }

        @Override
        public int getSectionForPosition(int position) {
            return 0;
        }

        class ViewHolder {
            TextView headerView;
            TextView titleView;
            CheckBox checkBox;
        }

    }
}
