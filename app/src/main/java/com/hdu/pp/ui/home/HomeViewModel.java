package com.hdu.pp.ui.home;

import android.util.Log;

import com.alibaba.fastjson.TypeReference;
import com.hdu.libnetwork.ApiResponse;
import com.hdu.libnetwork.ApiService;
import com.hdu.libnetwork.JsonCallback;
import com.hdu.libnetwork.Request;
import com.hdu.pp.AbsViewModel;
import com.hdu.pp.model.Feed;
import com.hdu.pp.ui.MutableDataSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;
import androidx.paging.PagedList;

public class HomeViewModel extends AbsViewModel<Feed> {

    private static final String TAG = "HomeViewModel";
    private volatile boolean withCache = true;
    private String mFeedType;
    private MutableLiveData<PagedList<Feed>> cacheLiveData = new MutableLiveData<>();

    private AtomicBoolean loadAfter = new AtomicBoolean(false);

    /**
     * DataSource<Key,Value></> key:加载数据的条件 Value:数据实体类
     *
     * @return
     */

    @Override
    public DataSource createDataSource() {
        return mDataSource;
    }

    public MutableLiveData<PagedList<Feed>> getCacheLiveData() {
        return cacheLiveData;
    }

    public void setFeedType(String feedType){
        mFeedType = feedType;
    }

    ItemKeyedDataSource<Integer,Feed> mDataSource = new ItemKeyedDataSource<Integer, Feed>() {
        @Override //都切换到子线程了
        public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull LoadInitialCallback<Feed> callback) {
            //作加载初始化数据
            loadData(0,callback);
            withCache = false;
        }

        @Override
        public void loadAfter(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Feed> callback) {
            //作加载分页数据
            loadData(params.key,callback);
        }

        @Override
        public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Feed> callback) {
            //能够向前加载，2 -> 1
            callback.onResult(Collections.emptyList());
        }

        @NonNull
        @Override
        public Integer getKey(@NonNull Feed item) {
            return item.id; //根据最后一条item信息返回id
        }
    };

    private void loadData(int key, ItemKeyedDataSource.LoadCallback<Feed> callback) {
        //feeds/queryHotFeedsList

        if (key>0){ //此次分页
            loadAfter.set(true);

        }

        Request request = ApiService.get("/feeds/queryHotFeedsList")
                .addParam("feedType", null)
                .addParam("userId", 0)
                .addParam("feedId", key)
                .addParam("pageCount", 10)
                .responseType(new TypeReference<ArrayList<Feed>>() {
                }.getType());
        if(withCache){

            request.cacheStrategy(Request.CACHE_ONLY);
            request.execute(new JsonCallback<List<Feed>>() {
                @Override
                public void onCacheSuccess(ApiResponse<List<Feed>> response) {
                    Log.e(TAG, "onCacheSuccess: "+response.body.size() );
                    List<Feed> body = response.body;//将缓存数据转换成dataSource才能提供给paging显示
                    MutableDataSource dataSource = new MutableDataSource<Integer,Feed>();
                    dataSource.data.addAll(response.body);

                    PagedList pagedList = dataSource.buildNewPagedList(config);
                    cacheLiveData.postValue(pagedList);//livedata发送pagedList 子线程用post
                }
            });
        }

        try {
            Request netRequest = withCache?request.clone():request; //网络数据
            netRequest.cacheStrategy(key == 0? Request.NET_CACHE:Request.NET_ONLY);
            ApiResponse<List<Feed>> response = netRequest.execute();
            Log.e(TAG, "loadData: "+response.toString() );
            List<Feed> data = response.body==null?Collections.emptyList():response.body;

            callback.onResult(data);

            if(key>0){ //上拉加载
                //通过liveData发送数据 告诉UI 是否应该主动关闭上拉加载分页动画 liveDta类似于Rxjava
                ((MutableLiveData<Boolean>)getBoundaryPageData()).postValue(data.size()>0);
                loadAfter.set(false);
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        Log.e(TAG, "loadData: key:"+key );
    }

    public void loadAfter(int id, ItemKeyedDataSource.LoadCallback<Feed> callback) {
        if (loadAfter.get()){
            callback.onResult(Collections.emptyList());
            return;
        }

        ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                loadData(id,callback);
            }
        });
    }
}