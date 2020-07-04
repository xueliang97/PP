package com.hdu.pp.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.hdu.libcommon.utils.PixUtils;
import com.hdu.pp.R;
import com.hdu.pp.exoplayer.IplayTarget;
import com.hdu.pp.exoplayer.PageListPlay;
import com.hdu.pp.exoplayer.PagedListPalyManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ListPlayerView extends FrameLayout implements IplayTarget, PlayerControlView.VisibilityListener, Player.EventListener {
    public View bufferView; //缓冲进度条
    public ImageView playBtn;
    public PPImageView cover,blur;
    protected String mCategory;
    protected String mVideoUrl;
    protected boolean isPlaying;
    protected int mWidthPx;
    protected int mHeightPx;

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

        playBtn.setOnClickListener(v->{
            if(isPlaying()){
                inActive();
            }else {
                onActive();
            }
        });
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        PageListPlay pageListPlay = PagedListPalyManager.get(mCategory);
        pageListPlay.controlView.show();
        return true;


    }

    public void bindData(String category, int widthPx, int heightPx, String coverUrl, String videoUrl){
        mCategory = category;
        mVideoUrl = videoUrl;
        mHeightPx = heightPx;
        mWidthPx = widthPx;

        cover.setImageUrl(cover,coverUrl,false);
        if (widthPx<heightPx){
            blur.setBlurImageUrl(coverUrl,10);
            blur.setVisibility(VISIBLE);
        }else{
            blur.setVisibility(INVISIBLE);
        }
        setSize(widthPx,heightPx);
    }

    public void setSize(int widthPx, int heightPx) {//设置真实的宽和高
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


    @Override
    public ViewGroup getOwner() {
        return this;
    }

    @Override
    public void onActive() {
        PageListPlay pageListPlay = PagedListPalyManager.get(mCategory);
        PlayerView playerView = pageListPlay.playerView;
        PlayerControlView controlView = pageListPlay.controlView;
        SimpleExoPlayer exoPlayer = pageListPlay.exoPlayer;

        //此处我们需要主动调用一次 switchPlayerView，把播放器Exoplayer和展示视频画面的View ExoplayerView相关联
        //为什么呢？因为在列表页点击视频Item跳转到视频详情页的时候，详情页会复用列表页的播放器Exoplayer，然后和新创建的展示视频画面的View ExoplayerView相关联，达到视频无缝续播的效果
        //如果 我们再次返回列表页，则需要再次把播放器和ExoplayerView相关联
        pageListPlay.switchPlayerView(playerView, true);
        ViewParent parent = playerView.getParent();
        if(parent!=this) {
            if (parent != null) {
                ((ViewGroup) parent).removeView(playerView);
            }
            ViewGroup.LayoutParams layoutParams = cover.getLayoutParams();
            this.addView(playerView, 1, layoutParams);
        }

        ViewParent ctrlParent = controlView.getParent();
        if(ctrlParent!=this){
            if (ctrlParent!=null){
                ((ViewGroup)ctrlParent).removeView(controlView);
            }

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.BOTTOM;
            this.addView(controlView,params);
            controlView.setVisibilityListener(this);
        }
        controlView.show();

        if (TextUtils.equals(pageListPlay.playUrl,mVideoUrl)){//判断是否和上一次播放是同一个视频资源

        }else{
            MediaSource mediaSource = PagedListPalyManager.createMediaSource(mVideoUrl);
            exoPlayer.prepare(mediaSource);
            exoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);//无限循环播放
            exoPlayer.addListener(this);
        }

        exoPlayer.setPlayWhenReady(true);
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isPlaying = false;
        bufferView.setVisibility(GONE);
        cover.setVisibility(VISIBLE);
        playBtn.setVisibility(VISIBLE);
        playBtn.setImageResource(R.drawable.icon_video_play);

    }

    @Override
    public void inActive() {
        PageListPlay pageListPlay = PagedListPalyManager.get(mCategory);
        pageListPlay.exoPlayer.setPlayWhenReady(false);
        playBtn.setVisibility(VISIBLE);
        playBtn.setImageResource(R.drawable.icon_video_play);
    }

    @Override
    public boolean isPlaying() {
        return isPlaying;
    }

    @Override
    public void onVisibilityChange(int visibility) {
        playBtn.setVisibility(visibility);
        playBtn.setImageResource(isPlaying() ? R.drawable.icon_video_pause : R.drawable.icon_video_play);
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        //监听播放状态
        PageListPlay pageListPlay = PagedListPalyManager.get(mCategory);
        SimpleExoPlayer exoPlayer = pageListPlay.exoPlayer;

        if(playbackState==Player.STATE_READY&&exoPlayer.getBufferedPosition()!=0){
            cover.setVisibility(INVISIBLE);
            bufferView.setVisibility(INVISIBLE);

        }else if(playbackState==Player.STATE_BUFFERING){
            bufferView.setVisibility(VISIBLE);
        }

        isPlaying = playbackState == Player.STATE_READY && exoPlayer.getBufferedPosition() != 0 && playWhenReady;
        playBtn.setImageResource(isPlaying ? R.drawable.icon_video_pause : R.drawable.icon_video_play);
    }

    public View getPlayController() {
        PageListPlay listPlay = PagedListPalyManager.get(mCategory);
        return listPlay.controlView;
    }
}
