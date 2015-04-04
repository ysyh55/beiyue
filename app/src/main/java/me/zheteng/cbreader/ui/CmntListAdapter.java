/*
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package me.zheteng.cbreader.ui;

import java.util.List;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import me.zheteng.cbreader.R;
import me.zheteng.cbreader.model.CmntDetail;
import me.zheteng.cbreader.model.CmntItem;
import me.zheteng.cbreader.model.Cmntdict;
import me.zheteng.cbreader.model.WebCmnt;
import me.zheteng.cbreader.ui.widget.FloorLayout;

/**
 * 网页接口的Adapter
 */
public class CmntListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements View.OnClickListener {

    private final Context mContext;
    private OnCommentClickListener mClickListener;
    private LayoutInflater mInflater;
    private WebCmnt mWebCmnt;

    public CmntListAdapter(Context context, WebCmnt webCmnt) {
        mContext = context;
        mWebCmnt = webCmnt;
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.comment_list_item, parent, false);
        CmntItemViewHolder holder = new CmntItemViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        CmntItemViewHolder holder1 = (CmntItemViewHolder) holder;

        CmntItem comment = getItem(position);
        List<Cmntdict> parents = mWebCmnt.cmntdict.get(comment.tid);
        CmntDetail cmntDetail = mWebCmnt.cmntstore.get(comment.tid);
        holder1.mNameView.setText(cmntDetail.name);
        holder1.mContentView.setText(cmntDetail.comment);
        holder1.mTimeView.setText(cmntDetail.getReadableTime());
        holder1.mSupportView.setText(String.format(mContext.getString(R.string.support), cmntDetail.score));
        holder1.mOpposeView.setText(String.format(mContext.getString(R.string.oppose), cmntDetail.reason));
        holder1.mSubFloor.setData(parents, mWebCmnt.cmntstore);

        holder1.mSupportView.setTag(position);
        holder1.mOpposeView.setTag(position);
        holder1.mContainer.setTag(position);
    }

    private CmntItem getItem(int position) {
        return mWebCmnt.cmntlist.get(position);
    }

    @Override
    public int getItemCount() {
        if (mWebCmnt == null || mWebCmnt.cmntlist == null) {
            return 0;
        }
        return mWebCmnt.cmntlist.size();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (mWebCmnt != null) {
            final CmntItem comment;
            final CmntDetail detail;
            switch (id) {
                case R.id.support:
                    comment = getItem((Integer) v.getTag());
                    detail = mWebCmnt.cmntstore.get(comment.tid);

                    mClickListener.onSupportClicked(v, comment, detail);
                    break;
                case R.id.oppose:
                    comment = getItem((Integer) v.getTag());
                    detail = mWebCmnt.cmntstore.get(comment.tid);

                    mClickListener.onAgainstClicked(v, comment, detail);
                    break;
                case R.id.container:
                    comment = getItem((Integer) v.getTag());
                    detail = mWebCmnt.cmntstore.get(comment.tid);
                    mClickListener.onCommentClicked(v, comment, detail);
                    break;
            }
        }
    }

    public void setClickListener(OnCommentClickListener mClickListener) {
        this.mClickListener = mClickListener;
    }

    public class CmntItemViewHolder extends RecyclerView.ViewHolder {

        public CmntItemViewHolder(View itemView) {
            super(itemView);
            mNameView = ((TextView) itemView.findViewById(R.id.name));
            mContentView = ((TextView) itemView.findViewById(R.id.content));
            mTimeView = ((TextView) itemView.findViewById(R.id.time));
            mSupportView = ((Button) itemView.findViewById(R.id.support));
            mOpposeView = ((Button) itemView.findViewById(R.id.oppose));
            mContainer = (ViewGroup) itemView.findViewById(R.id.container);
            mSubFloor = (FloorLayout) itemView.findViewById(R.id.sub_floor);

            if (mClickListener != null) {
                mSupportView.setOnClickListener(CmntListAdapter.this);
                mOpposeView.setOnClickListener(CmntListAdapter.this);
                mContainer.setOnClickListener(CmntListAdapter.this);
            }
        }

        // each data item is just a string in this case
        public TextView mNameView;
        public TextView mContentView;
        public TextView mTimeView;
        public Button mSupportView;
        public Button mOpposeView;
        public FloorLayout mSubFloor;

        public ViewGroup mContainer;
    }

    public interface OnCommentClickListener {
        void onCommentClicked(View v, CmntItem comment, CmntDetail detail);

        void onSupportClicked(View v, CmntItem comment, CmntDetail detail);

        void onAgainstClicked(View v, CmntItem comment, CmntDetail detail);
    }
}
