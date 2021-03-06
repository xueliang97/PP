package com.hdu.pp.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.hdu.libnavannotation.FragmentDestination;
import com.hdu.pp.R;
import com.hdu.pp.exoplayer.PageListPlayDetector;
import com.hdu.pp.model.Feed;
import com.hdu.pp.ui.AbsListFragment;
import com.hdu.pp.ui.MutableDataSource;
import com.scwang.smartrefresh.layout.api.RefreshLayout;

import java.util.List;

@FragmentDestination(pageUrl = "main/tabs/home",asStarter = true)
public class HomeFragment extends AbsListFragment<Feed,HomeViewModel> {
    private static final String TAG = "HomeFragment";
    private PageListPlayDetector playDetector;
    private String feedType;
    private boolean shouldPause = true;

    public static HomeFragment newInstance(String feedType) {

        Bundle args = new Bundle();
        args.putString("feedType",feedType);
        HomeFragment fragment = new HomeFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mViewModel.getCacheLiveData().observe(getViewLifecycleOwner(), new Observer<PagedList<Feed>>() {
            @Override
            public void onChanged(PagedList<Feed> feeds) {
                submitList(feeds);
            }
        });
        playDetector = new PageListPlayDetector(this, mRecyclerView);

        mViewModel.setFeedType(feedType);
    }

    @Override
    public PagedListAdapter  getAdapter() {
        String feedType = getArguments()==null ? "all" : getArguments().getString("feedType");
        return new FeedAdapter(getContext(),feedType){
            @Override
            public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
                super.onViewAttachedToWindow(holder);
                if (holder.isVideoItem()) {
                    playDetector.addTarget(holder.getListPlayerView());
                }
            }

            @Override
            public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
                super.onViewDetachedFromWindow(holder);
                playDetector.removeTarget(holder.getListPlayerView());
            }

            @Override
            public void onStartFeedDetailActivity(Feed feed) {
                boolean isVideo = feed.itemType==Feed.TYPE_VIDEO;
                shouldPause = !isVideo;
            }

            //每提交一次pageList对象到adapter就会触发
            //每调用一次adapter.submitlist
            @Override
            public void onCurrentListChanged(@Nullable PagedList<Feed> previousList, @Nullable PagedList<Feed> currentList) {
                if (previousList!=null&&currentList!=null){
                    if (!currentList.containsAll(previousList)){//下拉刷新
                        mRecyclerView.scrollToPosition(0);
                    }
                }
            }
        };
    }

    @Override
    public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
        final PagedList<Feed> currentList = adapter.getCurrentList();
        if (currentList==null||currentList.size()<=0){
            finishRefresh(false);
            return;
        }

        Feed feed = currentList.get(adapter.getItemCount() - 1);
        mViewModel.loadAfter(feed.id, new ItemKeyedDataSource.LoadCallback<Feed>() {
            @Override
            public void onResult(@NonNull List<Feed> data) {
                PagedList.Config config = currentList.getConfig();
                if (data!=null && data.size()>0){
                    MutableDataSource dataSource = new MutableDataSource();
                    dataSource.data.addAll(currentList);
                    dataSource.data.addAll(data);
                    PagedList pagedList = dataSource.buildNewPagedList(config);
                    submitList(pagedList);
                }
            }
        });
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        //invalidate 之后paging会重新创建一个DataSource重新调用它的loadInitial方法加载初始化数据
        mViewModel.getDataSource().invalidate();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(hidden){
            playDetector.onPause();
        }else {
            playDetector.onResume();
        }
    }

    @Override
    public void onPause() {
        if (shouldPause){
            playDetector.onPause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        //只恢复对用户可见页面的视频播放
        if (getParentFragment()!=null){
            if (getParentFragment().isVisible()&&isVisible()){
                playDetector.onResume();
            }
        }else {
            if (isVisible()){
                playDetector.onResume();
            }
        }

    }
}