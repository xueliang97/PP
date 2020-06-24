package com.hdu.pp;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.paging.DataSource;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

public abstract class AbsViewModel<T> extends ViewModel {

    protected PagedList.Config config;
    private DataSource dataSource;
    private LiveData<PagedList<T>> liveData;

    private MutableLiveData<Boolean> boundaryPageData = new MutableLiveData<>();

    public AbsViewModel() {

        config = new PagedList.Config.Builder()
                .setPageSize(10)//每次分页加载数量
                .setInitialLoadSizeHint(12).build();//第一次加载时的数量
//.setEnablePlaceholders()占位符

        liveData = new LivePagedListBuilder(factory, config)  //初始数据加载
                .setInitialLoadKey(0) //加载初始化时需要传递的参数
                .setBoundaryCallback(callback) //监听pagedlist数据加载状态
                .build();
        //用livedata加载分页数据



    }

    public LiveData<Boolean> getBoundaryPageData() {
        return boundaryPageData;
    }

    public LiveData<PagedList<T>> getPageData(){
        return liveData;
    }

    public DataSource getDataSource(){
        return dataSource;
    }

    //PagedList数据被加载，情况边界回调callback
    PagedList.BoundaryCallback<T> callback = new PagedList.BoundaryCallback<T>() {
        @Override
        public void onZeroItemsLoaded() { //新提交的PagedList中没有数据
            boundaryPageData.postValue(false);
        }

        @Override
        public void onItemAtFrontLoaded(@NonNull T itemAtFront) {
            //新提交的PagedList中第一条数据被加载到列表上
            boundaryPageData.postValue(true);
        }

        @Override
        public void onItemAtEndLoaded(@NonNull T itemAtEnd) {
            //新提交的PagedList中最后一条数据被加载到列表上
        }
    };

    DataSource.Factory factory = new DataSource.Factory() {
        @NonNull
        @Override
        public DataSource create() {
            if (dataSource==null || dataSource.isInvalid()) {
                dataSource = createDataSource();
            }
            return dataSource;
        }
    };

    //创建提供数据的数据源对象
    public abstract DataSource createDataSource() ;

    //做一些清理工作
    @Override
    protected void onCleared() {
        super.onCleared();
    }


}
