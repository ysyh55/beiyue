/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import me.zheteng.cbreader.MainApplication;
import me.zheteng.cbreader.R;
import me.zheteng.cbreader.model.CmntDetail;
import me.zheteng.cbreader.model.CmntItem;
import me.zheteng.cbreader.model.NewsComment;
import me.zheteng.cbreader.model.WebCmnt;
import me.zheteng.cbreader.ui.widget.MaterialProgressBar;
import me.zheteng.cbreader.utils.APIUtils;
import me.zheteng.cbreader.utils.PrefUtils;
import me.zheteng.cbreader.utils.Utils;
import me.zheteng.cbreader.utils.volley.CmtGsonRequest;
import me.zheteng.cbreader.utils.volley.DoCommentRequest;
import me.zheteng.cbreader.utils.volley.GsonRequest;
import me.zheteng.cbreader.utils.volley.StringRequest;

/**
 * 查看评论
 */
public class CommentActivity extends SwipeBackActionBarActivity implements ObservableScrollViewCallbacks,
        CmntListAdapter.OnCommentClickListener {
    public static final String ARTICLE_SID_KEY = "sid";
    public static final String ARTICLE_COUNTD_KEY = "count";
    private static final long REFRESH_DATA_DELAY = 500;

    private int mSid;

    private ObservableRecyclerView mRecyclerView;
    private int mToolbarHeight;
    private CommentListAdapter mAdapter; // json接口的Adapter
    private CmntListAdapter mCmntAdapter; // web接口的Adapter 两个Adapter选其一
    private LinearLayoutManager mLayoutManager;
    private TextView mNoDataHint;

    private Handler mHandler = new Handler();
    private WebCmnt mWebCmnt;

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
    private String mToken;
    private String mCaptchaUrl;
    private SharedPreferences mPref;
    private boolean mUseWebApi;

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
        initData();

    }

    private void initData() {
        mProgressbar.setVisibility(View.VISIBLE);
        mNoDataHint.setVisibility(View.GONE);
        mPref = PreferenceManager.getDefaultSharedPreferences(this);
        mUseWebApi = mPref.getBoolean(getString(R.string.pref_use_web_api_key), true);
        if (mUseWebApi) {
            MainApplication.requestQueue.add(new StringRequest(APIUtils.getArticleAddressBySid(mSid),
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String body) {
                            String sn = APIUtils.getSNFromArticleBody(body);
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("op", "1," + mSid + "," + sn);
                            MainApplication.requestQueue.add(new CmtGsonRequest<WebCmnt>(
                                    Request.Method.POST, APIUtils.CMT_URL, WebCmnt.class, APIUtils.ajaxHeaders,
                                    params,
                                    new Response.Listener<WebCmnt>() {
                                        @Override
                                        public void onResponse(WebCmnt webCmnt) {
                                            mProgressbar.setVisibility(View.GONE);
                                            mWebCmnt = webCmnt;
                                            setAdapter();
                                            mToken = webCmnt.token;
                                            System.out.println(webCmnt);
                                            if (mWebCmnt.cmntlist.size() == 0) {
                                                mNoDataHint.setVisibility(View.VISIBLE);
                                            }
                                        }
                                    }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError volleyError) {
                                    mProgressbar.setVisibility(View.GONE);
                                    mNoDataHint.setVisibility(View.VISIBLE);
                                }
                            }));
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    mNoDataHint.setVisibility(View.VISIBLE);
                    mProgressbar.setVisibility(View.GONE);
                }
            }));
        } else {
            mPage = mIsNewerFirst ? mCount / 10 + 1 : 1;
            requestData();

        }

    }

    private void setAdapter() {
        if (mUseWebApi) {
            mCmntAdapter = new CmntListAdapter(CommentActivity.this, mWebCmnt);
            mCmntAdapter.setClickListener(this);
            if (mRecyclerView != null) {
                mRecyclerView.setAdapter(mCmntAdapter);
                mProgressbar.setVisibility(View.GONE);
            }
        }
    }

    public void resetPage() {
        mPage = mPage = mIsNewerFirst ? mCount / 10 + 1 : 1;
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
                mProgressbar.setVisibility(View.VISIBLE);
                initData();
            }
        });

        mProgressbar = (MaterialProgressBar) findViewById(R.id.loading_progress);
    }

    protected void setupRecyclerView() {
        mRecyclerView.setScrollViewCallbacks(this);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        if (!mUseWebApi) {
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

            mAdapter = new CommentListAdapter(this, null);
            mRecyclerView.setAdapter(mAdapter);

        }

        mRecyclerView.setHasFixedSize(false);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_add_comment:

                showSendCommentDialog();

                break;

            case R.id.action_refresh:

                initData();

                break;
        }
        return true;
    }

    private void showSendCommentDialog() {
        showSendCommentDialog(null);
    }

    private void showSendCommentDialog(String pid) {
        if (mToken == null) {
            Toast.makeText(this, R.string.err_no_token, Toast.LENGTH_SHORT).show();
            return;
        }
        if (pid == null) {
            pid = "0";
        }

        PublishCommentFragment fragment = PublishCommentFragment.newInstance(mToken, mSid, pid);
        fragment.setPublishCommentDialogListener(new PublishCommentFragment.PublishCommentDialogListener() {
            @Override
            public void onCommentSuccess(DialogFragment dialog) {
//                        initData();
            }
        });
        fragment.show(getSupportFragmentManager(), "publish_comment");

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_comment, menu);
        return super.onCreateOptionsMenu(menu);
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

    @Override
    public void onCommentClicked(View v, CmntItem comment, CmntDetail detail) {
        showSendCommentDialog(comment.tid);
    }

    @Override
    public void onSupportClicked(View v, final CmntItem comment, final CmntDetail detail) {
        if (comment.supportAvailable) {
            doSupport(mSid, comment.tid, new OnDoSupportSuccessListener() {
                @Override
                public void onDoSupportSuccess() {
                    synchronized(this) {
                        if (comment.supportAvailable) {
                            detail.score = String.valueOf(Integer.parseInt(detail.score) + 1);
                            mCmntAdapter.notifyDataSetChanged();
                            comment.supportAvailable = false;
                            Toast.makeText(CommentActivity.this,
                                    R.string.support_success, Toast.LENGTH_SHORT).show();
                        }
                    }

                }
            });
        }
    }

    @Override
    public void onAgainstClicked(View v, final CmntItem comment, final CmntDetail detail) {
        if (comment.againstAvailable) {
            doAganst(mSid, comment.tid, new OnDoAgainstSuccessListener() {
                @Override
                public void onDoAgainstSuccess() {
                    synchronized(this) {
                        if (comment.againstAvailable) {
                            detail.reason = String.valueOf(Integer.parseInt(detail.reason) + 1);
                            mCmntAdapter.notifyDataSetChanged();
                            comment.againstAvailable = false;
                            Toast.makeText(CommentActivity.this,
                                    R.string.against_success, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
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
            holder1.mContainer.setTag(position);
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
            this.notifyDataSetChanged();
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
                                            Toast.makeText(CommentActivity.this,
                                                    R.string.support_success, Toast.LENGTH_SHORT).show();
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
                                            Toast.makeText(CommentActivity.this,
                                                    R.string.against_success, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            });
                        }
                        break;
                    case R.id.container:
                        comment = getItem((Integer) v.getTag());
                        showSendCommentDialog(comment.tid);
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

                mSupportView.setOnClickListener(CommentListAdapter.this);
                mOpposeView.setOnClickListener(CommentListAdapter.this);
                mContainer.setOnClickListener(CommentListAdapter.this);
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
        if (mToken == null) {
            Toast.makeText(this, "失败了, 请稍候再试一下吧! ", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, String> params = new HashMap<>();
        params.put("op", "support");
        params.put("sid", String.valueOf(sid));
        params.put("tid", tid);
        params.put("csrf_token", mToken);
        MainApplication.requestQueue.add(new DoCommentRequest<String>(Request.Method.POST,
                APIUtils.DO_CMT_URL, String.class,
                APIUtils.ajaxHeaders, params, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                if (s.equals("voted")) {
                    listener.onDoSupportSuccess();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                System.out.println(volleyError.networkResponse.toString());
            }
        }));
    }

    private void doAganst(int sid, String tid, final OnDoAgainstSuccessListener listener) {
        if (mToken == null) {
            Toast.makeText(this, "失败了, 请稍候再试一下吧! ", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, String> params = new HashMap<>();
        params.put("op", "against");
        params.put("sid", String.valueOf(sid));
        params.put("tid", tid);
        params.put("csrf_token", mToken);
        MainApplication.requestQueue.add(new DoCommentRequest<String>(Request.Method.POST,
                APIUtils.DO_CMT_URL, String.class,
                APIUtils.ajaxHeaders, params, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                if (s.equals("voted")) {
                    listener.onDoAgainstSuccess();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                System.out.println(volleyError.networkResponse.toString());
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
