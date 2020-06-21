package com.hdu.libcommon.utils;

import android.util.DisplayMetrics;

import com.hdu.libcommon.global.AppGlobals;

public class PixUtils {
    public static int dp2px(int dpValue){  //dp ->px
        DisplayMetrics metrics = AppGlobals.getApplication().getResources().getDisplayMetrics();
        return (int) (metrics.density*dpValue*0.5f);
    }

    public static int getScreenWidth(){
        DisplayMetrics metrics = AppGlobals.getApplication().getResources().getDisplayMetrics();
        return metrics.widthPixels;
    }

    public static int getScreenHeigth(){
        DisplayMetrics metrics = AppGlobals.getApplication().getResources().getDisplayMetrics();
        return metrics.heightPixels;
    }

}
