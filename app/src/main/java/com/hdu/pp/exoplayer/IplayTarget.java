package com.hdu.pp.exoplayer;

import android.view.ViewGroup;

public interface IplayTarget {
    ViewGroup getOwner();

    void onActive();//自动播放

    void inActive(); //停止播放

    boolean isPlaying(); //是否正在播放

}
