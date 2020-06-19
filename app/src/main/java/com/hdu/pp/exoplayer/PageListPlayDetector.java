package com.hdu.pp.exoplayer;

import android.graphics.Point;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;


/**
 * 列表视频滑动到就自动播放逻辑
 **/
public class PageListPlayDetector {

    private RecyclerView mRecyclerView;
    private List<IplayTarget> mTargets = new ArrayList<>();
    private IplayTarget playingTarget;

    public void addTarget(IplayTarget target){
        mTargets.add(target);
    }

    public void removeTarget(IplayTarget target){
        mTargets.remove(target);
    }

    public PageListPlayDetector(LifecycleOwner owner, RecyclerView recyclerView){
        mRecyclerView = recyclerView;

        //监听生命周期
        owner.getLifecycle().addObserver(new LifecycleEventObserver() {
            @Override
            public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                if (event == Lifecycle.Event.ON_DESTROY) {
                    playingTarget = null;
                    mTargets.clear();
                    owner.getLifecycle().removeObserver(this);
                }
            }
        });
        recyclerView.getAdapter().registerAdapterDataObserver(mDataObserver);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) { //列表滚动停止，自动播放
                    autoPlay();
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(playingTarget!=null&&playingTarget.isPlaying()&&isTargetInBounds(playingTarget)){
                    playingTarget.inActive();
                }
            }
        });
    }

    private RecyclerView.AdapterDataObserver mDataObserver = new RecyclerView.AdapterDataObserver() {
        //当有数据添加到RecyclerVIew就会回调
        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            autoPlay();
        }
    };

    private void autoPlay() {//判断是否满足自动播放条件

        if (mTargets.size()<=0||mRecyclerView.getChildCount()<=0)
            return;

        if (playingTarget!=null&&playingTarget.isPlaying()&&isTargetInBounds(playingTarget)){
            return;
        }

        IplayTarget activeTarget = null;
        for(IplayTarget target:mTargets){
            boolean inBounds = isTargetInBounds(target);
            if (inBounds) {
                activeTarget = target;
                break;
            }
        }
        if (activeTarget!=null){

            if (playingTarget!=null&&playingTarget.isPlaying()){//如果上一个Target正在播放则关闭
                playingTarget.inActive();
            }
            playingTarget = activeTarget;
            activeTarget.onActive();
        }


    }

    private boolean isTargetInBounds(IplayTarget target) {
        ViewGroup owner = target.getOwner();
        ensureRecyclerViewLocation();
        if (!owner.isShown() || !owner.isAttachedToWindow()) {
            return false;
        }

        int[] location = new int[2];
        owner.getLocationOnScreen(location);

        int center = location[1] + owner.getHeight() / 2; // Y值+height/2

        //承载视频播放画面的ViewGroup它需要至少一半的大小 在RecyclerView上下范围内
        return center >= rvLocation.x && center <= rvLocation.y;
    }

    private Point rvLocation = null;
    private Point ensureRecyclerViewLocation() {
        if(rvLocation==null) {
            int[] location = new int[2];
            mRecyclerView.getLocationOnScreen(location);

            int top = location[1];
            int bottom = location[1] + mRecyclerView.getHeight();

            rvLocation = new Point(top, bottom);
        }
        return rvLocation;
    }

    public void onResume() {
        if (playingTarget!=null){
            playingTarget.onActive();
        }
    }

    public void onPause() {
        if (playingTarget!=null){
            playingTarget.inActive();
        }
    }
}
