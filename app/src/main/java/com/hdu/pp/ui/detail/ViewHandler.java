package com.hdu.pp.ui.detail;

import android.content.Intent;
import android.view.ViewGroup;

import com.hdu.libcommon.EmptyView;
import com.hdu.libcommon.utils.PixUtils;
import com.hdu.pp.databinding.LayoutFeedDetailBottomInteractionBinding;
import com.hdu.pp.model.Comment;
import com.hdu.pp.model.Feed;
import com.hdu.pp.ui.MutableDataSource;
import com.hdu.pp.ui.MutableItemKeyedDataSource;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
图文详情页和视频详情页共同部分的逻辑
 **/
public abstract class ViewHandler {

    protected  FragmentActivity mActivity;
    protected Feed mFeed;
    protected RecyclerView mRecyclerView;
    protected LayoutFeedDetailBottomInteractionBinding mInteractionBinding;
    protected FeedCommentAdapter listAdapter;
    protected FeedDetailViewModel viewModel;
    protected CommentDialog commentDialog;

    public ViewHandler(FragmentActivity activity){
        mActivity = activity;
        viewModel = ViewModelProviders.of(activity).get(FeedDetailViewModel.class);

    }

    @CallSuper
    public void bindInitData(Feed feed) {
        mInteractionBinding.setOwner(mActivity);
        mFeed = feed;
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity,LinearLayoutManager.VERTICAL,false));
        mRecyclerView.setItemAnimator(null);
        listAdapter = new FeedCommentAdapter(mActivity);
        mRecyclerView.setAdapter(listAdapter);

        viewModel.setItemId(mFeed.itemId);

        viewModel.getPageData().observe(mActivity, new Observer<PagedList<Comment>>() {
            @Override
            public void onChanged(PagedList<Comment> comments) {
                listAdapter.submitList(comments);
                handleEmpty(comments.size()>0);
            }
        });
        mInteractionBinding.inputView.setOnClickListener((v)->{
            showCommentDialog();
        });

    }

    private void showCommentDialog() {
        if (commentDialog == null) {
            commentDialog = CommentDialog.newInstance(mFeed.itemId);
        }
        commentDialog.setCommentAddListener(comment -> {
            handleEmpty(true);
            listAdapter.addAndRefreshList(comment);
        });
        commentDialog.show(mActivity.getSupportFragmentManager(), "comment_dialog");
    }

    private EmptyView mEmptyView;
    protected void handleEmpty(boolean hasData){
        if(hasData){
            if (mEmptyView!=null){
                listAdapter.removeHeaderView(mEmptyView);
            }
        }else{
            if (mEmptyView==null){
                mEmptyView = new EmptyView(mActivity);
                RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.topMargin = PixUtils.dp2px(40);
                mEmptyView.setLayoutParams(layoutParams);
            }
            listAdapter.addHeaderVieew(mEmptyView);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (commentDialog!=null && commentDialog.isAdded()){
            commentDialog.onActivityResult(requestCode,resultCode,data);
        }
    }

    public void onPause() {
    }

    public void onResume(){

    }

    public void onBackPressed() {
    }
}
