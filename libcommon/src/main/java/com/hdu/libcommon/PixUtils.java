package com.hdu.libcommon;

import android.util.DisplayMetrics;

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
