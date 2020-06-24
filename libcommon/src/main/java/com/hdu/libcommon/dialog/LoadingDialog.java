package com.hdu.libcommon.dialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;


import com.hdu.libcommon.R;
import com.hdu.libcommon.ViewHelper;
import com.hdu.libcommon.utils.PixUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

public class LoadingDialog extends AlertDialog {

    private TextView loadingText;
    public LoadingDialog(@NonNull Context context) {
        super(context);
    }

    public LoadingDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public void setLoadingText(String loading){
        if (this.loadingText!=null){
            this.loadingText.setText(loading);
        }
    }

    @Override
    public void show() {
        super.show();
        setContentView(R.layout.layout_loading_view);
        loadingText = findViewById(R.id.loading_text);
        Window window = getWindow();
        WindowManager.LayoutParams attributes = window.getAttributes();
        attributes.width = WindowManager.LayoutParams.WRAP_CONTENT;
        attributes.height = WindowManager.LayoutParams.WRAP_CONTENT;
        attributes.gravity = Gravity.CENTER;
        attributes.dimAmount = 0.35f; //界面变暗的程度
        //这个背景必须设置哦，否则 会出现对话框 宽度很宽
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //给转菊花的loading对话框来一个圆角
        ViewHelper.setViewOutline(findViewById(R.id.loading_layout), PixUtils.dp2px(10), ViewHelper.RADIUS_ALL);
        window.setAttributes(attributes);
    }
}
