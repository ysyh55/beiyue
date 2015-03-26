/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.ui;

import java.util.ArrayList;
import java.util.List;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import me.zheteng.cbreader.MainApplication;
import me.zheteng.cbreader.R;
import me.zheteng.cbreader.model.NewsComment;
import me.zheteng.cbreader.ui.widget.MaterialProgressBar;
import me.zheteng.cbreader.utils.APIUtils;
import me.zheteng.cbreader.utils.PrefUtils;
import me.zheteng.cbreader.utils.Utils;
import me.zheteng.cbreader.utils.volley.GsonRequest;

/**
 * 查看评论
 */
public class CommentActivity extends BaseActivity implements ObservableScrollViewCallbacks {
    public static final String ARTICLE_SID_KEY = "sid";
    public static final String ARTICLE_COUNTD_KEY = "count";

    private int mSid;

    private ObservableRecyclerView mRecyclerView;
    private int mToolbarHeight;
    private CommentListAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private TextView mNoDataHint;

    int mPage = 1;

    // ----- used for load more
    private int previousTotal = 0;
    private boolean loading = true;
    private int visibleThreshold = 5;
    int firstVisibleItem, visibleItemCount, totalItemCount;
    // ----- end  used for load more
    /**
     * 由于接口是最早发表的在最前,所以我们要知道有多少个,好反过来请求最后一页
     */
    private int mCount;
    private boolean mIsLoadingData;
    private boolean mIsNewerFirst;
    private MaterialProgressBar mProgressbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_comments);

        Intent intent = getIntent();
        mSid = intent.getIntExtra(ARTICLE_SID_KEY, -1);
        mCount = intent.getIntExtra(ARTICLE_COUNTD_KEY, -1);
        mIsNewerFirst = PrefUtils.getIsCommentLatestFirst(this);
        mPage = mIsNewerFirst ? mCount / 10 + 1 : 1;
        initViews();
        requestData();
    }

    private void requestData() {
        if (mPage <= 0) {
            return;
        }
        mIsLoadingData = true;
        MainApplication.requestQueue.add(new GsonRequest<NewsComment[]>(APIUtils.getCommentListWithPageUrl(mSid, mPage),
                NewsComment[].class, null, new Response.Listener<NewsComment[]>() {
            @Override
            public void onResponse(NewsComment[] newsComments) {
                mProgressbar.setVisibility(View.GONE);
                changePage();
                List<NewsComment> list = getList(newsComments);
                mAdapter.swapData(list);
                mIsLoadingData = false;
                if (list.size() == 0 ){
                    if(mCount > 0){
                        mNoDataHint.setText(R.string.empty_comment);
                    } else {
                        mNoDataHint.setText(R.string.no_comment);
                    }
                    mNoDataHint.setVisibility(View.VISIBLE);
                    mNoDataHint.setOnClickListener(null);
                    return;
                }
                if (list.size() < 10 && mPage > 0 && mIsNewerFirst) {
                    loadMoreData();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                mProgressbar.setVisibility(View.GONE);
                mNoDataHint.setVisibility(View.VISIBLE);
                Toast.makeText(CommentActivity.this, "加载错误", Toast.LENGTH_SHORT).show();
                mIsLoadingData = false;
            }
        }));
    }

    private void changePage() {
        if (mIsNewerFirst) {
            mPage--;
        } else {
            mPage++;
        }
    }

    private void loadMoreData() {
        if (mPage <= 0) {
            return;
        }
        mIsLoadingData = true;
        MainApplication.requestQueue.add(new GsonRequest<NewsComment[]>(APIUtils.getCommentListWithPageUrl(mSid, mPage),
                NewsComment[].class, null, new Response.Listener<NewsComment[]>() {
            @Override
            public void onResponse(NewsComment[] newsComments) {
                changePage();
                List<NewsComment> list = getList(newsComments);
                mAdapter.appendData(list);
                mIsLoadingData = false;
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(CommentActivity.this, "加载错误", Toast.LENGTH_SHORT).show();
                mNoDataHint.setVisibility(View.VISIBLE);
                mIsLoadingData = false;
            }
        }));

    }

    private List<NewsComment> getList(NewsComment[] newsComments) {
        return mIsNewerFirst ? Utils.getListFromArrayReverse(newsComments) : Utils
                .getListFromArray(newsComments);
    }

    private void initViews() {
        mToolbar = (Toolbar) findViewById(R.id.actionbar_toolbar);
        mToolbarHeight = mToolbar.getHeight();
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.comment));
        mToolbar.getBackground().setAlpha(255);

        mRecyclerView = (ObservableRecyclerView) findViewById(R.id.comment_list);
        setupRecyclerView();

        mNoDataHint = (TextView) findViewById(R.id.no_data_hint);
        mNoDataHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNoDataHint.setVisibility(View.GONE);
                requestData();
            }
        });

        mProgressbar = (MaterialProgressBar) findViewById(R.id.loading_progress);
    }

    protected void setupRecyclerView() {
        mRecyclerView.setScrollViewCallbacks(this);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                visibleItemCount = mRecyclerView.getChildCount();
                totalItemCount = mLayoutManager.getItemCount();
                firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

                if (loading) {
                    if (totalItemCount > previousTotal) {
                        loading = false;
                        previousTotal = totalItemCount;
                    }
                }
                if (!loading && (totalItemCount - visibleItemCount)
                        <= (firstVisibleItem + visibleThreshold)) {
                    // End has been reached

                    Log.i("...", "end called");

                    if (!mIsLoadingData) {
                        loadMoreData();
                    }

                    loading = true;
                }
            }
        });
        mRecyclerView.setHasFixedSize(false);
