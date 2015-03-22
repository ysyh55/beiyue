/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.ui;

import static me.zheteng.cbreader.data.CnBetaContract.CommentEntry;

import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import me.zheteng.cbreader.R;
import me.zheteng.cbreader.model.Comment;
import me.zheteng.cbreader.model.FeedMessage;
import me.zheteng.cbreader.utils.TimeUtils;

/**
 * 查看评论
 */
public class CommentActivity extends BaseActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        ObservableScrollViewCallbacks {
    private static final int COMMENT_LOADER = 0;
    public static final String FEED_URI_KEY = "feed_uri";
    public static final String FEED_TITLE_KEY = "title";

    public static final String[] FEED_PROJECTION = new String[] {
            CommentEntry.COLUMN_NAME,
            CommentEntry.COLUMN_CONTENT,
            CommentEntry.COLUMN_PUB_DATE,
            CommentEntry.COLUMN_SUPPORT,
            CommentEntry.COLUMN_OPPOSE,
            CommentEntry.COLUMN_FEED_ID,
    };
    static final int COL_NAME = 0;
    static final int COL_CONTENT = 1;
    static final int COL_PUB_DATE = 2;
    static final int COL_SUPPORT = 3;
    static final int COL_OPPOSE = 4;
    static final int COL_FEED_ID = 5;

    private FeedMessage mMessage;
    private Uri mUri;
    private String mTitle;

    private ObservableRecyclerView mRecyclerView;
    private int mToolbarHeight;
    private CommentCursorAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_comments);

        Intent intent = getIntent();
        mUri = CommentEntry.buildCommentUri(intent.getStringExtra(FEED_URI_KEY));
        mTitle = intent.getStringExtra(FEED_TITLE_KEY);

        initViews();

        getSupportLoaderManager().initLoader(COMMENT_LOADER, null, this);

    }

    private void initViews() {
        mToolbar = (Toolbar) findViewById(R.id.actionbar_toolbar);
        mToolbarHeight = mToolbar.getHeight();
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.comment));

        mRecyclerView = (ObservableRecyclerView) findViewById(R.id.comment_list);
        setupRecyclerView();
    }

    protected void setupRecyclerView() {
        mRecyclerView.setScrollViewCallbacks(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));

        mAdapter = new CommentCursorAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (null != mUri) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    this,
                    mUri,
                    FEED_PROJECTION,
                    null,
                    null,
                    CommentEntry.COLUMN_PUB_DATE + " DESC"
            );
        }
        return null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();

        }
        return true;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onScrollChanged(int i, boolean b, boolean b2) {

    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {

    }

    public class CommentCursorAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
            implements View.OnClickListener {

        private Cursor dataCursor;

        private Context mContext;

        public CommentCursorAdapter(Context context) {
            mContext = context;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.comment_list_item, parent, false);

            CommentItemViewHolder viewHolder = new CommentItemViewHolder(view);
            view.setOnClickListener(this);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            CommentItemViewHolder holder1 = (CommentItemViewHolder) holder;

            Comment comment = getItem(position);
            holder1.mNameView.setText(comment.getName());
            holder1.mContentView.setText(comment.getContent());
            holder1.mTimeView.setText(TimeUtils.getElapsedTime(comment.getPubDate()));
            holder1.mSupportView.setText("" + comment.getSupport());
            holder1.mOpposeView.setText("" + comment.getOppose());
        }

        private Comment getItem(int position) {
            dataCursor.moveToPosition(position);
            String name = dataCursor.getString(COL_NAME);
            String content = dataCursor.getString(COL_CONTENT);
            long time = dataCursor.getLong(COL_PUB_DATE);
            int support = dataCursor.getInt(COL_SUPPORT);
            int oppose = dataCursor.getInt(COL_OPPOSE);

            String guid = dataCursor.getString(COL_FEED_ID);

            Comment comment = new Comment();
            comment.setFeedGuid(guid);
            comment.setName(name);
            comment.setContent(content);
            comment.setPubDate(time);
            comment.setSupport(support);
            comment.setOppose(oppose);

            return comment;

        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        @Override
        public int getItemCount() {
            int itemCount = 0;
            if (dataCursor == null) {
                itemCount = 0;
            } else {
                itemCount = dataCursor.getCount();
            }
            return itemCount;
        }

        public void changeCursor(Cursor cursor) {
            Cursor old = swapCursor(cursor);
            if (old != null) {
                old.close();
            }
        }

        public Cursor swapCursor(Cursor cursor) {
            if (dataCursor == cursor) {
                return null;
            }
            Cursor oldCursor = dataCursor;
            this.dataCursor = cursor;
            if (cursor != null) {
                this.notifyDataSetChanged();
            }
            return oldCursor;
        }

        @Override
        public void onClick(View v) {
            int position = mRecyclerView.getChildPosition(v);
            if (dataCursor != null) {

            }

        }

        public class CommentItemViewHolder extends RecyclerView.ViewHolder {
            public CommentItemViewHolder(View itemView) {
                super(itemView);

                mNameView = ((TextView) itemView.findViewById(R.id.name));
                mContentView = ((TextView) itemView.findViewById(R.id.content));
                mTimeView = ((TextView) itemView.findViewById(R.id.time));
                mSupportView = ((TextView) itemView.findViewById(R.id.support));
                mOpposeView = ((TextView) itemView.findViewById(R.id.oppose));
                mContainer = (ViewGroup) itemView.findViewById(R.id.container);
            }

            // each data item is just a string in this case
            public TextView mNameView;
            public TextView mContentView;
            public TextView mTimeView;
            public TextView mSupportView;
            public TextView mOpposeView;

            public ViewGroup mContainer;
        }
    }
}
