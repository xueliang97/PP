package com.hdu.pp.ui.detail;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.hdu.libcommon.extention.AbsPagedListAdapter;
import com.hdu.libcommon.utils.PixUtils;
import com.hdu.pp.databinding.LayoutFeedCommentListItemBinding;
import com.hdu.pp.login.MyUserManager;
import com.hdu.pp.model.Comment;
import com.hdu.pp.ui.MutableItemKeyedDataSource;
import com.hdu.pp.ui.home.InteractionPresenter;
import com.hdu.pp.ui.publish.PreviewActivity;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 评论列表的适配器
 */
public class FeedCommentAdapter extends AbsPagedListAdapter<Comment, FeedCommentAdapter.ViewHolder> {

    private Context mContext;
    protected FeedCommentAdapter(Context context) {
        super(new DiffUtil.ItemCallback<Comment>() {
            @Override
            public boolean areItemsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
                return oldItem.id==newItem.id;
            }

            @Override
            public boolean areContentsTheSame(@NonNull Comment oldItem, @NonNull Comment newItem) {
                return oldItem.equals(newItem);
            }
        });
        mContext = context;
    }

    @Override
    protected int getItemViewType2(int position) {
        return 0;
    }

    @Override
    protected ViewHolder onCreateViewHolder2(ViewGroup parent, int viewType) {
        LayoutFeedCommentListItemBinding binding = LayoutFeedCommentListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding.getRoot(),binding);
    }

    @Override
    protected void onBindViewHolder2(ViewHolder holder, int position) {
        Comment item = getItem(position);
        holder.bindData(item);
        holder.mBinding.commentDelete.setOnClickListener((v)->{
            InteractionPresenter.deleteFeedComment(mContext,item.itemId,item.commentId)
                    .observe((LifecycleOwner) mContext,(success)->{
                        if (success) {
                            MutableItemKeyedDataSource<Integer, Comment> dataSource = new MutableItemKeyedDataSource<Integer, Comment>((ItemKeyedDataSource) getCurrentList().getDataSource()) {
                                @NonNull
                                @Override
                                public Integer getKey(@NonNull Comment item) {
                                    return item.id;
                                }
                            };
                            PagedList<Comment> currentList =  getCurrentList();
                            for (Comment com:currentList){
                                if (com!=getItem(position)){
                                    dataSource.data.add(com);
                                }
                            }
                            PagedList<Comment> newPagedList = dataSource.buildNewPagedList(getCurrentList().getConfig());
                            submitList(newPagedList);
                        }
                    });
        });
        holder.mBinding.commentCover.setOnClickListener((v)->{
            boolean isVideo = item.commentType==Comment.COMMENT_TYPE_VIDEO;
            PreviewActivity.startActivityForResult((Activity) mContext, isVideo ? item.videoUrl : item.imageUrl, isVideo, null);
        });

    }

    public void addAndRefreshList(Comment comment) {
        PagedList<Comment> currentList = getCurrentList();
        MutableItemKeyedDataSource<Integer, Comment> mutableItemKeyedDataSource = new MutableItemKeyedDataSource<Integer, Comment>((ItemKeyedDataSource) currentList.getDataSource()) {
            @NonNull
            @Override
            public Integer getKey(@NonNull Comment item) {
                return item.id;
            }
        };
        mutableItemKeyedDataSource.data.add(comment);
        mutableItemKeyedDataSource.data.addAll(currentList);
        PagedList<Comment> pagedList = mutableItemKeyedDataSource.buildNewPagedList(currentList.getConfig());
        submitList(pagedList);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private LayoutFeedCommentListItemBinding mBinding;

        public ViewHolder(@NonNull View itemView, LayoutFeedCommentListItemBinding binding) {
            super(itemView);
            mBinding = binding;
        }

        public void bindData(Comment item) {
            mBinding.setComment(item);
            mBinding.labelAuthor.setVisibility(MyUserManager.get().getUserId()==item.author.userId?View.VISIBLE:View.GONE);
            mBinding.commentDelete.setVisibility(MyUserManager.get().getUserId()==item.author.userId?View.VISIBLE:View.GONE);

            if (!TextUtils.isEmpty(item.imageUrl)){
               mBinding.commentCover.setVisibility(View.VISIBLE);
               mBinding.commentCover.bindData(item.width,item.height,0, PixUtils.dp2px(200),PixUtils.dp2px(200),item.imageUrl);
               if (!TextUtils.isEmpty(item.videoUrl)){
                   mBinding.videoIcon.setVisibility(View.VISIBLE);
               }else {
                   mBinding.videoIcon.setVisibility(View.GONE);

               }
            }else {
                mBinding.commentCover.setVisibility(View.GONE);
                mBinding.videoIcon.setVisibility(View.GONE);
                mBinding.commentExt.setVisibility(View.GONE);
            }
        }
    }
}
