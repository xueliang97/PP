package com.hdu.pp.ui.find;

import com.alibaba.fastjson.TypeReference;
import com.hdu.libnetwork.ApiResponse;
import com.hdu.libnetwork.ApiService;
import com.hdu.pp.AbsViewModel;
import com.hdu.pp.login.MyUserManager;
import com.hdu.pp.model.TagList;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.NonNull;
import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;

public class TagListViewModel extends AbsViewModel<TagList> {

    private String tagType;
    private int offset;
    private AtomicBoolean loadAfter = new AtomicBoolean(); //标记paging框架是否也在做分页加载 因为手动请求可能与自动加载重合
    private MutableLiveData switchTabLiveData = new MutableLiveData();

    public void setTagType(String s){
        tagType = s;
    }

    public MutableLiveData getSwitchTabLiveData() {
        return switchTabLiveData;
    }

    @Override
    public DataSource createDataSource() {
        return new DataSource();
    }

    private class DataSource extends ItemKeyedDataSource<Long,TagList>{

        @Override
        public void loadInitial(@NonNull LoadInitialParams<Long> params, @NonNull LoadInitialCallback<TagList> callback) {
            loadData(params.requestedInitialKey,callback);
        }

        @Override
        public void loadAfter(@NonNull LoadParams<Long> params, @NonNull LoadCallback<TagList> callback) {
            loadData(params.key, callback);
        }

        @Override
        public void loadBefore(@NonNull LoadParams<Long> params, @NonNull LoadCallback<TagList> callback) {
            callback.onResult(Collections.emptyList());
        }

        @NonNull
        @Override
        public Long getKey(@NonNull TagList item) {
            //获取分页时入参
            return item.tagId;
        }


        private void loadData(Long requestKey, LoadCallback<TagList> callback) {
            if (requestKey > 0) { //分页请求
                loadAfter.set(true);
            }
            ApiResponse<List<TagList>> response = ApiService.get("/tag/queryTagList")
                    .addParam("userId", MyUserManager.get().getUserId())
                    .addParam("tagId", requestKey)
                    .addParam("tagType", tagType)
                    .addParam("pageCount", 10)
                    .addParam("offset", offset) //当前列表上已经展示的item 数量
                    .responseType(new TypeReference<ArrayList<TagList>>() {
                    }.getType())
                    .execute();

            List<TagList> result = response.body==null?Collections.emptyList():response.body;
            callback.onResult(result);
            if (requestKey > 0) {
                loadAfter.set(false);
                offset += result.size();
                //传递本次分页结果
                ((MutableLiveData) getBoundaryPageData()).postValue(result.size() > 0);
            } else {
                offset = result.size();
            }

        }

    }

    public void loadData(long tagId, ItemKeyedDataSource.LoadCallback callback){
        if (tagId <= 0 || loadAfter.get()) {
            callback.onResult(Collections.emptyList());
            return;
        }
        ArchTaskExecutor.getIOThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                ((TagListViewModel.DataSource) getDataSource()).loadData(tagId, callback);
            }
        });
    }


}
