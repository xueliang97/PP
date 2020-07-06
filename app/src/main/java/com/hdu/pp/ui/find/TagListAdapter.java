package com.hdu.pp.ui.find;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hdu.libcommon.extention.AbsPagedListAdapter;
import com.hdu.pp.databinding.LayoutTagListItemBinding;
import com.hdu.pp.model.TagList;
import com.hdu.pp.ui.home.InteractionPresenter;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

public class TagListAdapter extends AbsPagedListAdapter<TagList,TagListAdapter.ViewHolder> {
    private final Context mContext;
    private final LayoutInflater mInflater;

    protected TagListAdapter(Context context) {


        super(new DiffUtil.ItemCallback<TagList>() {
            @Override
            public boolean areItemsTheSame(@NonNull TagList oldItem, @NonNull TagList newItem) {
                return oldItem.tagId==newItem.tagId;
            }

            @Override
            public boolean areContentsTheSame(@NonNull TagList oldItem, @NonNull TagList newItem) {
                return oldItem.equals(newItem);
            }
        });
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    protected int getItemViewType2(int position) {
        return 0;
    }

    @Override
    protected ViewHolder onCreateViewHolder2(ViewGroup parent, int viewType) {
        LayoutTagListItemBinding itemBinding = LayoutTagListItemBinding.inflate(mInflater, parent, false);
        return new ViewHolder(itemBinding.getRoot(),itemBinding);
    }

    @Override
    protected void onBindViewHolder2(ViewHolder holder, int position) {
        holder.bindData(getItem(position));
        holder.mItemBinding.actionFollow.setOnClickListener((v)-> {
            InteractionPresenter.toggleTagLike((LifecycleOwner) mContext,getItem(position));
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private LayoutTagListItemBinding mItemBinding;

        public ViewHolder(@NonNull View itemView, LayoutTagListItemBinding itemBinding) {
            super(itemView);
            mItemBinding = itemBinding;
        }

        public void bindData(TagList item) {
            mItemBinding.setTagList(item);
        }
    }
}
