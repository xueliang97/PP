package com.hdu.pp.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import com.hdu.pp.R;
import com.hdu.pp.model.BottomBar;
import com.hdu.pp.model.Destination;
import com.hdu.pp.utils.AppConfig;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class AppBottomBar extends BottomNavigationView {
    private static int[] sIcons = new int[]{R.drawable.icon_tab_home, R.drawable.icon_tab_sofa
            ,R.drawable.icon_tab_publish,R.drawable.icon_tab_find,R.drawable.icon_tab_mine};

    public AppBottomBar(@NonNull Context context) {
        this(context,null);
    }

    public AppBottomBar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    @SuppressLint("RestrictedApi")
    public AppBottomBar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        BottomBar bottomBar = AppConfig.getBottomBar();
        List<BottomBar.Tabs> tabs = bottomBar.tabs;

        int[][] states = new int[2][];//定义选中/未选中的状态
        states[0] = new int[]{android.R.attr.state_selected};
        states[1] = new int[]{};

        int[] colors = new int[]{Color.parseColor(bottomBar.activeColor),Color.parseColor(bottomBar.inActiveColor)};//定义选中/未选中的颜色
        ColorStateList colorStateList = new ColorStateList(states,colors);

        setItemIconTintList(colorStateList);
        setItemTextColor(colorStateList);
        setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);//按钮什么状态都显示文本
        setSelectedItemId(bottomBar.selectTab);//设置默认选中按钮

        for(int i=0;i<tabs.size();i++){//依次将按钮显示在bottomBar上
            BottomBar.Tabs tab = tabs.get(i);
            if (!tab.enable)
                continue;
            int id = getId(tab.pageUrl);//将页面id与tab id相对应
            if (id<0)
                continue;
            MenuItem item = getMenu().add(0,id,tab.index,tab.title);
            item.setIcon(sIcons[tab.index]);
        }

        for(int i=0;i<tabs.size();i++){//设置按钮大小
            BottomBar.Tabs tab = tabs.get(i);
            int iconSize = dp2px(tab.size);

            BottomNavigationMenuView menuView = (BottomNavigationMenuView) getChildAt(0);
            BottomNavigationItemView itemView = (BottomNavigationItemView) menuView.getChildAt(tab.index);
            itemView.setIconSize(iconSize);

            if (TextUtils.isEmpty(tab.title)){//中间按钮着色
                itemView.setIconTintList(ColorStateList.valueOf(Color.parseColor(tab.tintColor)));
                itemView.setShifting(false);//点击时不上下浮动
            }
        }
        
    }

    private int dp2px(int size) {
        float s = getContext().getResources().getDisplayMetrics().density*size+0.5f;
        return (int)s;
    }

    private int getId(String pageUrl) {
        Destination destination = AppConfig.getsDestConfig().get(pageUrl);
        if(destination==null)
            return -1;
        return destination.id;
    }
}
