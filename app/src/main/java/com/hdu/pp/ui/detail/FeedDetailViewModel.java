package com.hdu.pp.ui.detail;

import com.alibaba.fastjson.TypeReference;
import com.hdu.libnetwork.ApiResponse;
import com.hdu.libnetwork.ApiService;
import com.hdu.pp.AbsViewModel;
import com.hdu.pp.login.MyUserManager;
import com.hdu.pp.model.Comment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.paging.DataSource;
import androidx.paging.ItemKeyedDataSource;

public class FeedDetailViewModel extends AbsViewModel<Comment> {
    private long itemId;

    @Override
    public DataSource createDataSource() {
        return null;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    //提供数据的数据源，根据最后一个item来分页
    class DataSource extends ItemKeyedDataSource<Integer,Comment>{

        //加载初始化数据
        @Override
        public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull LoadInitialCallback<Comment> callback) {
            loadData(params.requestedInitialKey,params.requestedLoadSize,callback);
        }

        //加载分页
        @Override
        public void loadAfter(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Comment> callback) {
            if (params.key>0) {
                loadData(params.key, params.requestedLoadSize, callback);
            }
        }

        //向前加载数据一般不需要
        @Override
        public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Comment> callback) {
            callback.onResult(Collections.emptyList());
        }

        @NonNull
        @Override
        public Integer getKey(@NonNull Comment item) {
            return item.id;
        }

        private void loadData(Integer key, int requestedLoadSize, LoadCallback<Comment> callback) {
            ApiResponse<List<Comment>> response = ApiService.get("/comment/queryFeedComments")
                    .addParam("id", key)
                    .addParam("itemId", itemId)
                    .addParam("userId", MyUserManager.get().getUserId())
                    .addParam("pageCount", requestedLoadSize)
                    .responseType(new TypeReference<ArrayList<Comment>>() {
                    }.getType())
                    .execute();
            List<Comment> list = response.body == null ? Collections.emptyList() : response.body;
            callback.onResult(list);
        }
    }
}
