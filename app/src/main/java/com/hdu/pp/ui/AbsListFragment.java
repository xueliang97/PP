package com.hdu.pp.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.hdu.libcommon.EmptyView;
import com.hdu.pp.AbsViewModel;
import com.hdu.pp.R;
import com.hdu.pp.databinding.LayoutRefreshViewBinding;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public abstract class AbsListFragment<T,M extends AbsViewModel<T>> extends Fragment implements OnRefreshListener, OnLoadMoreListener {
    //recyclerView列表通用配置 fragment初始化 配置与adapter相关

    protected LayoutRefreshViewBinding binding;
    protected RecyclerView mRecyclerView;
    protected SmartRefreshLayout mRefreshLayout;
    protected EmptyView mEmptyView;
    protected  M mViewModel;
    protected DividerItemDecoration decoration;
    protected PagedListAdapter<T,RecyclerView.ViewHolder> adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = LayoutRefreshViewBinding.inflate(inflater, container, false);
        binding.getRoot().setFitsSystemWindows(true);
        mRecyclerView = binding.recyclerView;
        mRefreshLayout = binding.refreshLayout;
        mEmptyView = binding.emptyView;

        mRefreshLayout.setEnableRefresh(true);
        mRefreshLayout.setEnableLoadMore(true);
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setOnLoadMoreListener(this);

        adapter = getAdapter();
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        mRecyclerView.setItemAnimator(null);

        //默认给列表中的Item加一个10dp的ItemDecoration
        decoration = new DividerItemDecoration(getContext(),LinearLayoutManager.VERTICAL);
        decoration.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.list_divider));
        mRecyclerView.addItemDecoration(decoration);

        genericViewModel();
        return binding.getRoot();
    }

    protected  void genericViewModel(){  //利用子类传递的泛型对象 实例化出absViewModel对象
        ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
        Type[] arguments = type.getActualTypeArguments();
        if (arguments.length>1){
            Type argument = arguments[1];
            Class modelClaz = ((Class) argument).asSubclass(AbsViewModel.class);
            mViewModel = (M)ViewModelProviders.of(this).get(modelClaz); //M泛型
            mViewModel.getPageData().observe(getViewLifecycleOwner(), new Observer<PagedList<T>>() {
                //初始化数据加载
                @Override
                public void onChanged(PagedList<T> pagedList) {
                    submitList(pagedList);//将pagedList与adapter关联
                }
            });

            mViewModel.getBoundaryPageData().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
                //监听分页有无更多数据，决定是否关闭加载动画
                @Override
                public void onChanged(Boolean hasData) {
                    finishRefresh(hasData);
                }
            });
        }
    }


    public void submitList(PagedList<T> pagedList){
        //只有新数据集大于0时，才能调用submitList
        if (pagedList.size()>0){
            adapter.submitList(pagedList);//将下拉刷新得到的结果变更到列表上
        }
        finishRefresh(pagedList.size()>0); //每次得到的数据都在pagedList里
    }

    public void finishRefresh(boolean hasData){//本次加载是否有数据
        PagedList<T> currentList = adapter.getCurrentList();
        hasData = hasData||currentList!=null&&currentList.size()>0;
        RefreshState state = mRefreshLayout.getState();
        if (state.isFooter&&state.isOpening){
            mRefreshLayout.finishLoadMore();
        }else if (state.isHeader && state.isOpening){
            mRefreshLayout.finishLoadMore();
        }

        if (hasData){
            mEmptyView.setVisibility(View.GONE);
        }else {
            mEmptyView.setVisibility(View.VISIBLE);
        }
    }

    public abstract PagedListAdapter getAdapter();//使用paging adapter要用PagedListAdapter
}
