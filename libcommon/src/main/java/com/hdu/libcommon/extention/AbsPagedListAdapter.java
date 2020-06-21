package com.hdu.libcommon.extention;

import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 一个能够添加HeaderView,FooterView的PagedListAdapter。
 * @param <T>
 * @param <VH>
 */
public abstract class AbsPagedListAdapter<T,VH extends RecyclerView.ViewHolder> extends PagedListAdapter<T,VH> {


    private SparseArray<View> mHeaders = new SparseArray<>();
    private SparseArray<View> mFooters = new SparseArray<>();

    private int BASE_ITEM_TYPE_HEADER = 100000;
    private int BASE_ITEM_TYPE_FOOTER = 200000;

    protected AbsPagedListAdapter(@NonNull DiffUtil.ItemCallback<T> diffCallback) {
        super(diffCallback);
    }

    public  void addHeaderVieew(View view){
        if (mHeaders.indexOfValue(view)<0){
            mHeaders.put(BASE_ITEM_TYPE_HEADER++,view);
            notifyDataSetChanged();
        }
    }

    public void addFooterView(View view) {
        //判断给View对象是否还没有处在mFooters数组里面
        if (mFooters.indexOfValue(view) < 0) {
            mFooters.put(BASE_ITEM_TYPE_FOOTER++, view);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        int itemCount = super.getItemCount();
        return itemCount+mHeaders.size()+mFooters.size();

    }

    public int getOriginalItemCount(){
        return getItemCount()-mHeaders.size()-mFooters.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (isHeaderPosition(position)){
            //返回该position对应的headerView的viewType
            return mHeaders.keyAt(position);
        }
        if (isFooterView(position)) {
            position = position-getOriginalItemCount()-mHeaders.size();
            return mFooters.keyAt(position);
        }
        position -= mHeaders.size();
        return getItemViewType2(position);
    }

    protected abstract int getItemViewType2(int position) ;

    private boolean isFooterView(int position) {
        return position>=getOriginalItemCount()+mHeaders.size();
    }

    private boolean isHeaderPosition(int position) {
        return position<mHeaders.size();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(mHeaders.indexOfKey(viewType)>=0){
            View view = mHeaders.get(viewType);
            return (VH)new RecyclerView.ViewHolder(view){

            };
        }

        if(mFooters.indexOfKey(viewType)>=0){
            View view = mFooters.get(viewType);
            return (VH)new RecyclerView.ViewHolder(view){

            };
        }



        return onCreateViewHolder2(parent,viewType);
    }

    protected abstract VH onCreateViewHolder2(ViewGroup parent, int viewType);

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        if (isHeaderPosition(position)||isFooterView(position))
            return;
        position = position-mHeaders.size();
        onBindViewHolder2(holder,position);
    }

    protected abstract void onBindViewHolder2(VH holder, int position);

    @Override
    public void registerAdapterDataObserver(@NonNull RecyclerView.AdapterDataObserver observer) {
        super.registerAdapterDataObserver(new AdapterDataObserverProxy(observer));
    }

    /**如果先添加了headerView,而后网络返回数据在更新在列表上
     * 由于Paging没有计算headerView,就会出现列表定位问题
     *RecyclerView#setAdapter方法会给Adpter注册AdapterDataObserver
     * 可以代理registerAdapterDataObserver()传进来的observer,在各个方法的实现中，把headerView的个数算上，再中转出去即可
     */
    private class AdapterDataObserverProxy extends RecyclerView.AdapterDataObserver{
        private RecyclerView.AdapterDataObserver mObserver;
        public AdapterDataObserverProxy(RecyclerView.AdapterDataObserver observer) {
            mObserver = observer;
        }

        public void onChanged() {
            mObserver.onChanged();
        }

        public void onItemRangeChanged(int positionStart, int itemCount) {
            mObserver.onItemRangeChanged(positionStart + mHeaders.size(), itemCount);
        }

        public void onItemRangeChanged(int positionStart, int itemCount, @Nullable Object payload) {
            mObserver.onItemRangeChanged(positionStart + mHeaders.size(), itemCount, payload);
        }

        public void onItemRangeInserted(int positionStart, int itemCount) {
            mObserver.onItemRangeInserted(positionStart + mHeaders.size(), itemCount);
        }

        public void onItemRangeRemoved(int positionStart, int itemCount) {
            mObserver.onItemRangeRemoved(positionStart + mHeaders.size(), itemCount);
        }

        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            mObserver.onItemRangeMoved(fromPosition + mHeaders.size(), toPosition + mHeaders.size(), itemCount);
        }

    }
}
