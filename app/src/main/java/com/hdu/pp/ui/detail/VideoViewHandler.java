package com.hdu.pp.ui.detail;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hdu.pp.R;
import com.hdu.pp.databinding.LayoutFeedDetailTypeBinding;
import com.hdu.pp.databinding.LayoutFeedDetailTypeVideoHeaderBinding;
import com.hdu.pp.model.Feed;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;

/**
 * 视频详情页处理
 */
public class VideoViewHandler extends ViewHandler{


    private LayoutFeedDetailTypeBinding mVideoBinding;
    private String category;
    private boolean backPressed;


    public VideoViewHandler(FragmentActivity activity) {
        super(activity);

        mVideoBinding = DataBindingUtil.setContentView(activity, R.layout.layout_feed_detail_type);

        mInteractionBinding = mVideoBinding.bottomInteraction;
        mRecyclerView = mVideoBinding.recyclerView;

        View authorInfoView = mVideoBinding.authorInfo.getRoot();
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) authorInfoView.getLayoutParams();
        params.setBehavior(new ViewAnchorBehavior(R.id.play_view));

        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) mVideoBinding.playView.getLayoutParams();
        ViewZoomBehavior behavior = (ViewZoomBehavior) layoutParams.getBehavior();
        behavior.setViewZoomCallback(new ViewZoomBehavior.ViewZoomCallback() {
            @Override
            public void onDragZoom(int height) {
                int bottom = mVideoBinding.playView.getBottom();
                boolean moveUp = height < bottom;
                boolean fullscreen = moveUp ? height >= mVideoBinding.coordinator.getBottom() - mInteractionBinding.getRoot().getHeight()
                        : height >= mVideoBinding.coordinator.getBottom();
                setViewAppearance(fullscreen);
            }
        });
    }

    @Override
    public void bindInitData(Feed feed) {
        super.bindInitData(feed);
        mVideoBinding.setFeed(feed);

        category = mActivity.getIntent().getStringExtra(FeedDetailActivity.KEY_CATEGORY);
        mVideoBinding.playView.bindData(category,mFeed.width,mFeed.height,mFeed.cover,mFeed.url);

        //这里需要延迟一帧 等待布局完成，再来拿playerView的bottom值 和 coordinator的bottom值
        //做个比较。来校验是否进入详情页时时视频在全屏播放
        mVideoBinding.playView.post(()->{
           boolean fullscreen = mVideoBinding.playView.getBottom()>=mVideoBinding.coordinator.getBottom();
           setViewAppearance(fullscreen);
        });
        //给headerView、 绑定数据并添加到列表之上
        LayoutFeedDetailTypeVideoHeaderBinding headerBinding = LayoutFeedDetailTypeVideoHeaderBinding.inflate(LayoutInflater.from(mActivity),mRecyclerView,false);
        headerBinding.setFeed(mFeed);
        listAdapter.addHeaderVieew(headerBinding.getRoot());
    }




    private void setViewAppearance(boolean fullscreen) {
        mVideoBinding.setFullscreen(fullscreen);
        mInteractionBinding.setFullscreen(fullscreen);
        mVideoBinding.fullscreenAuthorInfo.getRoot().setVisibility(fullscreen ? View.VISIBLE : View.GONE);

        //底部互动区域的高度
        int inputHeight = mInteractionBinding.getRoot().getMeasuredHeight();
        //播放控制器的高度
        int ctrlViewHeight = mVideoBinding.playView.getPlayController().getMeasuredHeight();
        //播放控制器的bottom值
        int bottom =mVideoBinding.playView.getPlayController().getBottom();
        //全屏播放时，播放控制器需要处在底部互动区域的上面
        mVideoBinding.playView.getPlayController().setY(fullscreen ? bottom - inputHeight - ctrlViewHeight
                : bottom - ctrlViewHeight);
   //     mInteractionBinding.inputView.setBackgroundResource(fullscreen ? R.drawable.bg_edit_view2 : R.drawable.bg_edit_view);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        backPressed = true;
        //按了返回键后需要 恢复 播放控制器的位置。否则回到列表页时 可能会不正确的显示
        mVideoBinding.playView.getPlayController().setTranslationY(0);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!backPressed) {
            mVideoBinding.playView.inActive();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        backPressed = false;
        mVideoBinding.playView.onActive();
    }
}
