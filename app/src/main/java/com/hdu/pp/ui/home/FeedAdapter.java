package com.hdu.pp.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.hdu.libcommon.extention.LiveDataBus;
import com.hdu.pp.R;
import com.hdu.pp.databinding.LayoutFeedTypeImageBinding;
import com.hdu.pp.databinding.LayoutFeedTypeVideoBinding;
import com.hdu.pp.model.Feed;
import com.hdu.pp.ui.detail.FeedDetailActivity;
import com.hdu.pp.view.ListPlayerView;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

public class FeedAdapter extends PagedListAdapter<Feed,FeedAdapter.ViewHolder> {
    LayoutInflater inflater;
    private String mCategory;
    private Context mContext;
    protected FeedAdapter(Context context,String category) { //差分异回调
        super(new DiffUtil.ItemCallback<Feed>() {
            @Override
            public boolean areItemsTheSame(@NonNull Feed oldItem, @NonNull Feed newItem) { //两个item是否相同
                return oldItem.id==newItem.id;
            }

            @Override
            public boolean areContentsTheSame(@NonNull Feed oldItem, @NonNull Feed newItem) { //内容是否相同
                return oldItem.equals(newItem);
            }
        });

        inflater = LayoutInflater.from(context);
        mCategory = category;
        mContext = context;
    }

    @Override
    public int getItemViewType(int position) {
       Feed feed = getItem(position);
       return feed.itemType;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewDataBinding binding = null;
        if (viewType==Feed.TYPE_IMAGE){
            binding = LayoutFeedTypeImageBinding.inflate(inflater);
        }else {
            binding = LayoutFeedTypeVideoBinding.inflate(inflater);
        }
        return new ViewHolder(binding.getRoot(),binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final  Feed feed = getItem(position);
        holder.bindData(feed);
        holder.itemView.setOnClickListener((v)->{
            FeedDetailActivity.startFeedDetailActivity(mContext,getItem(position),mCategory);
            onStartFeedDetailActivity(feed);
            if (mFeedObserver==null){
                mFeedObserver = new FeedObserver();
                LiveDataBus.get().with(InteractionPresenter.DATA_FROM_INTERACTION)
                        .observe((LifecycleOwner)mContext,mFeedObserver);
            }
            mFeedObserver.setFeed(feed);

        });
    }

    public void onStartFeedDetailActivity(Feed feed) {
    }

    private FeedObserver mFeedObserver;
    private class FeedObserver implements Observer<Feed>{
        private Feed mFeed;
        @Override
        public void onChanged(Feed feed) {
            if (mFeed.id!=feed.id)
                return;
            mFeed.author = feed.author;
            mFeed.ugc = feed.ugc;
            mFeed.notifyChange();
        }

        public void setFeed(Feed feed) {
            mFeed = feed;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewDataBinding mBinding;
        public ListPlayerView listPlayerView;
        public ImageView feedImage;

        public ViewHolder(@NonNull View itemView, ViewDataBinding binding) {
            super(itemView);
            mBinding = binding;
        }

        public void bindData(Feed item) {
            if (mBinding instanceof LayoutFeedTypeImageBinding){
                LayoutFeedTypeImageBinding imageBinding = (LayoutFeedTypeImageBinding) mBinding;
                imageBinding.setFeed(item);
                imageBinding.feedImage.bindData(item.width,item.height,16,item.cover);
                imageBinding.setLifecycleOwner((LifecycleOwner) mContext);
            }else{
                LayoutFeedTypeVideoBinding videoBinding = (LayoutFeedTypeVideoBinding) mBinding;
                videoBinding.setFeed(item);
                listPlayerView = videoBinding.listPlayerView;
                videoBinding.listPlayerView.bindData(mCategory,item.width,item.height,item.cover,item.url);
                videoBinding.setLifecycleOwner((LifecycleOwner) mContext);
            }
        }

        public boolean isVideoItem(){
            return mBinding instanceof LayoutFeedTypeVideoBinding;
        }

        public ListPlayerView getListPlayerView(){
            return listPlayerView;
        }
    }
}