//        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));

        mAdapter = new CommentListAdapter(this, null);
        mRecyclerView.setAdapter(mAdapter);
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
    public void onScrollChanged(int i, boolean b, boolean b2) {

    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {

    }

    public class CommentListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
    implements View.OnClickListener {

        private List<NewsComment> mData;

        private Context mContext;

        public CommentListAdapter(Context context, List<NewsComment> data) {
            mContext = context;
            mData = data == null ? new ArrayList<NewsComment>() : data;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.comment_list_item, parent, false);

            CommentItemViewHolder viewHolder = new CommentItemViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            CommentItemViewHolder holder1 = (CommentItemViewHolder) holder;

            NewsComment comment = getItem(position);
            holder1.mNameView.setText(comment.getUsername());
            holder1.mContentView.setText(comment.content);
            holder1.mTimeView.setText(comment.getReadableTime());
            holder1.mSupportView.setText(String.format(getString(R.string.support), comment.support));
            holder1.mOpposeView.setText(String.format(getString(R.string.oppose), comment.against));
            holder1.mSupportView.setTag(position);
            holder1.mOpposeView.setTag(position);
        }

        private NewsComment getItem(int position) {
            return mData.get(position);

        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        @Override
        public int getItemCount() {
            int itemCount = 0;
            if (mData == null) {
                itemCount = 0;
            } else {
                itemCount = mData.size();
            }
            return itemCount;
        }

        public List<NewsComment> swapData(List<NewsComment> data) {
            if (mData == data) {
                return null;
            }
            List<NewsComment> oldCursor = mData;
            this.mData = data;
            if (data != null) {
                this.notifyDataSetChanged();
            }
            return oldCursor;
        }

        @Override
        public void onClick(View v) {

            int id = v.getId();
            if (mData != null) {
                final NewsComment comment;
                switch (id) {
                    case R.id.support:
                        comment = getItem((Integer) v.getTag());
                        if (comment.supportAvailable) {
                            doSupport(mSid, comment.tid, new OnDoSupportSuccessListener() {
                                @Override
                                public void onDoSupportSuccess() {
                                    synchronized(this) {
                                        if (comment.supportAvailable) {
                                            comment.support = String.valueOf(Integer.parseInt(comment.support) + 1);
                                            notifyDataSetChanged();
                                            comment.supportAvailable = false;
                                        }
                                    }

                                }
                            });
                        }
                        break;
                    case R.id.oppose:
                        comment = getItem((Integer) v.getTag());
                        if (comment.againstAvailable) {
                            doAganst(mSid, comment.tid, new OnDoAgainstSuccessListener() {
                                @Override
                                public void onDoAgainstSuccess() {
                                    synchronized(this) {
                                        if (comment.againstAvailable) {
                                            comment.against = String.valueOf(Integer.parseInt(comment.against) + 1);
                                            notifyDataSetChanged();
                                            comment.againstAvailable = false;
                                        }
                                    }
                                }
                            });
                        }
                        break;
                }
            }

        }

        public List<NewsComment> appendData(List<NewsComment> list) {
            List<NewsComment> newList = new ArrayList<>(mData.size() + list.size());
            newList.addAll(mData);
            newList.addAll(list);

            mData = newList;
            this.notifyDataSetChanged();
            return mData;
        }

        public class CommentItemViewHolder extends RecyclerView.ViewHolder {
            public CommentItemViewHolder(View itemView) {
                super(itemView);

                mNameView = ((TextView) itemView.findViewById(R.id.name));
                mContentView = ((TextView) itemView.findViewById(R.id.content));
                mTimeView = ((TextView) itemView.findViewById(R.id.time));
                mSupportView = ((Button) itemView.findViewById(R.id.support));
                mOpposeView = ((Button) itemView.findViewById(R.id.oppose));
                mContainer = (ViewGroup) itemView.findViewById(R.id.container);

//                mSupportView.setOnClickListener(CommentListAdapter.this);
//                mOpposeView.setOnClickListener(CommentListAdapter.this);
            }

            // each data item is just a string in this case
            public TextView mNameView;
            public TextView mContentView;
            public TextView mTimeView;
            public Button mSupportView;
            public Button mOpposeView;

            public ViewGroup mContainer;
        }
    }

    public Drawable getSelectedItemDrawable() {
        int[] attrs = new int[]{R.attr.selectableItemBackground};
        TypedArray ta = obtainStyledAttributes(attrs);
        Drawable selectedItemDrawable = ta.getDrawable(0);
        ta.recycle();
        return selectedItemDrawable;
    }
    private void doSupport(int sid, String tid, final OnDoSupportSuccessListener listener) {
        MainApplication.requestQueue.add(new GsonRequest<String>(APIUtils.getDoCmtSupportUrl(sid, tid), String.class,
                null, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                if (s.equals("操作成功")) {
                    listener.onDoSupportSuccess();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }));
    }

    private void doAganst(int sid, String tid, final OnDoAgainstSuccessListener listener) {
        MainApplication.requestQueue.add(new GsonRequest<String>(APIUtils.getDoCmtSupportUrl(sid, tid), String.class,
                null, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                if (s.equals("操作成功")) {
                    listener.onDoAgainstSuccess();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }));
    }

    public interface OnDoSupportSuccessListener {
        public void onDoSupportSuccess();
    }

    public interface OnDoAgainstSuccessListener {
        public void onDoAgainstSuccess();
    }
}
