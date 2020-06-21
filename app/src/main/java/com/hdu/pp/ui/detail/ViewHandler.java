package com.hdu.pp.ui.detail;

import com.hdu.pp.databinding.LayoutFeedDetailBottomInteractionBinding;
import com.hdu.pp.model.Feed;

import androidx.annotation.CallSuper;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
图文详情页和视频详情页共同部分的逻辑
 **/
public abstract class ViewHandler {

    private final FragmentActivity mActivity;
    private Feed mFeed;
    protected RecyclerView mRecyclerView;
    protected LayoutFeedDetailBottomInteractionBinding mInteractionBinding;
    protected FeedCommentAdapter listAdapter;

    public ViewHandler(FragmentActivity activity){
        mActivity = activity;

    }

    @CallSuper
    public void bindInitData(Feed feed) {
        mFeed = feed;
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mActivity,LinearLayoutManager.VERTICAL,false));
        mRecyclerView.setItemAnimator(null);
        listAdapter = new FeedCommentAdapter();
        mRecyclerView.setAdapter(listAdapter);

    }
}
