package com.hdu.pp.model;

import java.io.Serializable;

import androidx.annotation.Nullable;
import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.databinding.library.baseAdapters.BR;

//BaseObservable 字段变更重新执行数据绑定
public class Ugc extends BaseObservable implements Serializable {//帖子下面三个选项点赞分享 评论
    /**
     * likeCount : 153
     * shareCount : 0
     * commentCount : 4454
     * hasFavorite : false
     * hasLiked : true
     * hasdiss:false
     */

    public int likeCount;

    @Bindable
    public int getShareCount() {
        return shareCount;
    }

    public void setShareCount(int shareCount) {
        this.shareCount = shareCount;
    }

    public int shareCount;
    public int commentCount;
    public boolean hasFavorite;



    @Bindable
    public boolean isHasdiss() {
        return hasdiss;
    }

    public void setHasdiss(boolean hasdiss) {//点赞和踩是互斥的
        if (this.hasdiss==hasdiss)
            return;
        if (hasdiss){
            setHasLiked(false);
        }
        this.hasdiss = hasdiss;
        notifyPropertyChanged(BR._all);
    }

    public boolean hasdiss;
    public boolean hasLiked;

    public void setHasLiked(boolean hasLiked) {
        if(this.hasLiked == hasLiked)
            return;
        if(hasLiked) {
            likeCount = likeCount + 1;
            setHasdiss(false);
        }
        else
            likeCount = likeCount-1;
        this.hasLiked = hasLiked;
        notifyPropertyChanged(BR._all);
    }

    @Bindable
    public boolean isHasLiked() {
        return hasLiked;
    }



    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj==null||!(obj instanceof Ugc))
            return false;
        Ugc newUgc = (Ugc)obj;
        return likeCount == newUgc.likeCount
                && shareCount == newUgc.shareCount
                && commentCount == newUgc.commentCount
                && hasFavorite == newUgc.hasFavorite
                && hasLiked == newUgc.hasLiked
                && hasdiss == newUgc.hasdiss;
    }
}
