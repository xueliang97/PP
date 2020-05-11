package com.hdu.pp.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hdu.pp.R;
import com.hdu.pp.databinding.LayoutFeedTypeImageBinding;
import com.hdu.pp.databinding.LayoutFeedTypeVideoBinding;
import com.hdu.pp.model.Feed;

import androidx.annotation.NonNull;
import androidx.databinding.ViewDataBinding;
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
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewDataBinding mBinding;

        public ViewHolder(@NonNull View itemView, ViewDataBinding binding) {
            super(itemView);
            mBinding = binding;
        }

        public void bindData(Feed item) {
            if (mBinding instanceof LayoutFeedTypeImageBinding){
                LayoutFeedTypeImageBinding imageBinding = (LayoutFeedTypeImageBinding) mBinding;
                imageBinding.setFeed(item);
                imageBinding.feedImage.bindData(item.width,item.height,16,item.cover);
            }else{
                LayoutFeedTypeVideoBinding videoBinding = (LayoutFeedTypeVideoBinding) mBinding;
                videoBinding.setFeed(item);
                videoBinding.listPlayerView.bindData(mCategory,item.width,item.height,item.cover,item.url);
            }
        }
    }
}