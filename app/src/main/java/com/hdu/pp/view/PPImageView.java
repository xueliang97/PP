package com.hdu.pp.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.hdu.libcommon.utils.PixUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.databinding.BindingAdapter;
import jp.wasabeef.glide.transformations.BlurTransformation;

public class PPImageView extends AppCompatImageView {//重写ImageView 使其可以databinding图片 写加载图片的方法
    public PPImageView(Context context) {
        super(context);
    }

    public PPImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PPImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setImageUrl(String imageUrl) {
        setImageUrl(this, imageUrl, false);
    }


    @BindingAdapter(value = {"image_url","isCircle"},requireAll = false) //databinding绑定的函数，从网络上加载绑定头像
    public static void setImageUrl(PPImageView view,String image_url,boolean isCircle){
        RequestBuilder<Drawable> builder = Glide.with(view).load(image_url);
        if (isCircle){
            builder.transform(new CircleCrop());
        }
        ViewGroup.LayoutParams params = view.getLayoutParams();
        if (params!=null && params.width>0 && params.height>0) {
            builder.override(params.width,params.height); //重新适应图片的尺寸
        }
        builder.into(view);
    }

    public void bindData(int widthPx,int heightPx,int marginLeft,String image_url) {
        bindData(widthPx, heightPx, marginLeft, PixUtils.getScreenWidth(),PixUtils.getScreenHeigth(),image_url);
    }

    public void bindData(int widthPx,int heightPx,int marginLeft,int maxWidth,int maxHeight,String image_url){
        if(widthPx<=0||heightPx<=0){
            Glide.with(this).load(image_url).into(new SimpleTarget<Drawable>() {
                @Override
                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                    int width = resource.getIntrinsicWidth(); //根据网络获取图片设置宽高
                    int height = resource.getIntrinsicHeight();
                    setSize(width,height,marginLeft,maxWidth,maxHeight);

                    setImageDrawable(resource);
                }
            });
            return;
        }
        setSize(widthPx,heightPx,marginLeft,maxWidth,maxHeight);
        setImageUrl(this,image_url,false);
    }

    private void setSize(int width, int height, int marginLeft, int maxWidth, int maxHeight) {
        int finalWidth,finalHeight;
        if(width>height){
            finalWidth = maxWidth;
            finalHeight = (int) (height/(width*1.0f/finalWidth ));
        }else{
            finalHeight = maxHeight;
            finalWidth = (int) (width/(height*1.0f/finalHeight));
        }

        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(finalWidth, finalHeight);
        params.leftMargin = height>width? PixUtils.dp2px(marginLeft):0;  //
        setLayoutParams(params);
    }

    public void setBlurImageUrl(String coverUrl, int radius) {//载入高斯模糊图片
        Glide.with(this).load(coverUrl).override(50)
                .transform(new BlurTransformation())
                .dontAnimate()
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        setBackground(resource);
                    }
                });
    }
}
