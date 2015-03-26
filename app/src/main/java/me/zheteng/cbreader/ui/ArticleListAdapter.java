/*
 * Copyright (C) 2015 junyuecao@gmail.com All Rights Reserved.
 */
package me.zheteng.cbreader.ui;

import java.util.ArrayList;
import java.util.List;

import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;
import me.zheteng.cbreader.R;
import me.zheteng.cbreader.model.Article;
import me.zheteng.cbreader.ui.widget.MaterialProgressBar;

public class ArticleListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements View.OnClickListener {
    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_ITEM = 1;
    private static final int VIEW_TYPE_PROG = 2;

    private List<Article> mData;

    private boolean mHasHeaderView;
    private int mFlexibleSpaceImageHeight;
    private RecyclerView mRecyclerView;
    private Activity mContext;
    private boolean mIsShowThumb = true;
    public boolean mIsFromTopFragment;

    public ArticleListAdapter(Activity context, List<Article> data, boolean hasHeaderView, RecyclerView view,
                              int flexibleSpaceImageHeight, boolean showImage) {
        mContext = context;
        mData = data == null ? new ArrayList<Article>() : data;
        if (hasHeaderView) {
            mHasHeaderView = hasHeaderView;
        }
        this.mRecyclerView = view;
        this.mFlexibleSpaceImageHeight = flexibleSpaceImageHeight;
        mIsShowThumb = showImage;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_HEADER:
                View headerView = new View(mContext);
                headerView.setLayoutParams(
                        new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,
                                mFlexibleSpaceImageHeight));
                headerView.setMinimumHeight(mFlexibleSpaceImageHeight);
                // This is required to disable header's list selector effect
                headerView.setClickable(true);
                return new HeaderViewHolder(headerView);
            case VIEW_TYPE_ITEM:
                View view = LayoutInflater.from(mContext).inflate(R.layout.feed_list_item, parent, false);

                ArticleItemViewHolder viewHolder = new ArticleItemViewHolder(view);
                view.setOnClickListener(this);
                return viewHolder;
            case VIEW_TYPE_PROG:
                return new ProgressViewHolder(LayoutInflater.from(mContext).inflate(R.layout.progress_item,
                        parent, false));
            default:
                throw new UnsupportedOperationException("没有这个ViewType");
        }

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ArticleItemViewHolder) {
            ArticleItemViewHolder holder1 = (ArticleItemViewHolder) holder;

            Article article = getItem(position);
            holder1.mTitleView.setText(article.title);
            holder1.mDescriptionView.setText(article.summary.replaceAll("&nbsp;", " "));
            holder1.mTimeView.setText(article.getReadableTime());
            holder1.mCommentCountView.setText("" + article.comments);
            if (mIsShowThumb) {
                holder1.mThumbImage.setVisibility(View.VISIBLE);
                Picasso.with(mContext)
                        .load(article.thumb)
                        .resizeDimen(R.dimen.thumb_image_size, R.dimen.thumb_image_size)
                        .into(holder1.mThumbImage);
            } else {
                holder1.mThumbImage.setVisibility(View.GONE);
            }
        }
    }

    private Article getItem(int position) {
        if (mHasHeaderView) {
            position = position - 1;
        }

        return mData.get(position);

    }

    @Override
    public int getItemViewType(int position) {
        if (!mHasHeaderView) {
            return VIEW_TYPE_ITEM;
        } else {
            if (position == 0) {
                return VIEW_TYPE_HEADER;
            } else {
                return getItem(position) != null ? VIEW_TYPE_ITEM : VIEW_TYPE_PROG;
            }
        }
    }

    @Override
    public int getItemCount() {
        int itemCount = 0;
        if (mData == null) {
            itemCount = 0;
        } else {
            itemCount = mData.size();
        }

        if (!mHasHeaderView) {
            return itemCount;
        } else {
            return itemCount + 1;
        }
    }

    private int[] getAllSid() {
        if (mData == null) {
            return new int[0];
        }

        int[] sids = new int[mData.size()];
        int i = 0;
        for (Article article : mData) {
            sids[i++] = article.sid;
        }
        return sids;
    }

    public List<Article> appendData(List<Article> articles) {
        List<Article> newList = new ArrayList<>(mData.size() + articles.size());
        newList.addAll(mData);
        newList.addAll(articles);

        mData = newList;
        this.notifyDataSetChanged();
        return mData;
    }

    public List<Article> swapData(List<Article> articles) {
        if (mData == articles) {
            return null;
        }
        List<Article> oldData = mData;
        this.mData = articles;
        if (articles != null) {
            this.notifyDataSetChanged();
        }
        return oldData;
    }

    public void addItem(Article article) {
        if (mData == null) {
            return;
        }
        mData.add(article);
        notifyDataSetChanged();
    }

    public void removeLast() {
        if (mData == null) {
            return;
        }
        mData.remove(mData.size() - 1);
        notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        int position = mRecyclerView.getChildPosition(v);
        if (mData != null) {
            Intent intent = new Intent(mContext, ReadActivity.class);
            intent.putParcelableArrayListExtra(ReadActivity.ARTICLE_ARTICLES_KEY,
                    (ArrayList<? extends android.os.Parcelable>) mData);
            intent.putExtra(ReadActivity.ARTICLE_POSITON_KEY, mData.indexOf(getItem(position)));
            intent.putExtra(ReadActivity.FROM_TOP_KEY, mIsFromTopFragment);

            mContext.startActivityForResult(intent, NewsListFragment.CURRENT_STATE_REQUEST);
        }

    }

    public List<Article> getData() {
        return mData;
    }

    public void setIsShowThumb(boolean mIsShowThumb) {
        this.mIsShowThumb = mIsShowThumb;
        mRecyclerView.invalidate();
    }

    public void setIsFromTopFragment(boolean b) {
        mIsFromTopFragment = b;
    }

    public class ArticleItemViewHolder extends RecyclerView.ViewHolder {

        public ArticleItemViewHolder(View itemView) {
            super(itemView);

            mTitleView = ((TextView) itemView.findViewById(R.id.title));
            mDescriptionView = ((TextView) itemView.findViewById(R.id.description));
            mTimeView = ((TextView) itemView.findViewById(R.id.time));
            mCommentCountView = ((TextView) itemView.findViewById(R.id.comment_count));
            mContainer = (ViewGroup) itemView.findViewById(R.id.container);
            mThumbImage = (ImageView) itemView.findViewById(R.id.thumb_image);
        }

        // each data item is just a string in this case
        public TextView mTitleView;
        public TextView mDescriptionView;
        public TextView mTimeView;
        public TextView mCommentCountView;

        public ViewGroup mContainer;
        public ImageView mThumbImage;

    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {

        public HeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

    public class ProgressViewHolder extends RecyclerView.ViewHolder {
        public MaterialProgressBar mProgressBar;

        public ProgressViewHolder(View v) {
            super(v);
            mProgressBar = (MaterialProgressBar) v.findViewById(R.id.list_progress);
        }
    }
}