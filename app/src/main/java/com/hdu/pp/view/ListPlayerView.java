package com.hdu.pp.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.hdu.libcommon.PixUtils;
import com.hdu.pp.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ListPlayerView extends FrameLayout {
    private View bufferView; //缓冲进度条
    private ImageView playBtn;
    private PPImageView cover,blur;
    private String mCategory;
    private String mVideoUrl;

    public ListPlayerView(@NonNull Context context) {
        this(context,null);
    }

    public ListPlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ListPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.layout_player_view,this,true);

        bufferView = findViewById(R.id.buffer_view);
        cover = findViewById(R.id.cover);
        blur = findViewById(R.id.blur_background);
        playBtn = findViewById(R.id.play_btn);
    }

    public void bindData(String category,int widthPx,int heightPx,String coverUrl,String videoUrl){
        mCategory = category;
        mVideoUrl = videoUrl;

        cover.setImageUrl(cover,coverUrl,false);
        if (widthPx<heightPx){
            blur.setBlurImageUrl(coverUrl,10);
            blur.setVisibility(VISIBLE);
        }else{
            blur.setVisibility(INVISIBLE);
        }
        setSize(widthPx,heightPx);
    }

    private void setSize(int widthPx, int heightPx) {//设置真实的宽和高
        int maxWidth = PixUtils.getScreenWidth();
        int maxHeight = maxWidth;

        int layoutWidth = maxWidth; //计算之后的值
        int layoutHeight = 0;

        int coverWidth; //封面宽度
        int coverHeight;

        if(widthPx>=heightPx){
            coverWidth = maxWidth;
            layoutHeight = coverHeight = (int) (heightPx/(widthPx*1.0f/maxWidth));
        }else{
            layoutHeight =coverHeight= maxHeight;
            coverWidth = (int) (widthPx/(heightPx*1.0f/maxHeight));
        }
        ViewGroup.LayoutParams params = getLayoutParams();
        params.width = layoutWidth;
        params.height = layoutHeight;
        setLayoutParams(params);//设置ListPlayerView

        ViewGroup.LayoutParams blurParams = blur.getLayoutParams();
        params.width = layoutWidth;
        params.height = layoutHeight;
        blur.setLayoutParams(blurParams);

        FrameLayout.LayoutParams coverLayoutParams = (LayoutParams) cover.getLayoutParams();
        coverLayoutParams.width = coverWidth;
        coverLayoutParams.height = coverHeight;
        coverLayoutParams.gravity = Gravity.CENTER;
        cover.setLayoutParams(coverLayoutParams);

        FrameLayout.LayoutParams playBtnParams = (LayoutParams) playBtn.getLayoutParams();
        playBtnParams.gravity = Gravity.CENTER;
        playBtn.setLayoutParams(playBtnParams);
    }


}
