/*
 * Copyright (C) 2015 junyuecao@gmail.com All Rights Reserved.
 */
package me.zheteng.cbreader.ui;

import java.util.ArrayList;
import java.util.List;

import com.squareup.picasso.Picasso;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import me.zheteng.cbreader.R;
import me.zheteng.cbreader.model.Article;
import me.zheteng.cbreader.ui.widget.MaterialProgressBar;

public class ArticleListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_CARD = 1;
    private static final int VIEW_TYPE_LIST = 3;
    private static final int VIEW_TYPE_PROG = 2;
    private boolean mIsLoadingMore;
    private boolean mItemClickable;

    public void setOnLoadMoreListener(OnLoadMoreListener mLoadMoreListener) {
        this.mLoadMoreListener = mLoadMoreListener;
    }

    public void resetLoadMoreButton() {
        mIsLoadingMore = false;
        notifyDataSetChanged();
    }

    public void setItemClickable(boolean b) {
        mItemClickable = b;
    }

    private enum StyleMode {
        CARD,
        LIST,
        GRID
    }

    private List<Article> mData;

    private RecyclerView mRecyclerView;
    private Activity mContext;
    private boolean mIsShowThumb = true;
    public boolean mLoadOnlyOne; //传入这个参数可以让read页不加载更多
    private OnItemDismissListener mOnItemDismissListener;
    private OnLoadMoreListener mLoadMoreListener;

    private StyleMode mStyleMode = StyleMode.CARD;

    public void setStyle(StyleMode mode) {
        mStyleMode = mode;
    }

    public void setStyle(String style) {
        if (style.equals(mContext.getString(R.string.pref_card_style_value))) {
            mStyleMode = StyleMode.CARD;
        } else if (style.equals(mContext.getString(R.string.pref_list_style_value))) {
            mStyleMode = StyleMode.LIST;

        } else if (style.equals(mContext.getString(R.string.pref_grid_style_value))) {
            mStyleMode = StyleMode.GRID;
        }
    }

    public ArticleListAdapter(Activity context, List<Article> data, RecyclerView view,
                              boolean showImage, final boolean dismissable) {
        mContext = context;

        mData = data == null ? new ArrayList<Article>() : data;
        this.mRecyclerView = view;

        SwipeDismissRecyclerViewTouchListener touchListener =
                new SwipeDismissRecyclerViewTouchListener(
                        mRecyclerView,
                        new SwipeDismissRecyclerViewTouchListener.DismissCallbacks() {
                            @Override
                            public boolean canDismiss(int position) {
                                return dismissable;
                            }

                            @Override
                            public void onDismiss(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                if (mOnItemDismissListener != null) {
                                    mOnItemDismissListener.onDismiss(recyclerView, reverseSortedPositions);
                                }
                            }
                        });

        mRecyclerView.setOnTouchListener(touchListener);
        mRecyclerView.setOnScrollListener(touchListener.makeScrollListener());
        mRecyclerView.addOnItemTouchListener(new RecyclerItemClickListener(mContext,
                new OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        if (mData != null) {
                            if (position == mData.size()) {
                                view.findViewById(R.id.load_more).setVisibility(View.GONE);
                                if (mLoadMoreListener != null) {
                                    mLoadMoreListener.onLoadMoreButtonClicked();
                                }
                            } else {

                                Intent intent = new Intent(mContext, ReadActivity.class);
                                intent.putParcelableArrayListExtra(ReadActivity.ARTICLE_ARTICLES_KEY,
                                        (ArrayList<? extends android.os.Parcelable>) mData);
                                intent.putExtra(ReadActivity.ARTICLE_POSITON_KEY, mData.indexOf(getItem(position)));
                                intent.putExtra(ReadActivity.DISABLE_LOAD_MORE_KEY, mLoadOnlyOne);

                                mContext.startActivityForResult(intent, NewsListFragment.CURRENT_STATE_REQUEST);
                            }
                        }
                    }
                }));

        mIsShowThumb = showImage;
    }

    public void setOnItemDissmissListener(OnItemDismissListener mOnItemDismissListener) {
        this.mOnItemDismissListener = mOnItemDismissListener;
    }

    public interface OnItemClickListener {
        public void onItemClick(View view, int position);
    }

    public interface OnItemDismissListener {
        public void onDismiss(RecyclerView recyclerView, int[] reverseSortedPositions);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case VIEW_TYPE_CARD:
                view = LayoutInflater.from(mContext).inflate(R.layout.feed_list_item, parent, false);
                ArticleItemViewHolder viewHolder = new ArticleItemViewHolder(view);
                view.setClickable(mItemClickable);

                return viewHolder;
            case VIEW_TYPE_LIST:
                view = LayoutInflater.from(mContext).inflate(R.layout.list_styled_item, parent, false);
                view.setClickable(mItemClickable);
                ListItemViewHolder viewHolder1 = new ListItemViewHolder(view);

                return viewHolder1;
            case VIEW_TYPE_PROG:
                view = LayoutInflater.from(mContext).inflate(R.layout.progress_item,
                        parent, false);
                ProgressViewHolder viewHolder2 = new ProgressViewHolder(view);

                return viewHolder2;
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
            if (mIsShowThumb && !TextUtils.isEmpty(article.thumb)) {
                holder1.mThumbImage.setVisibility(View.VISIBLE);
                Picasso.with(mContext)
                        .load(article.thumb)
                        .resizeDimen(R.dimen.thumb_image_size, R.dimen.thumb_image_size)
                        .into(holder1.mThumbImage);
            } else {
                holder1.mThumbImage.setVisibility(View.GONE);
            }

        } else if (holder instanceof ListItemViewHolder) {
            ListItemViewHolder holder1 = (ListItemViewHolder) holder;

            Article article = getItem(position);
            holder1.mTitleView.setText(article.title);
            holder1.mTimeView.setText(article.getReadableTime());
            holder1.mCommentCountView.setText("" + article.comments + "条评论");
            if (mIsShowThumb && !TextUtils.isEmpty(article.thumb)) {
                holder1.mThumbImage.setVisibility(View.VISIBLE);
                Picasso.with(mContext)
                        .load(article.thumb)
                        .resizeDimen(R.dimen.thumb_image_size, R.dimen.thumb_image_size)
                        .into(holder1.mThumbImage);
            } else {
                holder1.mThumbImage.setVisibility(View.GONE);
            }

        } else if (holder instanceof ProgressViewHolder) {
            ProgressViewHolder holder1 = (ProgressViewHolder) holder;
            if (mIsLoadingMore) {
                holder1.mLoadMore.setVisibility(View.GONE);
            } else {
                holder1.mLoadMore.setVisibility(View.VISIBLE);
            }
        }
    }

    private Article getItem(int position) {
        return mData.get(position);

    }

    @Override
    public int getItemViewType(int position) {
        if (mData != null) {
            if (position < mData.size()) {
                return mStyleMode == StyleMode.CARD ? VIEW_TYPE_CARD : VIEW_TYPE_LIST;
            } else {
                return VIEW_TYPE_PROG;
            }
        } else {
            return VIEW_TYPE_PROG;
        }
    }

    @Override
    public int getItemCount() {
        int itemCount = 0;
        if (mData == null) {
            if (mLoadMoreListener != null) {
                itemCount = 1;
            } else {
                itemCount = 0;
            }
        } else {
            if (mLoadMoreListener != null) {
                itemCount = mData.size() + 1;
            } else {
                itemCount = mData.size();
            }
        }

        return itemCount;
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

    public List<Article> getData() {
        return mData;
    }

    public void setIsShowThumb(boolean mIsShowThumb) {
        this.mIsShowThumb = mIsShowThumb;
        mRecyclerView.invalidate();
    }

    public void setLoadOnlyOneArticle(boolean b) {
        mLoadOnlyOne = b;
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

    public class ListItemViewHolder extends RecyclerView.ViewHolder {

        public ListItemViewHolder(View itemView) {
            super(itemView);

            mTitleView = ((TextView) itemView.findViewById(R.id.title));
            mTimeView = ((TextView) itemView.findViewById(R.id.time));
            mCommentCountView = ((TextView) itemView.findViewById(R.id.comment_count));
            mContainer = (ViewGroup) itemView.findViewById(R.id.container);
            mThumbImage = (ImageView) itemView.findViewById(R.id.thumb_image);
        }

        // each data item is just a string in this case
        public TextView mTitleView;
        public TextView mTimeView;
        public TextView mCommentCountView;

        public ViewGroup mContainer;
        public ImageView mThumbImage;

    }

    public class ProgressViewHolder extends RecyclerView.ViewHolder {
        public MaterialProgressBar mProgressBar;
        public TextView mLoadMore;

        public ProgressViewHolder(View v) {
            super(v);
            mProgressBar = (MaterialProgressBar) v.findViewById(R.id.list_progress);
            mLoadMore = (TextView) v.findViewById(R.id.load_more);
        }
    }

    public class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {
        private OnItemClickListener mListener;

        GestureDetector mGestureDetector;

        public RecyclerItemClickListener(Context context, OnItemClickListener listener) {
            mListener = listener;
            mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
            View childView = view.findChildViewUnder(e.getX(), e.getY());
            if (childView != null && mListener != null && mGestureDetector.onTouchEvent(e)) {
                mListener.onItemClick(childView, view.getChildPosition(childView));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView view, MotionEvent motionEvent) {
        }
    }

    public interface OnLoadMoreListener {
        public void onLoadMoreButtonClicked();
    }
}